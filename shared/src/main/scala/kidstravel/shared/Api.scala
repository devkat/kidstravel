package kidstravel.shared

import kidstravel.shared.geo.{City, CityLabel, Country}
import kidstravel.shared.poi.Poi

import scala.concurrent.Future

trait Api {

  def getCitiesByName(fragment: String): Future[Seq[CityLabel]]

  def getPois(): Seq[Poi]

  def updatePoi(poi: Poi): Seq[Poi]

  def deletePoi(id: Long): Seq[Poi]

  def getCountries(): Seq[Country]

}
