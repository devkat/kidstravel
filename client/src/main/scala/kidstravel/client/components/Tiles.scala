package kidstravel.client.components

import diode.Action
import diode.data.{Empty, Pot}
import diode.react.ModelProxy
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.{BackendScope, ReactComponentB, _}
import japgolly.scalajs.react.vdom.prefix_<^._
import kidstravel.client.logger._
import kidstravel.client.KidsTravelMain.Loc
import kidstravel.client.services.KidsTravelCircuit
import scala.scalajs.js

trait Tiles {

  type T <: AnyRef

  def getAction: Action

  def tileComponent(router: RouterCtl[Loc], proxy: ModelProxy[T]): ReactElement

  case class Props(router: RouterCtl[Loc], proxy: ModelProxy[Pot[Seq[T]]])

  class Backend($: BackendScope[Props, Unit]) {

    def load = $.props >>= (p => p.proxy.value match {
      case Empty => p.proxy.dispatch(getAction)
      case _ => Callback.empty
    })

    def render(props: Props) = {
      println("Rendering tiles")
      val proxy = props.proxy
      <.div(
        ^.`class` := "row",
        proxy().renderFailed(ex => "Error loading"),
        proxy().renderPending(_ > 100, _ => <.p("Loading …")),
        proxy().render(items =>
          items.zipWithIndex.map { case (_, i) =>
            proxy.connector.connect(proxy.modelReader.zoom(_.get(i)), s"tile_$i": js.Any).apply(tileComponent(props.router, _))
            //proxy.connector.connect(proxy.modelReader.zoom(_.get(i))).apply(tileComponent(props.router, _))
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

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[Pot[Seq[T]]]) = component(Props(router, proxy))

}
