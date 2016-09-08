package kidstravel.client.services

import autowire._
import diode._
import diode.data._
import diode.react.ReactConnector
import kidstravel.shared.Api
import boopickle.Default._
import diode.ActionResult.{ModelUpdate, ModelUpdateEffect}
import kidstravel.shared.geo.{City, CityLabel}
import kidstravel.shared.poi.Poi

case class DashboardModel(
  cityCandidates: Pot[Seq[CityLabel]],
  topCities: Pot[Seq[(City, Pot[FlickrImage])]]
)

// The base model of our application
case class RootModel(
  dashboard: DashboardModel,
  city: Pot[City],
  pois: PotMap[Long, Poi]
)

// Application circuit
object KidsTravelCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {

  // initial application model
  override protected def initialModel =
    RootModel(DashboardModel(Empty, Empty), Empty, PotMap(new PoiFetch(this)))

  // combine all handlers into one
  override protected val actionHandler = composeHandlers(
    new PoiHandler(zoomRW(_.pois)((m, v) => m.copy(pois = v))),
    new CitySearchHandler(
      zoomRW(_.dashboard)((m, v) => m.copy(dashboard = v)).
      zoomRW(_.cityCandidates)((m, v) => m.copy(cityCandidates = v))),
    new TopCitiesHandler(
      zoomRW(_.dashboard)((m, v) => m.copy(dashboard = v)).
      zoomRW(_.topCities)((m, v) => m.copy(topCities = v))),
    new CityHandler(zoomRW(_.city)((m, v) => m.copy(city = v)))
  )
}
