package kidstravel.client.services

import autowire._
import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector
import kidstravel.shared.Api
import boopickle.Default._
import kidstravel.shared.geo.{City, CityLabel}
import kidstravel.shared.poi.Poi

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

// Actions
case object RefreshPois extends Action

case class UpdateAllPois(pois: Seq[Poi]) extends Action

case class UpdatePoi(poi: Poi) extends Action

case class DeletePoi(poi: Poi) extends Action

case object GetTopCities extends Action

case class UpdateCities(cities: Seq[City]) extends Action

case class GetCityCandidates(fragment: String) extends Action

case class UpdateCityCandidates(candidates: Seq[CityLabel]) extends Action

case class DashboardModel(
  cityCandidates: Pot[Seq[CityLabel]],
  topCities: Pot[Seq[City]]
)

// The base model of our application
case class RootModel(
  pois: Pot[Pois],
  dashboard: DashboardModel
)

case class Pois(pois: Seq[Poi]) {
  def updated(newPoi: Poi) = {
    pois.indexWhere(_.id == newPoi.id) match {
      case -1 =>
        // add new
        Pois(pois :+ newPoi)
      case idx =>
        // replace old
        Pois(pois.updated(idx, newPoi))
    }
  }
  def remove(poi: Poi) = Pois(pois.filterNot(_ == poi))
}

/**
  * Handles actions related to todos
  *
  * @param modelRW Reader/Writer to access the model
  */
class PoiHandler[M](modelRW: ModelRW[M, Pot[Pois]]) extends ActionHandler(modelRW) {
  override def handle = {
    case RefreshPois =>
      effectOnly(Effect(AjaxClient[Api].getPois().call().map(UpdateAllPois)))
    case UpdateAllPois(todos) =>
      // got new todos, update model
      updated(Ready(Pois(todos)))
    case UpdatePoi(poi) =>
      // make a local update and inform server
      updated(value.map(_.updated(poi)), Effect(AjaxClient[Api].updatePoi(poi).call().map(UpdateAllPois)))
    case DeletePoi(item) =>
      // make a local update and inform server
      updated(value.map(_.remove(item)), Effect(AjaxClient[Api].deletePoi(item.id).call().map(UpdateAllPois)))
  }
}

class CitySearchHandler[M](modelRW: ModelRW[M, Pot[Seq[CityLabel]]]) extends ActionHandler(modelRW) {
  override def handle = {
    case GetCityCandidates(fragment) =>
      effectOnly(Effect(AjaxClient[Api].getCitiesByName(fragment).call().map(UpdateCityCandidates)))
    case UpdateCityCandidates(cities) =>
      updated(Ready(cities))
  }
}

case class TopCityHandler[M](modelRWx: ModelRW[M, Pot[Seq[City]]]) extends ActionHandler(modelRWx) {
  override def handle = {
    case GetTopCities =>
      effectOnly(Effect(AjaxClient[Api].getTopCities().call().map(UpdateCities)))
    case UpdateCities(cities) =>
      updated(Ready(cities))
  }
}

// Application circuit
object KidsTravelCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  // initial application model
  override protected def initialModel = RootModel(Empty, DashboardModel(Empty, Empty))
  // combine all handlers into one
  override protected val actionHandler = composeHandlers(
    new PoiHandler(zoomRW(_.pois)((m, v) => m.copy(pois = v))),
    new CitySearchHandler(zoomRW(_.dashboard.cityCandidates)((m, v) => m.copy(dashboard = m.dashboard.copy(cityCandidates = v)))),
    new TopCityHandler(zoomRW(_.dashboard.topCities)((m, v) => m.copy(dashboard = m.dashboard.copy(topCities = v))))
  )
}
