package kidstravel.client.modules

import diode.data.Pot
import diode.react._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import kidstravel.client.KidsTravelMain.{Loc, PoiLoc}
import kidstravel.client.components.SearchBox
import kidstravel.shared.geo.CityLabel

import scala.language.existentials

object Dashboard {

  case class Props(router: RouterCtl[Loc], cityProxy: ModelProxy[Pot[Seq[CityLabel]]])

  case class State()

  // create the React component for Dashboard
  private val component = ReactComponentB[Props]("Dashboard")
    // create and store the connect proxy in state for later use
    .initialState_P(props => State())
    .renderPS { (_, props, state) =>
      <.div(
        <.h2("Dashboard"),
        // create a link to the To Do view
        <.div(props.router.link(PoiLoc)("Check your todos!")),
        SearchBox(props.cityProxy)
      )
    }
    .build

  def apply(router: RouterCtl[Loc], cityProxy: ModelProxy[Pot[Seq[CityLabel]]]) =
    component(Props(router, cityProxy))
}
