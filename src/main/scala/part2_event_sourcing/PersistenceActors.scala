package part2_event_sourcing

import java.util.Date

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor

object PersistenceActors extends App {
  /*
   * We have a business which keeps track of our invoices
   */

  // Command
  case class Invoice(recipient: String, date: Date, amount: Int)
  case class InvoiceBulk(invoices: List[Invoice])

  // Special commands
  case object Shutdown
  // Events
  case class InvoiceRecorded(id: Int, recipient: String, date: Date, amount: Int)


  class Accountant extends PersistentActor with ActorLogging {

    var latestInvoiceId = 0
    var totalAmount = 0

    override def persistenceId: String = "simple-accountant" // unique

    // The normal receive method
    override def receiveCommand: Receive = {
      case Invoice(recipient, date, amount) =>
        /*
         * When you receive a command
         * 1) create an event to persist into the store
         * 2) persist the event, the pass in a callback that will triggered once the event is written
         * 3) we update the actor state when the event has persisted
         */
        log.info(s"Receive invoice for amount: $amount")
        persist(InvoiceRecorded(latestInvoiceId, recipient, date, amount))
          /* Time gap: all other messages to this actor are stashed */
        { e => // 2)
          // Safe to access mutable state here, we don't have race conditions - EXCEPTION, no deberíamos acceder a variables mutables desde un punto fuera del actor
          // Akka persistence asegura que no hay otro accediendo al callback al
          // mismo tiempo, aunque se ejecute en un punto en el futuro
          // Update the state
          latestInvoiceId += 1                                                    // 3)
          totalAmount += amount
          // sender ! "PersistentACK" // It will correctly identify the sender

          log.info(s"Persisted $e as invoice #${e.id}, for total amount $totalAmount" )

        }

      case InvoiceBulk(invoices) =>
        // 1) Create all events
        // 2) Persist all events
        // 3) Update state when each event is persisted

        val invoiceIds = latestInvoiceId to (latestInvoiceId + invoices.size)
        val events = invoices.zip(invoiceIds).map{ pair =>
          val id = pair._2
          val invoice = pair._1
          InvoiceRecorded(id, invoice.recipient, invoice.date, invoice.amount)

        }
        // persistAll persists all events in sequence, we receive a callback for each event
        persistAll(events) { e =>
          latestInvoiceId += 1                                                    // 3)
          totalAmount += e.amount
          log.info(s"Persisted SINGLE $e as invoice #${e.id}, for total amount $totalAmount" )

        }

      case  Shutdown => context.stop(self)
      // Act as a normal actor, we don't need to persist events if we don't want
      case "print" =>
        log.info(s"Latest invoice #${latestInvoiceId}, for total amount $totalAmount" )

    }

    // Handle that will be called on recovery
    override def receiveRecover: Receive = {
      /*
       * best practice: follow the logic in the persist steps of the receiveCommand
       */
      case InvoiceRecorded(id, _, _, amount) =>
        latestInvoiceId += id                                                    // 3)
        totalAmount += amount
        log.info(s"Recovered invoice #$id for amount $amount, totalAmount: $totalAmount")

    }
    /*
     * This method will be call when persisting fails
     * the actor will be STOPPED
     *
     * Best practice: Start the actor again after a while,
     * Use Backoff supervisor. Really difficult to see
     */
    override def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
      log.error(s"Failure to persist $event because of $cause")
      super.onPersistFailure(cause, event, seqNr)
    }

    /*
     * This method is called if the journal throws an exception when persisting an event
     * The actor is RESUMED(you now for sure the event is not persisted).
     *  Difficult to reproduced, journal is really robusted.
     *
     */
    override def onPersistRejected(cause: Throwable, event: Any, seqNr: Long): Unit = {
      log.error(s"Persist rejected for $event because of $cause")
      super.onPersistRejected(cause, event, seqNr)
    }
  }

  val system = ActorSystem("PersistentActors")
  val accountant = system.actorOf(Props[Accountant], "simpleAccountant")

  //(1 to 10).foreach(i => accountant ! Invoice("The sofa company", new Date, i * 1000))


  /*
   * Persistence failures
   * 1) When persist an event => call onPersistFailure
   * 2)
   */

  /*
   * Persisting multiple events (multiple InvoiceRecorded events at the same time)
   *
   * use persistAll
   */

  val invoices = (1 to 5).map( i =>
    Invoice("The awesome chairs", new Date, i * 2000)
  )
  //accountant ! InvoiceBulk(invoices.toList)

  /*
   * Never ever call persist or persisAll from futures
   */

  /*
   * Shutdown of persisting actors
   * We can't tell PoisonPill, that way the messages will go to dead letters
   *
   * Best practice: Define your own shutdown message, this way the messages will go to
   * the normal mailbox. This way the Shutdown message will be process after the rest
   * of messages are processed
   */
  accountant ! Shutdown // Safety shutdown, all messages have been correctly persisted
}
