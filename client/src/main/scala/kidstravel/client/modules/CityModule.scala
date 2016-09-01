package kidstravel.client.modules

import diode.data.Pot
import diode.react.ModelProxy
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB}
import kidstravel.client.services.RefreshPois
import kidstravel.shared.geo.City

object CityModule {

  case class Props(cityId: Long, proxy: ModelProxy[Pot[City]])

  class Backend($: BackendScope[Props, Unit]) {
    def mounted(props: Props) =
      Callback { println(s"Mounted city ${props.cityId}")}
      //Callback.when(props.proxy().isEmpty)(props.proxy.dispatch(RefreshPois))

    def render(p: Props) =
      <.div()
  }

  val component = ReactComponentB[Props]("TODO")
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted(scope.props))
    .build

  def apply(cityId: Long, proxy: ModelProxy[Pot[City]]) = component(Props(cityId, proxy))
}
