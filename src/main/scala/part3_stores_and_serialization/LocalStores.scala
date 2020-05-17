package part3_stores_and_serialization

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object LocalStores extends App {

  val localStoresActorSystem = ActorSystem("localStoresActorSystem", ConfigFactory.load().getConfig("localStores"))
  val persistentActor = localStoresActorSystem.actorOf(Props[SimplePersistActor], "simplePersistentActor")

  (1 to 10).foreach(i => persistentActor ! s"I love Akka [$i]")

  persistentActor ! "print"
  persistentActor ! "snap"

  (11 to 20).foreach(i => persistentActor ! s"I love Akka [$i]")
}
