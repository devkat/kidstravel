package services

import java.util.{Date, UUID}
import javax.inject.Inject

import geo.GeoDataLoader
import kidstravel.shared.Api
import kidstravel.shared.geo.Country
import kidstravel.shared.poi.Poi
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.lifted.TableQuery

class ApiService @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider,
    protected val geoDataLoader: GeoDataLoader) extends Api {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.driver.api._
  //private val Countries = TableQuery[Coun]

  override def getCountries(): Seq[Country] = {
    Nil
  }

  override def getPois(): Seq[Poi] = Nil

  override def updatePoi(poi: Poi): Seq[Poi] = Nil

  override def deletePoi(id: Long): Seq[Poi] = Nil
}
