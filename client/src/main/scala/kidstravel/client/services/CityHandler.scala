package kidstravel.client.services

import diode.ActionResult.{ModelUpdate, ModelUpdateEffect}
import diode.{Action, ActionHandler, Effect, ModelRW}
import diode.data.{Empty, Pending, Pot, Ready}
import kidstravel.shared.Api
import kidstravel.shared.geo.{City, CityLabel}
import autowire._
import boopickle.Default._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

case object GetTopCities extends Action

case class UpdateCities(cities: Seq[City]) extends Action

case class GetCityImage(city: City) extends Action

case class UpdateCityImage(city: City, image: FlickrImage) extends Action

case class GetCityCandidates(fragment: String) extends Action

case class UpdateCityCandidates(candidates: Seq[CityLabel]) extends Action

case class GetCity(cityId: Long) extends Action

case class UpdateCity(city: City) extends Action

class CitySearchHandler[M](modelRW: ModelRW[M, Pot[Seq[CityLabel]]]) extends ActionHandler(modelRW) {
  override def handle = {
    case GetCityCandidates(fragment) =>
      effectOnly(Effect(AjaxClient[Api].getCitiesByName(fragment).call().map(UpdateCityCandidates)))
    case UpdateCityCandidates(cities) =>
      updated(Ready(cities))
  }
}

class TopCitiesHandler[M](modelRW: ModelRW[M, Pot[Seq[(City, Pot[FlickrImage])]]])
  extends ActionHandler(modelRW) {

  def zoomToFlickrImage(city: City): ModelRW[M, Pot[FlickrImage]] =
    modelRW.zoomRW(_.get.find(_._1 == city).get._2)((m, v) =>
      m.map(_.map { case (c, img) => (c, if (c == city) v else img) })
    )

  override def handle = {

    case GetTopCities =>
      ModelUpdateEffect(
        modelRW.updated(Pending()),
        Effect(AjaxClient[Api].getTopCities().call().map(UpdateCities))
      )

    case UpdateCities(cities) =>
      updated(
        Ready(cities.map((_, Empty))),
        {
          val effects: Seq[Effect] = cities.map(city => Effect(FlickrService.search(s"${city.name} skyline").map(UpdateCityImage(city, _))))
          effects.reduceLeft(_ + _)
        }
      )

    case GetCityImage(city) =>
      ModelUpdateEffect(
        zoomToFlickrImage(city).updated(Pending()),
        Effect(FlickrService.search(s"${city.name} skyline").map(UpdateCityImage(city, _)))
      )

    case UpdateCityImage(city, image) =>
      ModelUpdate(zoomToFlickrImage(city).updated(Ready(image)))

  }
}

class CityHandler[M](modelRW: ModelRW[M, Pot[City]])
  extends ActionHandler(modelRW) {
  override def handle = {

    case GetCity(cityId) =>
      ModelUpdateEffect(
        modelRW.updated(Pending()),
        Effect(AjaxClient[Api].getCity(cityId).call().map(UpdateCity))
      )

    case UpdateCity(city) =>
      updated(Ready(city))

  }
}
