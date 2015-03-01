package edu.gmu.horde.http

import akka.http.marshallers.sprayjson.SprayJsonSupport
import edu.gmu.horde._
import edu.gmu.horde.storage.{AttributeValue, DoubleValue, StringValue}
import spray.json._

trait Protocols extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val agentInfoFormat = jsonFormat4(AgentInfo.apply)
  implicit val requestAgentsFormat = jsonFormat1(AgentsSummary.apply)
  implicit val trainFormat = jsonFormat1(Train.apply)
  implicit val stateFormat = jsonFormat1(State.apply)
  implicit object attributeValueFormat extends RootJsonFormat[AttributeValue] {
    override def read(json: JsValue) = json match {
      case JsNumber(num) =>
        DoubleValue(num.toDouble)
      case JsString(str) =>
        StringValue(str)
      case _ => throw new DeserializationException("Attribute value expected")
    }

    override def write(obj: AttributeValue) = obj match {
      case DoubleValue(num) => JsNumber(num)
      case StringValue(str) => JsString(str)
    }
  }
  implicit val agentPossibleStatesFormat = jsonFormat3(AgentPossibleStates.apply)
  implicit val agentDetailFormat = jsonFormat5(AgentDetail.apply)
  implicit val runFormat = jsonFormat1(Run.apply)
  implicit val hordeStateFormat = jsonFormat3(HordeState.apply)
}
