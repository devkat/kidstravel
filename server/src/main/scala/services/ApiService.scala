package services

import java.util.{Date, UUID}
import javax.inject.Inject

import dao.Schema
import geo.GeoDataLoader
import kidstravel.shared.Api
import kidstravel.shared.geo.{City, CityLabel, Country}
import kidstravel.shared.poi.Poi
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.Future

class ApiService @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider,
    protected val geoDataLoader: GeoDataLoader) extends Api {

  import scala.concurrent.ExecutionContext.Implicits.global

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.driver.api._
  import Schema._

  override def getCountries(): Seq[Country] = {
    Nil
  }

  override def getPoi(id: Long): Future[Poi] =
    db.run(pois.filter(_.id === id).result.head)

  override def updatePoi(poi: Poi): Seq[Poi] = Nil

  override def deletePoi(id: Long): Seq[Poi] = Nil

  override def getCitiesByName(fragment: String): Future[Seq[CityLabel]] = {
    val query = for {
      city <- cities.filter(_.name.toLowerCase.startsWith(fragment.toLowerCase))
      country <- countries if city.countryCode === country.code
    } yield (city.id, city.name, country.name, city.subdivisionId)

    db.run(query.take(10).result).map(_.map {
      case (id, name, country, subdivisionId) => CityLabel(id, name, country, subdivisionId.map(_.toString))
    })
  }

  override def getTopCities(): Future[Seq[City]] = {
    val query = cities.sortBy(_.population.desc).take(10)
    db.run(query.result)
  }

  override def getCity(cityId: Long): Future[City] =
    db.run(cities.filter(_.id === cityId).result.head)

}
