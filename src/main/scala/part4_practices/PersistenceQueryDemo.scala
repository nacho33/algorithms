package part4_practices

import akka.NotUsed
import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.journal.{Tagged, WriteEventAdapter}
import akka.persistence.query.{EventEnvelope, Offset, PersistenceQuery}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory

import scala.util.Random

object PersistenceQueryDemo extends App {

  val system = ActorSystem("PersistenceQueryDemo", ConfigFactory.load.getConfig("persistenceQuery"))

  // read journal
  val readJournal = PersistenceQuery(system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  // give me all persistenceIds
  //  Source viene de akka streams, infinite akka stream, cuando un nuevo persistentActor
  // persiste un evento aparece directamente
  // Se muestran los eventos en orden en que se persisten
  val persistenceIds: Source[String, NotUsed] = readJournal.persistenceIds()
  // Si queremos los del momento, y el stream se cerrará al final, por lo que no se
  // mostrarán los nuevos
  // Se muestran los eventos en orden en que se persisten
  val persistenceIdsv2: Source[String, NotUsed] = readJournal.currentPersistenceIds()

  implicit val materializer: ActorMaterializer = ActorMaterializer()(system)

//  persistenceIds.runForeach { persistenceId =>
//    println(s"Found persistence Id: $persistenceId")
//  }

  class SimplePersitenceActor extends PersistentActor with ActorLogging {

    override def persistenceId: String = "persistence-query-id-1"

    override def receiveCommand: Receive = {
      case m => persist(m) { e =>
        log.info(s"persisted $m")
      }
    }

    override def receiveRecover: Receive = {
      case e =>
        log.info(s"Recovered: $e")
    }
  }

  val simpleActor = system.actorOf(Props[SimplePersitenceActor], "simplePersistentActor")

  import system.dispatcher
  import scala.concurrent.duration._
  system.scheduler.scheduleOnce(5 seconds){
    simpleActor ! "hello again persistent actor"
  }

  // pick up events by a persistenceID
  val events: Source[EventEnvelope, NotUsed] =
    readJournal.eventsByPersistenceId("persistence-query-id-1", 0, Long.MaxValue)

  events.runForeach{event =>
    println(s"read event: $event")
  }

  // events by tags, across multiple persistenceIds
  val genres = Array("pop", "rock", "hip hop", "indie", "jazz")
  case class Song(artist: String, title: String, genre: String)
  // Command
  case class Playlist(songs: List[Song])
  // events
  case class PlaylistPurchased(id: Int, songs: List[Song])

  class MusisStoreCheckoutActor extends PersistentActor with ActorLogging {
    override def persistenceId: String = "music-store-checkout"

    var latestPlaylistId= 0

    override def receiveCommand: Receive = {
      case Playlist(songs) =>
        persist(PlaylistPurchased(latestPlaylistId, songs)) {_ =>
          log.info(s"User purchased: $songs")
          latestPlaylistId += 1
        }
    }

    override def receiveRecover: Receive = {
      case event @ PlaylistPurchased(id, _) =>
        log.info(s"Recovered: $event")
        latestPlaylistId = id
    }


  }

  class MusicStoreEventAdapter extends WriteEventAdapter {
    override def manifest(event: Any): String = "music-store"
    override def toJournal(event: Any): Any = event match {
      case event @ PlaylistPurchased(id, songs) =>
        val genres = songs.map(_.genre).toSet
        Tagged(event, genres)

      case event => event

    }
  }

  val checkoutActor = system.actorOf(Props[MusisStoreCheckoutActor], "musicStoreActor")
  val r = new Random()
  (1 to 10).foreach{ i =>
    val maxSongs = r.nextInt(5)
    val songs = (1 to maxSongs).map{ s =>
      val randomGenre = genres(r.nextInt(5))
      Song(s"Artist $s", s"Akka song $s", randomGenre)
    }
    checkoutActor ! Playlist(songs.toList)

  }
  // No garantiza el orden de los eventos porque va sobre multiples persistenceIDs
  val rockPlaylist: Source[EventEnvelope, NotUsed] = readJournal.eventsByTag("rock", Offset.noOffset)
  rockPlaylist.runForeach(event => println(s"Found playlist with a rock song $event"))

}
