package part4_practices

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor
import akka.persistence.journal.{EventSeq, ReadEventAdapter}
import com.typesafe.config.{ConfigBeanFactory, ConfigFactory}

import scala.collection.mutable

object EventAdapters extends App {

  // online store for acoustic guitars

  val ACOUSTIC = "acoustic"
  val ELECTRIC = "electric"
  // DataStructures
  case class Guitar(id: String, model: String, make: String, guitarType: String = ACOUSTIC)
  //Commands
  case class AddGuitar(guitar: Guitar, quantity: Int)
  //Events
  case class GuitarAdded(guitarId: String, guitarModel: String, guitarMake: String, quantity: Int)
  // Solution 1: 2 types of events
  case class GuitarAddedV2(guitarId: String, guitarModel: String, guitarMake: String, quantity: Int, guitarType: String)

  class InventoryManager extends PersistentActor with ActorLogging {

    override def persistenceId: String = "guitar-inventory-manager"

    val inventory: mutable.Map[Guitar, Int] = new mutable.HashMap[Guitar, Int]()

    override def receiveCommand: Receive = {
      case AddGuitar(guitar @ Guitar(id, model, make, guitarType), quantity) =>
        persist(GuitarAddedV2(id, model, make, quantity, guitarType)) { _ =>
          addGuitarInventory(guitar, quantity)
          log.info(s"Added $quantity x $guitar to inventory")
        }
      case "print" =>
        log.info(s"Current Inventory: $inventory")
    }

    override def receiveRecover: Receive = {

      // Solution 1: 2 types of events
      //case event @ GuitarAdded(id, model, make, quantity) =>
      //  log.info(s"Recovered: $event")
      //  val guitar = Guitar(id, model, make)
      //  addGuitarInventory(guitar, quantity)

      case event @ GuitarAddedV2(id, model, make, quantity, guitarType) =>
        log.info(s"Recovered: $event")
        val guitar = Guitar(id, model, make, guitarType)
        addGuitarInventory(guitar, quantity)
    }

    def addGuitarInventory(guitar: Guitar, quantity: Int) = {
      val existingQuantity = inventory.getOrElse(guitar, 0)
      inventory.put(guitar, existingQuantity + quantity)
    }

  }

  // Solution 2: Add a ReadEventAdapter and adding it in conf file
  class GuitarReadEventAdapter extends ReadEventAdapter {
    /*
     * when the journal sends a message to the actor
     * journal -> string of bytes -> serializer
     * serializer -> deserializado -> read event adapter
     * read event adapter -> evento que el actor entiende -> actor
     */
    // puede hacer split de eventos
    override def fromJournal(event: Any, manifest: String): EventSeq = event match {
      case GuitarAdded(id, model, make, quantity) =>
        EventSeq.single(GuitarAddedV2(id, model, make, quantity, ACOUSTIC))
      case other => EventSeq.single(other)
    }
  }

  // WriteEventAdapter -> use for backwards compatibility
  // Actor -> writeEventAdapter -> serializer -> journal
  // tienen el método toJournal
  // Si queremos aplicar las dos posibilidades, podemos extender de
  // EventAdapter y definiremos los 2 métodos

  val system = ActorSystem("eventAdapters", ConfigFactory.load().getConfig("eventAdapters"))
  val inventoryManager = system.actorOf(Props[InventoryManager], "inventoryManager")

  val guitars = (1 to 10).map(i => Guitar(i.toString, s"Hakker $i", "rtjvm"))
  //guitars.foreach(guitar => inventoryManager ! AddGuitar(guitar, 5))

  inventoryManager ! "print"
}
