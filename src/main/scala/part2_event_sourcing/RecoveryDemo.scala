package part2_event_sourcing

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.{PersistentActor, Recovery, RecoveryCompleted, SnapshotSelectionCriteria}

object RecoveryDemo extends App {

  case class Command(contents: String)
  case class Event(id: Int, contents: String)
  class RecoveryActor extends PersistentActor with ActorLogging {

    override def persistenceId: String = "recorevy-actor"

    override def receiveCommand: Receive = online(0)

    def online(persistId: Int): Receive = {
      case Command(contents) =>
        persist(Event(persistId, contents)) {event =>
          log.info(s"Persisted event: $event, recovery is: ${if(this.recoveryFinished) "" else "NOT"} finished.")
          context.become(online(persistId + 1))
        }
    }

    override def receiveRecover: Receive = {
      case RecoveryCompleted =>
        // additional initializations
        log.info("I have finished recovering")
      case Event(id, contents )=>
        //if(contents.contains("314")) throw new RuntimeException("I can't take it anymore")
        log.info(s"Recoreved event: $contents, recovery is: ${if(this.recoveryFinished) "" else "NOT"} finished.")
        context.become(online(id + 1)) // Won't change the handler for receiveRecover
      // After recovery the 'normal' handler(receiveCommand) will be the result of all the stacking of context.become
    }

    override def onRecoveryFailure(cause: Throwable, event: Option[Any]): Unit = {
      log.error("I failed during recovery")
      super.onRecoveryFailure(cause, event)
    }

    // we recover at most 100 events (first 100)
    //override def recovery: Recovery = Recovery(toSequenceNr = 100)

    // recovery from the last snapshot
    // override def recovery: Recovery = Recovery(fromSnapshot = SnapshotSelectionCriteria.Latest)

    // we don't want to recovery
    override def recovery: Recovery = Recovery.none

  }

  val system = ActorSystem("RecoveryDemo")
  val recoveryActor = system.actorOf(Props[RecoveryActor], "recoveryActor")

  /*
   * stashing commands, all commands are stashed during recovery
   */
  (1 to 1000).foreach { i => recoveryActor ! Command(s"Command: $i")}

  /*
   * failure during recovery => call onRecoveryFailure, the actor is stopped
   *
   * We can customize recovery, overriding method recovery
   * DO NOT persist more events after a customized incomplete recovery
   *
   * Recovery status or knowing when you're done recovering
   * getting a signal when you're done recovering
   *
   * stateless actors
   *
   * It's not possible to change receiveRecover status, so we persist the info we need
   * so we have it in the events.
   * We can't change receiveCommand behaviour during recovery, it will change at the end
   * of the recovery
   */

}
