package geo

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, SinkQueue}
import cats._
import cats.std.all._
import cats.data.Validated.{Invalid, Valid}
import ch.becompany.akka.io.csv.{CsvReader, CsvSpec}
import ch.becompany.akka.io.file.ResourceReader
import kidstravel.shared.geo.City
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import util.Logging

import collection.JavaConverters._
import collection.immutable.Queue
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

case class GeoName(
  geonameid: Long, // integer id of record in geonames database
  name: String, // name of geographical point (utf8) varchar(200)
  asciiname: String, // name of geographical point in plain ascii characters, varchar(200)
  alternatenames: String, // alternatenames, comma separated, ascii names automatically transliterated, convenience attribute from alternatename table, varchar(10000)
  latitude: String, // latitude in decimal degrees (wgs84)
  longitude: String, // longitude in decimal degrees (wgs84)
  featureClass: String, // see http://www.geonames.org/export/codes.html, char(1)
  featureCode: String, // see http://www.geonames.org/export/codes.html, varchar(10)
  countryCode: String, // ISO-3166 2-letter country code, 2 characters
  cc2: String, // alternate country codes, comma separated, ISO-3166 2-letter country code, 200 characters
  admin1Code: String, // fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
  admin2Code: String, // code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)
  admin3Code: String, // code for third level administrative division, varchar(20)
  admin4Code: String, // code for fourth level administrative division, varchar(20)
  population: Option[Long], // bigint (8 byte int)
  elevation: Option[Int], // in meters, integer
  dem: String, // digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
  timezone: String, // the timezone id (see file timeZone.txt) varchar(40)
  modificationDate: String // date of last modification in yyyy-MM-dd format
)

class GeoDataLoader @Inject()(dbConfigProvider: DatabaseConfigProvider)(
    implicit system: ActorSystem, ec: ExecutionContext)
  extends Logging {

  import dao.Schema._
  import ch.becompany.akka.io.csv.Parsers._

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig.driver.api._
  private val db = dbConfig.db

  private implicit val materializer = ActorMaterializer()

  private val csvReader = new CsvReader[GeoName](CsvSpec(fieldDelimiter = '\t'))

  private def importCity(geoName: GeoName): DBIOAction[Int, NoStream, Effect.All] = {
    val city = City(-1, geoName.name, geoName.countryCode, None)
    cities += city
  }

  private def toDbioAction[S](queue: SinkQueue[DBIOAction[S, NoStream, Effect.All]]): DBIOAction[Queue[S], NoStream, Effect.All] =
    DBIO.from(queue.pull() map {
      case Some(action) => action.flatMap(s => toDbioAction(queue).map(_ :+ s))
      case None => DBIO.successful(Queue())
    }).flatMap(r => r)


  def importGeoData(): Unit = {
    println(s"Importing geo data.")

    val dbActions = csvReader.read(ResourceReader.read("/geonames/cities1000.txt")).
      map(record => { logger.debug(s"Importing $record"); record }).
      map(_.bimap(
        errors => { logger.error(errors.foldLeft("")(_ + "; " + _)); errors },
        t => { logger.info(s"Importing $t".take(80)); t }
      )).
      collect { case Valid(t) => t}.
      map(importCity)

    val future = db.run(toDbioAction(dbActions.runWith(Sink.queue())).transactionally)
    Await.result(future, 10.minutes)
  }

  importGeoData()

}
