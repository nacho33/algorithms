package part3_stores_and_serialization

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}

class SimplePersistActor extends PersistentActor with ActorLogging {

    override def persistenceId: String = "simple-persistent-actor"

    // mutable state
    var nMessages = 0

    override def receiveCommand: Receive = {
      case "print" =>
        log.info(s"I have persisted $nMessages message so far")
      case "snap" =>
        saveSnapshot(nMessages)
      case SaveSnapshotSuccess(metadata) =>
        log.info(s"save snapshot was successful: $metadata")
      case SaveSnapshotFailure(_, cause) =>
        log.info(s"save snapshot failed: $cause")
      case message => persist(message) { _ =>
        log.info(s"Persisting $message")
        nMessages += 1
      }
    }

    override def receiveRecover: Receive = {
      case RecoveryCompleted =>
        log.info(s"Recovery done ")
      case SnapshotOffer(metadata, payload: Int) =>
        log.info(s"Recovered snapshot $payload ")
        nMessages = payload
      case message =>
        log.info(s"Recovered $message")
        nMessages += 1

    }
  }
