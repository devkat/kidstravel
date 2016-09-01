package kidstravel.client.components

import diode.data.{Empty, Pot}
import diode.react.ModelProxy
import diode.react.ReactPot._
import diode.{Action, ModelR}
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, _}
import kidstravel.client.KidsTravelMain.Loc
import kidstravel.client.services.{KidsTravelCircuit, RootModel}

case class TileProps[T](router: RouterCtl[Loc], proxy: ModelProxy[T])

/**
  * Render sequence of models as tiles.
  */
trait Tiles {

  // The type of the model objects.
  type T <: AnyRef

  /**
    * Override to provide the action to obtain the model objects.
    * @return An action.
    */
  def getAction: Action

  /**
    * Returns the tile component class.
    * @return
    */
  def tileComponent: ReactComponentC.ReqProps[TileProps[T], _, _, _ <: TopNode]

  case class Props(router: RouterCtl[Loc], proxy: ModelProxy[Pot[Seq[T]]])

  class Backend($: BackendScope[Props, Pot[Seq[T]]]) {

    private var unsubscribe = Option.empty[() => Unit]

    def willMount(props: Props) = {
      val modelReader = props.proxy.modelReader.asInstanceOf[ModelR[RootModel, Pot[Seq[T]]]]
      Callback {
        unsubscribe = Some(KidsTravelCircuit.subscribe(modelReader)(changeHandler(modelReader)))
      } >> $.setState(modelReader())
    }

    def willUnmount = Callback {
      unsubscribe.foreach(f => f())
      unsubscribe = None
    }

    private def changeHandler(modelReader: ModelR[RootModel, Pot[Seq[T]]])(
        cursor: ModelR[RootModel, Pot[Seq[T]]]): Unit = {
      // modify state if we are mounted and state has actually changed
      if ($.isMounted() && modelReader =!= $.accessDirect.state) {
        $.accessDirect.setState(modelReader())
      }
    }

    def didMount = $.props >>= (p => p.proxy.value match {
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
            //proxy.connector.connect(proxy.modelReader.zoom(_.get(i)), s"tile_$i": js.Any).apply(tileComponent(props.router, _))
            //proxy.connector.connect(proxy.modelReader.zoom(_.get(i))).apply(tileComponent(props.router, _))
            //proxy.wrap(_.get(i))(tileComponent(_))
            tileComponent.withKey(s"tile_$i")(TileProps(props.router, proxy.zoom(_.get(i))))
          }
        )
      )
    }
  }

  private val component = ReactComponentB[Props]("Tiles").
    initialState(Empty: Pot[Seq[T]]).
    renderBackend[Backend].
    componentWillMount(scope => scope.backend.willMount(scope.props)).
    componentDidMount(_.backend.didMount).
    build

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[Pot[Seq[T]]]) = component(Props(router, proxy))

}
