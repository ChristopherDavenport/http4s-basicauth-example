package io.chrisdavenport.http4sbasicauthexample

import cats._
import cats.syntax.all._
import cats.effect._
import org.http4s._
import org.http4s.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.authentication.BasicAuth


object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    Server.server[IO].use(_ => IO.never).as(ExitCode.Success)
  }

}

object Server {
  val allowedUsers = Map("chris" -> "foo")

  def server[F[_]: Concurrent: Timer: ContextShift]: Resource[F, Unit] = {
    for {
      _ <- EmberServerBuilder.default[F]
        .withHttpApp(routes.orNotFound)
        .build
    } yield ()
  }

  // Here we compose AuthMiddleware(how we get a user)
  // and AuthedRoutes(what to do when we have a user) and generate a set of responses
  // including what to do when they are not a valid user.
  def routes[F[_]: Sync]: HttpRoutes[F] = authMiddleware(allowedUsers)(Sync[F])(authedRoutes[F])

  // If User is in Static Map then Allow to operate, 
  // otherwise return auth challenge. 
  // Can be any method of validation you want. This was just simple to demonstrate.
  def authMiddleware[F[_]: Sync](staticValid: Map[String, String]): AuthMiddleware[F, String] = BasicAuth.apply("localhost:8080", {(bc: BasicCredentials) => 
    staticValid.get(bc.username).flatMap(pass => Alternative[Option].guard(pass === bc.password)).as(bc.username).pure[F]
  })


  // What to do when you already have a user as validated by an authMiddleware
  def authedRoutes[F[_]: Applicative: Defer]: AuthedRoutes[String, F] = {
    val dsl = new Http4sDsl[F]{}; import dsl._

    AuthedRoutes.of[String, F]{
      case (GET -> Root) as who => Ok(s"Hello there $who") 
    }
  }
  

}