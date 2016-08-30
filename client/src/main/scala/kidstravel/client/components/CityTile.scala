package kidstravel.client.components

import diode.data.Pot
import diode.react.ModelProxy
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._
import kidstravel.shared.geo.City

object CityTile {

  case class Props(proxy: ModelProxy[City])

  class Backend($: BackendScope[Props, Unit]) {

    def render(props: Props) = {
      val city = props.proxy()
      <.div(
        ^.`class` := "col-lg-3",
        <.h3(city.name)
      )
    }
  }

  private val component = ReactComponentB[Props]("CityTile").
    renderBackend[Backend].
    build

  def apply(proxy: ModelProxy[City]) = component(Props(proxy))
}
