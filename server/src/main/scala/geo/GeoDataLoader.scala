package geo

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, SinkQueue, Source}
import cats._
import cats.std.all._
import cats.data.Validated.{Invalid, Valid}
import ch.becompany.akka.io.csv.{CsvReader, CsvSpec}
import ch.becompany.akka.io.file.ResourceReader
import kidstravel.shared.geo.{City, Country}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import util.Logging

import collection.JavaConverters._
import collection.immutable.Queue
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

case class CountryRecord(
  code2: String, //ISO
  code3: String, // ISO3
  codeNumeric: Option[Int], // ISO-Numeric
  fips: String, // fips
  name: String, // Country
  capital: String, // Capital
  area: Option[Double], // Area(in sq km)
  population: Option[Int], // Population
  continent: String, // Continent
  tld: String, // tld
  currencyCode: String, // CurrencyCode
  currencyName: String, // CurrencyName
  phone: String, // Phone
  postalCodeFormat: String, // Postal Code Format
  postalCodeRegex: String, // Postal Code Regex
  languages: String, // Languages
  geonameId: Long, // geonameid
  neighbours: String, // neighbours
  equivalentFipsCode: String // EquivalentFipsCode
)

case class GeoName(
  geonameId: Long, // integer id of record in geonames database
  name: String, // name of geographical point (utf8) varchar(200)
  asciiName: String, // name of geographical point in plain ascii characters, varchar(200)
  alternateNames: String, // alternatenames, comma separated, ascii names automatically transliterated, convenience attribute from alternatename table, varchar(10000)
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

  val csvSpec = CsvSpec(fieldDelimiter = '\t')
  private val countryReader = new CsvReader[CountryRecord](csvSpec)
  private val cityReader = new CsvReader[GeoName](csvSpec)

  private def importCountry(record: CountryRecord): DBIOAction[Int, NoStream, Effect.All] =
    countries += Country(record.code2, record.name)

  private def importCity(geoName: GeoName): DBIOAction[Int, NoStream, Effect.All] =
    cities.map(c => (c.name, c.countryCode, c.population)) +=
      (geoName.name, geoName.countryCode, geoName.population)

  private def toDbioAction[S](queue: SinkQueue[DBIOAction[S, NoStream, Effect.All]]):
      DBIOAction[Queue[S], NoStream, Effect.All] =
    DBIO.from(queue.pull() map {
      case Some(action) => action.flatMap(s => toDbioAction(queue).map(_ :+ s))
      case None => DBIO.successful(Queue())
    }).flatMap(r => r)

  private def importFile[T](file: String, csvReader: CsvReader[T], importer: T => DBIOAction[Int, NoStream, Effect.All]):
      Source[DBIOAction[Int, NoStream, Effect.All], _] =
    csvReader.read(ResourceReader.read(file, encoding = Some("UTF-8"))).
      //map(record => { logger.debug(s"Importing $record"); record }).
      map(_.bimap(
        errors => { logger.error(errors.foldLeft("")(_ + "; " + _)); errors },
        t => t //{ logger.info(s"Importing $t".take(80)); t }
      )).
      collect { case Valid(t) => t}.
      map(importer)

  def importGeoData(): Unit = {
    println(s"Importing geo data.")

    val countryActions = importFile("/geonames/countryInfo.txt", countryReader, importCountry)
    val cityActions = importFile("/geonames/cities1000.txt", cityReader, importCity)

    val future = db.run((for {
      _ <- toDbioAction(countryActions.runWith(Sink.queue()))
      _ <- toDbioAction(cityActions.runWith(Sink.queue()))
    } yield {}).transactionally)
    Await.result(future, 10.minutes)
  }

  //importGeoData()

}
