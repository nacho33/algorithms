package part2_event_sourcing

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}

import scala.collection.mutable

object Snapshots extends App {

  /*
   * Chat
   */

  // Commands
  case class ReceivedMessage(contents: String)
  case class SentMessage(contents: String)

  // Events
  case class ReceivedMessageRecord(id: Int, contents: String)
  case class SentMessageRecord(id: Int, contents: String)

  // Best practice
  object Chat {
    def props(owner: String, contact: String) = Props(new Chat(owner, contact))
  }

  class Chat(owner: String, contact: String) extends PersistentActor with ActorLogging {

    val MAX_MESSAGES = 10
    var commandsWithoutCheckpoint = 0
    var currentMessageId = 0
    val lastMessages = new mutable.Queue[(String, String)]()

    override def persistenceId: String = s"$owner-$contact-chat"

    override def receiveCommand: Receive = {
      case ReceivedMessage(contents) =>
        persist(ReceivedMessageRecord(currentMessageId, contents)){ e =>
          log.info(s"Received message: $contents")
          maybeReplaceMessage(contact, contents)
          currentMessageId += 1
          maybeCheckpoint()
        }
      case SentMessage(contents) =>
        persist(SentMessageRecord(currentMessageId, contents)) { e =>
          log.info(s"Sent message: $contents")
          maybeReplaceMessage(owner, contents)
          currentMessageId += 1
          maybeCheckpoint()

        }
      case "print" =>
        log.info(s"Most recent messages: $lastMessages")

      // snapshots related messages
      case SaveSnapshotSuccess(metadata) =>
        log.info(s"Saving Snapshot succedeed with metadata: $metadata")
      case SaveSnapshotFailure(metadata, reason) =>
        log.warning(s"Saving Snapshot $metadata failed because of $reason")

    }

    override def receiveRecover: Receive = {
      case ReceivedMessageRecord(id, contents) =>
        log.info(s"Recover received message $id: $contents")
        maybeReplaceMessage(contact, contents)
        currentMessageId = id

      case SentMessageRecord(id, contents) =>
        log.info(s"Recover sent message $id: $contents")
        maybeReplaceMessage(owner, contents)
        currentMessageId = id

      case SnapshotOffer(metadata, contents) =>
        log.info(s"Recover Snapshot: $metadata")
        contents.asInstanceOf[mutable.Queue[(String, String)]].foreach(lastMessages.enqueue(_))
    }

    def maybeReplaceMessage(sender: String, contents: String): Unit = {
      if(lastMessages.size >= MAX_MESSAGES) lastMessages.dequeue()
      lastMessages.enqueue((sender, contents))
    }

    def maybeCheckpoint(): Unit = {
      commandsWithoutCheckpoint += 1
      if(commandsWithoutCheckpoint >= MAX_MESSAGES){
        log.info(s"Saving Checkpoint...")
        saveSnapshot(lastMessages)  // asynchronous operation, when it is achieved the persistedActor recieve a
        //SaveSnapshotSuccess, but during this time we've been processing the rest of the messages
        commandsWithoutCheckpoint = 0
      }
    }
  }

  val system = ActorSystem("SnapshotDemo")
  val chat = system.actorOf(Chat.props("nacho123","pepe321"))


  /*(1 to 10001).foreach{ i =>
    chat ! ReceivedMessage(s"Akka rocks: $i")
    chat ! SentMessage(s"Akka rules: $i")
  }*/

  chat ! "print"

  /*
   * Pattern
   * - after each persist, maybe save a snapshot(logig is up to you)
   * - if you save a snapshot, handle the SnapshotOffer in receiveRecover
   * - (optional but best practice) handle SaveSnapshotSuccess and
   *    SaveSnapshotFailure in receiveCommand
   */
}
