package geo

import org.scalatestplus.play.PlaySpec
import play.api.{Application, Configuration, Mode, Play}
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits._

class GeoDataLoaderSpec extends PlaySpec {

  val app = new GuiceApplicationBuilder()
    .configure(
      Map(
        "slick.dbs.default.driver" -> "slick.driver.H2Driver$",
        "slick.dbs.default.db.driver" -> "org.h2.Driver",
        "slick.dbs.default.db.url" -> "jdbc:h2:target/db"
      )
    )
    .in(Mode.Test)
    .build()

  "GeoDataLoader" should {
    "import geo data" in {

      val loader = Application.
        instanceCache[GeoDataLoader].
        apply(app).
        importGeoData()
    }
  }


}
