package kidstravel.client.components

import diode.Action
import diode.data.Pot
import diode.react.ModelProxy
import diode.react.ReactPot._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

trait SearchBox {

  type T

  def getAction: String => Action
  def updateAction: Seq[T] => Action
  def asString: T => String

  case class Props(proxy: ModelProxy[Pot[Seq[T]]])

  class Backend($: BackendScope[Props, Unit]) {

    private def updateCandidates(e: ReactEventI): Callback = {
      val fragment = e.target.value
      if (fragment.length >= 3)
        $.props >>= (_.proxy.dispatch(getAction(fragment)))
      else
        $.props >>= (_.proxy.dispatch(updateAction(Seq.empty)))
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
        props.proxy().render(ts => <.ol(ts.map(t => <.li(asString(t)))))
      )
  }

  private val component = ReactComponentB[Props]("SearchBox").
    renderBackend[Backend].
    build

  def apply(proxy: ModelProxy[Pot[Seq[T]]]) = component(Props(proxy))

}
