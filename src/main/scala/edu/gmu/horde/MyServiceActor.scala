package edu.gmu.horde

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Akka Horde</h1>
                <a href="/api/start">Start</a>
                <a href="/api/train">Train</a>
                <a href="/api/run">Run</a>
              </body>
            </html>
          }
        }
      }
    }
    path("api") {
      path("start") {


        // JSONP support
//        jsonpWithParameter("callback") {
//          // use in-scope marshaller to create completer function
//          produce(instanceOf[Order]) { completer => ctx =>
//            processOrderRequest(orderId, completer)
//          }
//        }
      }
    }
}