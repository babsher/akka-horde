package edu.gmu.horde

import akka.io.IO
import spray.http.HttpHeaders.Location

import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import spray.can.Http
import spray.can.server.Stats
import spray.util._
import spray.http._
import HttpMethods._
import MediaTypes._
import spray.can.Http.RegisterChunkHandler
import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import akka.util.Timeout
import spray.can.Http
import spray.routing._
import spray.http._
import MediaTypes._

import scala.concurrent.duration._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with ActorLogging {

  var horde :ActorRef = createHorde()

  def receive = {
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      sender ! index

    case HttpRequest(GET, Uri.Path("/api/start"), _, _, _) =>
      horde ! Scenario("demo")
      sender ! HttpResponse(entity = "Started")
      // TODO add redirect to index
    case HttpRequest(GET, Uri.Path("/api/run"), _, _, _) =>
      horde ! Run(true, false)
      sender ! redirect("/", "Running")
    case HttpRequest(GET, Uri.Path("/api/train"), _, _, _) =>
      horde ! Run(true, true)
      sender ! redirect("/", "Training")
    case HttpRequest(GET, Uri.Path("/api/stop"), _, _, _) =>
      horde ! Stop
      sender ! redirect("/", "Stopping")
    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      sender ! HttpResponse(entity = "PONG!")

    case _: HttpRequest => sender ! HttpResponse(status = 404, entity = "Unknown resource!")

    case Timedout(HttpRequest(method, uri, _, _, _)) =>
      sender ! HttpResponse(
        status = 500,
        entity = "The " + method + " request to '" + uri + "' has timed out..."
      )
  }

  ////////////// helpers //////////////

  def redirect(uri :String, text :String) = {
    HttpResponse(
      status = StatusCodes.PermanentRedirect,
      headers = Location(uri) :: Nil,
      entity = HttpEntity(`text/html`, <html>
        <body>
          <h1>{text}</h1>
        </body>
      </html>.toString()))
  }

  lazy val index = HttpResponse(
    entity = HttpEntity(`text/html`,
      <html>
        <body>
          <h1><i>Akka Horde</i>!</h1>
          <p>Defined resources:</p>
          <ul>
            <li><a href="/ping">ping</a></li>
            <li><hr></hr></li>
            <li><a href="/api/start">Start</a></li>
            <li><a href="/api/run">Run</a></li>
            <li><a href="/api/train">Train</a></li>
            <li><hr></hr></li>
            <li><a href="/server-stats">server-stats</a></li>
            <li><hr></hr></li>
            <li><a href="/api/stop">Stop</a></li>
          </ul>
        </body>
      </html>.toString()
    )
  )

  def statsPresentation(s: Stats) = HttpResponse(
    entity = HttpEntity(`text/html`,
      <html>
        <body>
          <h1>HttpServer Stats</h1>
          <table>
            <tr><td>uptime:</td><td>{s.uptime.formatHMS}</td></tr>
            <tr><td>totalRequests:</td><td>{s.totalRequests}</td></tr>
            <tr><td>openRequests:</td><td>{s.openRequests}</td></tr>
            <tr><td>maxOpenRequests:</td><td>{s.maxOpenRequests}</td></tr>
            <tr><td>totalConnections:</td><td>{s.totalConnections}</td></tr>
            <tr><td>openConnections:</td><td>{s.openConnections}</td></tr>
            <tr><td>maxOpenConnections:</td><td>{s.maxOpenConnections}</td></tr>
            <tr><td>requestTimeouts:</td><td>{s.requestTimeouts}</td></tr>
          </table>
        </body>
      </html>.toString()
    )
  )

  def createHorde(): ActorRef = {
    context.actorOf(Props[HordeFSM])
  }
}


