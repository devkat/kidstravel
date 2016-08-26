package kidstravel.client.components

import diode.data.Pot
import diode.react.ModelProxy
import diode.react.ReactPot._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import kidstravel.client.services.{GetCityCandidates, UpdateCityCandidates}
import kidstravel.shared.geo.CityLabel

object SearchBox {

  case class Props(proxy: ModelProxy[Pot[Seq[CityLabel]]])

  class Backend($: BackendScope[Props, Unit]) {

    private def updateCandidates(e: ReactEventI): Callback = {
      val fragment = e.target.value
      if (fragment.length >= 3)
        $.props >>= (_.proxy.dispatch(GetCityCandidates(fragment)))
      else
        $.props >>= (_.proxy.dispatch(UpdateCityCandidates(Seq.empty)))
    }

    def render(props: Props) =
      <.div(
        <.input(
          ^.`type` := "text",
          ^.placeholder := "Enter at least 3 characters",
          ^.onKeyUp ==> updateCandidates
        ),
        props.proxy().renderFailed(ex => "Error loading"),
        props.proxy().renderPending(_ > 100, _ => <.p("Loading …")),
        props.proxy().render(cities =>
          <.ol(cities.map(city => <.li(city.asString)))
        )
      )
  }

  private val component = ReactComponentB[Props]("SearchBox").
    renderBackend[Backend].
    build

  def apply(cityProxy: ModelProxy[Pot[Seq[CityLabel]]]) = component(Props(cityProxy))

}
