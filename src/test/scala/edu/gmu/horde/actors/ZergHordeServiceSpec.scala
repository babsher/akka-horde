package edu.gmu.horde.actors

import akka.event.NoLogging
import akka.http.model.StatusCodes._
import akka.http.model.{HttpRequest, HttpResponse}
import akka.http.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Flow
import edu.gmu.horde.{AgentInfo, AgentSummary}
import org.scalatest.{FlatSpec, Matchers}
import spray.testkit.ScalatestRouteTest

/**
 * http://sysgears.com/articles/scala-rest-api-integration-testing-with-spray-testkit/
 */
class ZergHordeServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with ZergHordeService {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging
  
  val agentInfo = AgentInfo("test", "") :: Nil
  val agentSummary = AgentSummary(agentInfo)

  override lazy val connectionFlow = Flow[HttpRequest].map { request =>
    if (request.uri.toString endsWith "api/agents") {
      HttpResponse(status = OK)
    }
  }

  "Zerg Service" should "respond to Request Agent Summary" in {
    
    
  }
}
