package kidstravel.client.services

import autowire._
import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector
import kidstravel.shared.Api
import boopickle.Default._
import kidstravel.shared.poi.Poi

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

// Actions
case object RefreshPois extends Action

case class UpdateAllPois(pois: Seq[Poi]) extends Action

case class UpdatePoi(poi: Poi) extends Action

case class DeletePoi(poi: Poi) extends Action



// The base model of our application
case class RootModel(pois: Pot[Pois])

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



// Application circuit
object KidsTravelCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  // initial application model
  override protected def initialModel = RootModel(Empty)
  // combine all handlers into one
  override protected val actionHandler = composeHandlers(
    new PoiHandler(zoomRW(_.pois)((m, v) => m.copy(pois = v)))
  )
}