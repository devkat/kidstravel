package kidstravel.client.components

import diode.data.{Empty, Pot}
import diode.react.ModelProxy
import diode.react.ReactPot._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ShouldComponentUpdate}
import japgolly.scalajs.react.vdom.prefix_<^._
import kidstravel.client.services.{FlickrImage, GetCityImage}
import kidstravel.shared.geo.City

object CityTile {

  case class Props(proxy: ModelProxy[(City, Pot[FlickrImage])])

  class Backend($: BackendScope[Props, Unit]) {

    def load = $.props >>= (p => p.proxy.value._2 match {
      case Empty => p.proxy.dispatch(GetCityImage(p.proxy.value._1))
      case _ => Callback.empty
    })

    def render(props: Props) = {
      val city = props.proxy()._1
      val imgPot = props.proxy()._2
      println(s"Rendering ${city.name} ($imgPot)")
      <.div(
        ^.`class` := "col-lg-3",
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
              ^.backgroundColor := "rgba(0, 0, 0, 0.5)"
            )(city.name)
          )
        )
      )
    }
  }

  private def component = ReactComponentB[Props]("CityTile").
    renderBackend[Backend].
    componentDidMount(_.backend.load).
    //shouldComponentUpdate(p => p.currentProps.proxy()._1 != p.nextProps.proxy()._1).
    build

  def apply(proxy: ModelProxy[(City, Pot[FlickrImage])]) = component(Props(proxy))
}
