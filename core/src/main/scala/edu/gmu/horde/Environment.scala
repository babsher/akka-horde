package edu.gmu.horde

import akka.actor._
import akka.actor.Actor.Receive
import akka.util.Subclassification
import akka.event.EventBus
import org.slf4j.LoggerFactory

object Environment {
  val log = LoggerFactory.getLogger(Environment.getClass())
}

class Environment extends Actor with ActorLogging {

  import Environment.log
  import akka.util.Subclassification

  class StartsWithSubclassification extends Subclassification[String] {
    override def isEqual(x: String, y: String): Boolean =
      x == y

    override def isSubclass(x: String, y: String): Boolean =
      x.startsWith(y)
  }

  import akka.event.SubchannelClassification

  /**
   * Publishes the payload of the MsgEnvelope when the topic of the
   * MsgEnvelope starts with the String specified when subscribing.
   */
  class SubchannelBusImpl extends EventBus with SubchannelClassification {
    type Event = MsgEnvelope
    type Classifier = String
    type Subscriber = ActorRef

    // Subclassification is an object providing `isEqual` and `isSubclass`
    // to be consumed by the other methods of this classifier
    override protected val subclassification: Subclassification[Classifier] =
      new StartsWithSubclassification

    // is used for extracting the classifier from the incoming events
    override protected def classify(event: Event): Classifier = event.topic

    // will be invoked for each event for all subscribers which registered
    // themselves for the event’s classifier
    override protected def publish(event: Event, subscriber: Subscriber): Unit = {
      subscriber ! event.payload
    }
  }

  var root: ActorRef = null

  override def receive: Actor.Receive = {
    case SetRoot(r: ActorRef) =>
      root = r
  }
}