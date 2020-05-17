package part2_event_sourcing

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.persistence.PersistentActor

object MultiplePersists extends App {

  /*
   * Diligent accountant: with every Invoice we persist two events
   *  - a tax record for the fiscal authority
   *  - an invoice for personal logs or some auditory authority
   */

  case class Invoice(recipient: String, date: Date, amount: Int)

  case class TaxRecord(taxId: String, recordId: Int, date: Date, amount: Int)
  case class InvoiceRecord(invoiceRecordId: Int, recipient: String, date: Date, amount: Int)

  object DiligentAccountant {
    def props(taxId: String, taxAuthority: ActorRef) = Props(new DiligentAccountant(taxId, taxAuthority))
  }

  class DiligentAccountant(taxId: String, taxAuthority: ActorRef)
    extends PersistentActor
      with ActorLogging {

    var latestTaxRecordId = 0
    var latestInvoiceRecordId = 0
    override def persistenceId: String = "diligent-accountant"

    override def receiveCommand: Receive = {
      case Invoice(recipient, date, amount) =>
        // The message ordering is guaranteed
        // persistence is also based in messages
        // journal ! taxRecord, not literally but the idea
        persist(TaxRecord(taxId, latestTaxRecordId, date, amount/3)) { taxRecord =>
          taxAuthority ! taxRecord // 1st
          latestTaxRecordId += 1

          persist("I hereby declare this tax record to be true and complete.") { declaration =>
            taxAuthority ! declaration  // 3rd
          }
        }
        // journal ! invoiceRecord, not literally but the idea
        persist(InvoiceRecord(latestInvoiceRecordId, recipient, date, amount)) { invoiceRecord =>
          taxAuthority ! invoiceRecord // 2nd
          latestInvoiceRecordId += 1

          persist("I hereby declare this invoice record to be true and complete.") { declaration =>
            taxAuthority ! declaration //4th
          }
        }


    }

    override def receiveRecover: Receive = {
      case event => // not focused in this lesson
        log.info(s"Recovered: $event")
    }
  }

  class TaxAuthority extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(s"Received: $message")
    }
  }
  val system = ActorSystem("diligentAccountant")
  val taxAuthority = system.actorOf(Props[TaxAuthority], "HMRC")
  val accountant = system.actorOf(DiligentAccountant.props("UK123_123", taxAuthority))

  accountant ! Invoice("The sofa company", new Date(), 2000)  // 1st all of it
  accountant ! Invoice("The superCar company", new Date(), 50003) // 2nd

  // nested persisting
}
