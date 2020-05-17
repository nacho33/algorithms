package part2_event_sourcing

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor

import scala.collection.mutable

object PersistenceActorsExercise extends App{

  /*
   * Persistent actor for a voting station
   * Keep:
   *  - citizen voting
   *  - poll Map(candidate, votes)
   */

  val CANDIDATES = List("Casado", "Sanchez", "Iglesias", "Arrimadas", "Me")

  case class Vote(citizenPID: String, candidate: String)

  case object Shutdown
  case object PrintResult
  // Events, we don't really need it
  // case class PersistingVote(id: Int, citizenPID: String, candidate: String)

  class VotingStationActor extends PersistentActor with ActorLogging {

    var citizensAlreadyVoted: mutable.Set[String] = new mutable.HashSet[String]()
    var poll: Map[String, Int] = Map()
    override def persistenceId: String = "simple-voting-station" // unique

    def handleInternalStateChange(vote: Vote) = {
      val votes = poll.getOrElse(vote.candidate, 0)
      citizensAlreadyVoted + vote.citizenPID
      poll = poll + (vote.candidate -> (votes + 1))
    }

    override def receiveCommand: Receive = {
      case vote @ Vote(citizenPID, candidate) =>
        if(!CANDIDATES.contains(candidate))
          log.warning(s"Wrong candidate: $candidate")
        else if(citizensAlreadyVoted.contains(citizenPID))
          log.warning(s"Citizen: $citizenPID already voted")
        else {
          persist(vote) { _ => // command sourcing, we persist the command as it has all the info we need
            log.info(s"Persisted vote for candidate: $candidate")
            handleInternalStateChange(vote)
          }
        }
      case  Shutdown => context.stop(self)
      case PrintResult =>
        log.info(s"The results are: $poll")
    }

    override def receiveRecover: Receive = {
      case vote @ Vote(citizenPID, candidate) => // We only have valid votes
        handleInternalStateChange(vote)
        log.info(s"Recovering vote for $candidate")
    }

    override def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
      log.error(s"Failure to persist $event because of $cause")
      super.onPersistFailure(cause, event, seqNr)
    }

    override def onPersistRejected(cause: Throwable, event: Any, seqNr: Long): Unit = {
      log.error(s"Persist rejected for $event because of $cause")
      super.onPersistRejected(cause, event, seqNr)
    }


  }

  val system = ActorSystem("VotingStationSystem")
  val votingStation = system.actorOf(Props[VotingStationActor], "voting-station")

  val votesMap: Map[String, String] = Map(
    UUID.randomUUID().toString -> "Iglesias",
    UUID.randomUUID().toString -> "Me",
    UUID.randomUUID().toString -> "Me",
    UUID.randomUUID().toString -> "Arrimadas",
    UUID.randomUUID().toString -> "Sanchez",
    UUID.randomUUID().toString -> "No one",
    UUID.randomUUID().toString -> "Me"
  )

  votesMap.foreach(vote => votingStation ! Vote(vote._1, vote._2))
  votingStation ! PrintResult
  votingStation ! Shutdown

}

/*
object VoteSender {

  val system = ActorSystem("VotingStationSystem")

  val votingStation = system.actorSelection("/user/VotingStationSystem/voting-station")

  class VoteActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case s: String =>

    }
  }

  scala.io.Source.stdin.getLines().foreach { line =>
    onlineVotingActor ! Vote(Person.generate, line)
  }
}
*/
