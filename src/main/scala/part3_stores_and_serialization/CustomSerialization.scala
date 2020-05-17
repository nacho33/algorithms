package part3_stores_and_serialization

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor
import akka.serialization.Serializer
import com.typesafe.config.ConfigFactory

//Commands
case class RegisterUser(email: String, name: String)
// Events
case class UserRegistered(id: Int, email: String, name: String)

// Serializer
class UserRegistrationSerializer extends Serializer {

  val SEPARATOR = "//"
  override def identifier: Int = 13232

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case event @ UserRegistered(id, email, name) =>
      println(s"Serializing: $event")
      s"[$id$SEPARATOR$email$SEPARATOR$name]".getBytes()
    case _ =>
      throw new IllegalArgumentException("Only user registration events supported in this serializer")
  }

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = {
    val string = new String(bytes)
    val values = string.substring(1, string.length - 1).split(SEPARATOR)
    val result = UserRegistered(values(0).toInt, values(1), values(2))
    println(s"Deserializing: $string to $result")
    result
  }


  // false => manifest is None
  // true => the manifest is used to instantiate the proper class via refletion
  //         the manifes will pass with some class
  override def includeManifest: Boolean = false

}

class UserRegistrationActor extends PersistentActor with ActorLogging {

  override def persistenceId: String = "user-registration"

  var currentId = 0

  override def receiveCommand: Receive = {
    case RegisterUser(email, name) =>
      persist(UserRegistered(currentId, email, name)) { e =>
        currentId += 1
        log.info(s"Persisting: $e")
      }
  }

  override def receiveRecover: Receive = {
    case event @ UserRegistered(id, _, _) =>
      log.info(s"Recovered: $event")
      currentId = id
  }
}

object CustomSerialization extends App {
  /*
   * send command to the actor
   * actor calls persist
   * serializer serializes the events into bytes
   * journal writes the bytes
   *
   * journal sends the bytes back to the actor
   * the serializer call from binary method, turn the array of bytes into the actual object
   * than the actor expect in receive recover
   */
  val system = ActorSystem("CustomSerialization", ConfigFactory.load().getConfig("customSerializerDemo"))
  val userRegistrationActor = system.actorOf(Props[UserRegistrationActor], "UserRegistration")

 // (1 to 10).foreach(i => userRegistrationActor ! RegisterUser(s"user_$i@rtjvm.com", s"user$i"))

}
