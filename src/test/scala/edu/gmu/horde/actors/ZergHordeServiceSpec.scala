package edu.gmu.horde.actors

import akka.actor.{Actor, ActorRef}
import akka.event.NoLogging
import akka.http.model.StatusCodes._
import akka.http.model.ContentTypes._
import akka.http.model._
import akka.http.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Flow
import akka.testkit.{TestActorRef, TestProbe}
import edu.gmu.horde.http.ZergHordeService
import edu.gmu.horde.{RequestAgentInfo, AgentInfo, AgentsSummary}
import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent.duration._

/**
 * http://sysgears.com/articles/scala-rest-api-integration-testing-with-spray-testkit/
 */
class ZergHordeServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with ZergHordeService {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  val agentInfo = AgentInfo("test", "") :: Nil
  val agentSummary = AgentsSummary(agentInfo)
  override val horde: ActorRef = TestActorRef(new Actor {
    def receive = {
      case RequestAgentInfo(null) => sender() ! agentSummary
    }
  })

  override lazy val zergConnectionFlow: Flow[HttpRequest, HttpResponse] = Flow[HttpRequest].map { request =>
    if (request.uri.toString endsWith "api/agents") {
      HttpResponse(status = OK)
    } else {
      HttpResponse(status = BadRequest, entity = marshal("Bad ip format"))
    }
  }

  "Zerg Service" should "respond to GET / with index.html" in {
    Get(s"/") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`)
      responseAs[String].length should be > 330
    }    
    Get(s"/index.html") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`)
      responseAs[String].length should be > 330
    }
  }
  
  "Zerg Service" should "respond to GET /api/agents with AgentSummary" in {
    
    Get(s"/api/agents") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[AgentsSummary] shouldBe agentSummary
    }
  }
}
