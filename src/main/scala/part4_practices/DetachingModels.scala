package part4_practices

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor
import akka.persistence.journal.{EventAdapter, EventSeq}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable

object DetachingModels extends App {

  import DomainModel._

  class CouponManager extends PersistentActor with ActorLogging {

    val coupons: mutable.Map[String, User] = new mutable.HashMap[String, User]()

    override def persistenceId: String = "coupon-manager"

    override def receiveCommand: Receive = {
      case ApplyCoupon(coupon, user) =>
        if(!coupons.contains(coupon.code)) {
          persist(CouponApplied(coupon.code, user)) { e =>
            log.info(s"Persisted: $e")
            coupons.put(coupon.code, user)
          }
        }
    }

    override def receiveRecover: Receive = {
      case event @ CouponApplied(code, user) =>
        log.info(s"Recovered: $event")
        coupons.put(code, user)
    }
  }

  val system = ActorSystem("DetachingModels", ConfigFactory.load.getConfig("detachingModels"))
  val couponManager = system.actorOf(Props[CouponManager], "couponManager")

  /*
  for {
    i <- 10 to 15
  } yield {
    val coupon = Coupon(s"MEGA COUPON $i", 100)
    val user = User(s"user$i", s"user_$i@rtjvm.com", s"username_$i")
    couponManager ! ApplyCoupon(coupon, user)
  }
*/

}

object DomainModel {
  // Data Structure
  case class User(id: String, email: String, name: String)
  case class Coupon(code: String, promotionAmount: Int)

  // Commands
  case class ApplyCoupon(coupon: Coupon, user: User)

  // Events
  case class CouponApplied(code: String, user: User)
}

object DataModel {
  case class WrittenCouponApplied(code: String, userId: String, userEmail: String)
  case class WrittenCouponAppliedV2(code: String, userId: String, userEmail: String, username: String)

}

class ModelAdapter extends EventAdapter {

  import DomainModel._
  import DataModel._

  override def manifest(event: Any): String = "coupon-model-adapter" // we don't use it

  // journal -> serializer -> fromJournal -> Actor
  override def fromJournal(event: Any, manifest: String): EventSeq = event match {
    case event @ WrittenCouponApplied(code, userId, userEmail) =>
      println(s"Converting $event to DomainModel")
      EventSeq.single(CouponApplied(code, User(userId, userEmail, "")))

    case event @ WrittenCouponAppliedV2(code, userId, userEmail, username) =>
      println(s"Converting $event to DomainModel")
      EventSeq.single(CouponApplied(code, User(userId, userEmail, username)))

    case other => EventSeq.single(other)
  }
  // actor -> toJournal -> serializer -> journal
  override def toJournal(event: Any): Any = event match {
    case event @ CouponApplied(code: String, user: User) =>
      println(s"Converting $event to Data Model")
      WrittenCouponAppliedV2(code, user.id, user.email, user.name)
  }

}
