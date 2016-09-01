package kidstravel.client.modules

import diode.data.Pot
import diode.react._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import kidstravel.client.KidsTravelMain.{Loc, PoiLoc}
import kidstravel.client.components.{CitySearchBox, CityTiles}
import kidstravel.client.services.DashboardModel
import kidstravel.shared.geo.{City, CityLabel}

import scala.language.existentials

object Dashboard {

  case class Props(
    router: RouterCtl[Loc],
    proxy: ModelProxy[DashboardModel])

  case class State()

  // create the React component for Dashboard
  private val component = ReactComponentB[Props]("Dashboard")
    // create and store the connect proxy in state for later use
    .initialState_P(props => State())
    .renderPS { (_, props, state) =>
      <.div(
        <.div(
          <.h2("Search"),
          // create a link to the To Do view
          //<.div(props.router.link(PoiLoc)("Check your todos!")),
          CitySearchBox(props.proxy.zoom(_.cityCandidates))
        ),
        <.div(
          <.h2("Top 10 Cities"),
          props.proxy.connect(_.topCities).apply(CityTiles(props.router, _))
        )
      )
    }.
    build

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[DashboardModel]) =
    component(Props(router, proxy))
}
