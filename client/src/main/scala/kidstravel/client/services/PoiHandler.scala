package kidstravel.client.services

import autowire._
import boopickle.Default._
import diode.{Action, ActionHandler, Dispatcher, ModelRW}
import diode.data._
import kidstravel.shared.Api
import kidstravel.shared.poi.Poi

import scala.concurrent.Future
import scala.util.{Failure, Try}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

case class GetPoi(poiId: Long) extends Action

case object RefreshPois extends Action

case class UpdatePoi(poi: Poi) extends Action

case class DeletePoi(poi: Poi) extends Action

// AsyncAction for updating POIs
case class UpdatePois(
                      keys: Set[Long],
                      state: PotState = PotState.PotEmpty,
                      result: Try[Map[Long, Pot[Poi]]] = Failure(new AsyncAction.PendingException)
                    ) extends AsyncAction[Map[Long, Pot[Poi]], UpdatePois] {
  def next(newState: PotState, newValue: Try[Map[Long, Pot[Poi]]]) =
    UpdatePois(keys, newState, newValue)
}

// an implementation of Fetch for POIs
class PoiFetch(dispatch: Dispatcher) extends Fetch[Long] {

  override def fetch(key: Long): Unit =
    dispatch(UpdatePois(keys = Set(key)))

  override def fetch(keys: Traversable[Long]): Unit =
    dispatch(UpdatePois(keys = Set() ++ keys))

}

/**
  * Handles actions related to POIs
  *
  * @param modelRW Reader/Writer to access the model
  */
class PoiHandler[M](modelRW: ModelRW[M, PotMap[Long, Poi]]) extends ActionHandler(modelRW) {

  def loadPois(ids: Set[Long]): Future[Map[Long, Pot[Poi]]] =
    Future.sequence(ids.map(id => AjaxClient[Api].getPoi(id).call().map(poi => (id, Ready(poi))))).map(_.toMap)

  override def handle = {
    case action: UpdatePois =>
      val updateEffect = action.effect(loadPois(action.keys))(identity)
      action.handleWith(this, updateEffect)(AsyncAction.mapHandler(action.keys))
  }

}
