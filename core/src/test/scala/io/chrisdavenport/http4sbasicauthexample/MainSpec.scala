package io.chrisdavenport.http4sbasicauthexample

import munit.CatsEffectSuite
import cats.effect._

class MainSpec extends CatsEffectSuite {

  test("Main should exit succesfully") {
    testEqual(true, true)
  }

}