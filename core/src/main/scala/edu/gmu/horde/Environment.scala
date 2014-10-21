package edu.gmu.horde

import akka.actor._
import akka.event.EventBus
import edu.gmu.horde.zerg.{Publish, Unsubscribe, Subscribe, UnitUpdate}
import org.slf4j.LoggerFactory

object Environment {
  val log = LoggerFactory.getLogger(Environment.getClass())
}

final case class MsgEnvelope(payload: UnitUpdate)

class Environment extends Actor with ActorLogging {
  import Environment.log
  import akka.event.LookupClassification

  /**
   * Publishes the payload of the MsgEnvelope when the topic of the
   * MsgEnvelope starts with the String specified when subscribing.
   */
  class UpdateEventBus extends EventBus with LookupClassification {
    type Event = MsgEnvelope
    type Classifier = Integer
    type Subscriber = ActorRef

    // is used for extracting the classifier from the incoming events
    override protected def classify(event: Event): Classifier = event.payload.id

    // will be invoked for each event for all subscribers which registered themselves
    // for the event’s classifier
    override protected def publish(event: Event, subscriber: Subscriber): Unit = {
      subscriber ! event.payload
    }

    // must define a full order over the subscribers, expressed as expected from
    // `java.lang.Comparable.compare`
    override protected def compareSubscribers(a: Subscriber, b: Subscriber): Int =
      a.compareTo(b)

    // determines the initial size of the index data structure
    // used internally (i.e. the expected number of different classifiers)
    override protected def mapSize: Int = 128
  }

  var root: ActorRef = null
  val eventBus = new UpdateEventBus()

  override def receive: Actor.Receive = {
    case SetRoot(r: ActorRef) =>
      root = r
    case Subscribe(id: Int, ref: ActorRef) =>
      eventBus.subscribe(ref, id)
    case Unsubscribe(ref: ActorRef, id: Option[Int]) =>
      id match {
        case Some(id) => eventBus.unsubscribe(ref, id)
        case None => eventBus.unsubscribe(ref)
      }
    case Publish(u: UnitUpdate) =>
      eventBus.publish(MsgEnvelope(u))
  }
}