package part3_stores_and_serialization

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Cassandra extends App {
  val cassandraActorSystem = ActorSystem("cassandraSystem", ConfigFactory.load().getConfig("cassandraDemo"))
  val persistentActor = cassandraActorSystem.actorOf(Props[SimplePersistActor], "simplePersistentActor")

  (1 to 10).foreach(i => persistentActor ! s"I love Akka [$i]")

  persistentActor ! "print"
  persistentActor ! "snap"

  (11 to 20).foreach(i => persistentActor ! s"I love Akka [$i]")
}
