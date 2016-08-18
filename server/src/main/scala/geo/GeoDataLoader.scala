package geo

import javax.inject.Inject

import akka.stream.scaladsl.{FileIO, Sink, SinkQueue, Source}
import ch.becompany.akka.io.csv.CsvReader
import ch.becompany.akka.io.file.ResourceReader
import org.geonames.{ToponymSearchCriteria, ToponymSearchResult, WebService}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}
import slick.driver.JdbcProfile
import util.Logging

import collection.JavaConverters._
import collection.immutable.Queue

class GeoDataLoader @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider,
    protected val config: play.api.Configuration)
  extends Logging {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db

  case class GeoName()

  val csvReader = new CsvReader[GeoName]

  def importCity(geoName: GeoName) = {

  }

  def toDbioAction[S](queue: SinkQueue[DBIOAction[S, NoStream, Effect.All]]): DBIOAction[Queue[S], NoStream, Effect.All] =
    DBIO.from(queue.pull() map { tOption =>
      tOption match {
        case Some(action) => action.flatMap(s => toDbioAction(queue).map(_ :+ s))
        case None => DBIO.successful(Queue())
      }
    }).flatMap(r => r)


  def importGeoData(): Unit = {
    println(s"Importing geo data.")

    val dbActions = csvReader.read(ResourceReader.read("/geonames/cities1000.txt")).
      map(record => { logger.info(s"Importing $record"); record }).
      map()

    db.run(toDbioAction(dbActions.runWith(Sink.queue())).transactionally)
  }

  importGeoData()

}
