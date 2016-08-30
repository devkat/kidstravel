package kidstravel.client.components

import diode.Action
import diode.data.{Empty, Pot}
import diode.react.ModelProxy
import diode.react.ReactPot._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, _}
import japgolly.scalajs.react.vdom.prefix_<^._
import kidstravel.client.services.KidsTravelCircuit

trait Tiles {

  type T <: AnyRef

  def getAction: Action

  def tileComponent(proxy: ModelProxy[T]): ReactElement

  case class Props(proxy: ModelProxy[Pot[Seq[T]]])

  class Backend($: BackendScope[Props, Unit]) {

    def load = $.props >>= (p => p.proxy.value match {
      case Empty => p.proxy.dispatch(getAction)
      case _ => Callback.empty
    })

    def render(props: Props) = {
      val proxy = props.proxy
      <.div(
        ^.`class` := "row",
        proxy().renderFailed(ex => "Error loading"),
        proxy().renderPending(_ > 100, _ => <.p("Loading …")),
        proxy().render(items =>
          items.zipWithIndex.map { case (_, i) =>
            proxy.connect(_.get(i)).apply(tileComponent(_))
            //proxy.wrap(_.get(i))(tileComponent(_))
            //tileComponent(proxy.zoom(_.get(i)))
          }
        )
      )
    }
  }

  private val component = ReactComponentB[Props]("Tiles").
    renderBackend[Backend].
    componentDidMount(_.backend.load).
    build

  def apply(proxy: ModelProxy[Pot[Seq[T]]]) = component(Props(proxy))

}
