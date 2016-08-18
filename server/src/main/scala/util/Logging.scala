package util

import play.api.Logger

trait Logging {

  val logger = Logger("kidstravel." + this.getClass.getName)

}
