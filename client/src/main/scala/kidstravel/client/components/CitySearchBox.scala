package kidstravel.client.components

import kidstravel.client.services.{GetCityCandidates, UpdateCityCandidates}
import kidstravel.shared.geo.CityLabel

/**
  * Created by nobby on 26.08.16.
  */
object CitySearchBox extends SearchBox {

  override type T = CityLabel

  override def getAction = GetCityCandidates(_)
  override def updateAction = UpdateCityCandidates(_)

  override def asString = _.asString
}
