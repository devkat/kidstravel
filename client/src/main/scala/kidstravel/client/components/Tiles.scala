package kidstravel.client.components

import diode.Action
import diode.data.Pot
import diode.react.ModelProxy
import diode.react.ReactPot._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, _}
import japgolly.scalajs.react.vdom.prefix_<^._

trait Tiles {

  type T

  def getAction: Action

  case class Props(proxy: ModelProxy[Pot[Seq[T]]])

  class Backend($: BackendScope[Props, Unit]) {

    def load = $.props >>= (_.proxy.dispatch(getAction))

    def render(props: Props) =
      <.div(
        ^.`class` := "row",
        props.proxy().renderFailed(ex => "Error loading"),
        props.proxy().renderPending(_ > 100, _ => <.p("Loading …")),
        props.proxy().render(items =>
          <.ol(items.map(item => <.li(item.toString)))
        )
      )
  }

  private val component = ReactComponentB[Props]("Tiles").
    renderBackend[Backend].
    componentDidMount(_.backend.load).
    build

  def apply(proxy: ModelProxy[Pot[Seq[T]]]) = component(Props(proxy))

}
