package part2_event_sourcing

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.persistence.PersistentActor

object PersistAsyncDemo extends App {

  case class Command(contents: String)
  case class Event(contents: String)

  object CriticalStreamProcessor {
    def props(eventAggregator: ActorRef) = Props(new CriticalStreamProcessor(eventAggregator))
  }

  class CriticalStreamProcessor(eventAggregator: ActorRef) extends PersistentActor with ActorLogging {

    override def persistenceId: String = "critical-stream-processor"

    override def receiveCommand: Receive = {
      case Command(contents) =>
        eventAggregator ! s"Processing $contents"
        persistAsync(Event(contents)){ e =>
          eventAggregator ! e
        }

        val processContents = contents + "_processed"
        persistAsync(Event(processContents)){ e =>
          eventAggregator ! e
        }
    }

    override def receiveRecover: Receive = {
      case message => log.info(s"Recovered: $message") // We don't focus here
    }
  }

  class EventAggregator extends Actor with ActorLogging {

    override def receive: Receive = {
      case message => log.info(s"$message")
    }
  }

  val system = ActorSystem("PersistAsyncDemo")
  val eventAggregator = system.actorOf(Props[EventAggregator], "EventAggregator")
  val streamProcessor = system.actorOf(CriticalStreamProcessor.props(eventAggregator), "StreamProcessor")

  streamProcessor ! Command("command1")
  streamProcessor ! Command("command2")
}
