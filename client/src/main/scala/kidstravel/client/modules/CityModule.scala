package kidstravel.client.modules

import diode.data.Pot
import diode.react.ModelProxy
import diode.react.ReactPot._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB}
import kidstravel.client.services.{GetCity}
import kidstravel.shared.geo.City

object CityModule {

  case class Props(cityId: Long, proxy: ModelProxy[Pot[City]])

  class Backend($: BackendScope[Props, Unit]) {
    def mounted(props: Props) =
      Callback.when(props.proxy().isEmpty)(props.proxy.dispatch(GetCity(props.cityId)))

    def render(p: Props) = {
      val pot = p.proxy()
      <.div(
        pot.renderPending(_ > 10, _ => <.p("Loading …")),
        pot.renderFailed(_ => <.p("Could not load city.")),
        pot.renderReady(city =>
          <.div(
            <.h1(city.name)
          )
        )
      )
    }
  }

  val component = ReactComponentB[Props]("City")
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted(scope.props))
    .build

  def apply(cityId: Long, proxy: ModelProxy[Pot[City]]) = component(Props(cityId, proxy))
}
