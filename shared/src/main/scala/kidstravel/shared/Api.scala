package kidstravel.shared

import kidstravel.shared.geo.Country
import kidstravel.shared.poi.Poi

trait Api {

  def getPois(): Seq[Poi]

  def updatePoi(poi: Poi): Seq[Poi]

  def deletePoi(id: Long): Seq[Poi]

  def getCountries(): Seq[Country]

}
