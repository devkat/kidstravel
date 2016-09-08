package kidstravel.client.modules

import diode.data._
import diode.react.ModelProxy
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import kidstravel.client.KidsTravelMain.Loc
import kidstravel.shared.poi.Poi

object PoiEditor {

  case class Props(poiId: Option[Long], router: RouterCtl[Loc], proxy: ModelProxy[PotMap[Long, Poi]])

  class Backend($: BackendScope[Props, Unit]) {

    def renderPoi(poi: Poi) =
      <.form(
        <.div(
          ^.`class` := "form-group",
          <.label("Name"),
          <.input(
            ^.`class` := "form-control",
            ^.`type` := "text",
            ^.value := poi.name
          ),
          <.button(
            ^.`type` := "submit",
            ^.`class` := "btn btn-default"
          )
        )
      )

    def render(props: Props) = {
      props.poiId match {
        case Some(id) =>
          val pois = props.proxy()
          pois(id) match {
            case Empty | Pending(_) => <.p("Loading …")
            case Failed(_) => <.p("Could not load point of interest.")
            case Unavailable => <.p("Point of interest not available.")
            case Ready(poi) => renderPoi(poi)
            case _ => <.p()
          }
        case None =>
          val newPoi = Poi(-1, "")
          renderPoi(newPoi)
      }
    }
  }

  val component = ReactComponentB[Props]("PoiEditor")
    .renderBackend[Backend]
    .build

  def apply(poiId: Option[Long], router: RouterCtl[Loc], proxy: ModelProxy[PotMap[Long, Poi]]) =
    component(Props(poiId, router, proxy))

}
