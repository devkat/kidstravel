package kidstravel.client.modules

import diode.data.Pot
import diode.react._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import kidstravel.client.KidsTravelMain.{Loc, PoiLoc}
import kidstravel.shared.poi.Poi

import scala.language.existentials
import scala.util.Random

object Dashboard {

  case class Props(router: RouterCtl[Loc])

  case class State()

  // create the React component for Dashboard
  private val component = ReactComponentB[Props]("Dashboard")
    // create and store the connect proxy in state for later use
    .initialState_P(props => State())
    .renderPS { (_, props, state) =>
      <.div(
        // header, MessageOfTheDay and chart components
        <.h2("Dashboard"),
        // create a link to the To Do view
        <.div(props.router.link(PoiLoc)("Check your todos!"))
      )
    }
    .build

  def apply(router: RouterCtl[Loc]) = component(Props(router))
}
