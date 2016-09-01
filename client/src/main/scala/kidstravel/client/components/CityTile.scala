package kidstravel.client.components

import diode.data.Pot
import diode.react.ModelProxy
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import kidstravel.client.KidsTravelMain.{CityLoc, Loc}
import kidstravel.client.services.FlickrImage
import kidstravel.shared.geo.City

object CityTile {

  case class Props(router: RouterCtl[Loc], proxy: ModelProxy[(City, Pot[FlickrImage])])

  class Backend($: BackendScope[Props, Unit]) {

    def render(props: Props) = {
      val city = props.proxy()._1
      val imgPot = props.proxy()._2
      println(s"Rendering ${city.name} ($imgPot)")
      <.div(
        ^.`class` := "col-lg-3",
        imgPot.renderEmpty(<.p("Loading …")),
        imgPot.renderPending(_ > 10, _ => <.p("Loading …")),
        imgPot.renderReady(img =>
          <.div(
            ^.backgroundImage := s"url(${img.url})",
            ^.backgroundSize := "cover",
            ^.height := 200.px,
            ^.marginBottom := 15.px,
            <.h3(
              ^.padding := 5.px + " " + 10.px,
              ^.margin := 0.px,
              ^.color := "white",
              ^.backgroundColor := "rgba(0, 0, 0, 0.5)",
              props.router.link(CityLoc(city.id))(city.name)(^.color := "white")
            )
          )
        )
      )
    }
  }

  private def component = ReactComponentB[Props]("CityTile").
    renderBackend[Backend].
    build

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[(City, Pot[FlickrImage])]) =
    component(Props(router, proxy))

}
