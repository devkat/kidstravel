package kidstravel.client

import japgolly.scalajs.react.ReactDOM
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import kidstravel.client.components.GlobalStyles
import kidstravel.client.modules.{CityModule, Dashboard, MainMenu, PoiEditor}
import kidstravel.client.services.KidsTravelCircuit
import kidstravel.client.logger._
import kidstravel.shared.geo.City
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scalacss.Defaults._
import scalacss.ScalaCssReact._

@JSExport("KidsTravelMain")
object KidsTravelMain extends js.JSApp {

  // Define the locations (pages) used in this application
  sealed trait Loc

  case object DashboardLoc extends Loc

  case object NewPoiLoc extends Loc

  case class PoiLoc(id: Long) extends Loc

  case class CityLoc(id: Long) extends Loc

  // configure the router
  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    val dashboardWrapper = KidsTravelCircuit.connect(_.dashboard)
    val cityWrapper = KidsTravelCircuit.connect(_.city)
    val poisWrapper = KidsTravelCircuit.connect(_.pois)

    // wrap/connect components to the circuit
    (
      staticRoute(root, DashboardLoc) ~>
        renderR { ctl => dashboardWrapper(m => Dashboard(ctl, m)) } |

      dynamicRouteCT("#city" / long.caseClass[CityLoc]) ~>
        dynRenderR { case (cityLoc, ctl) => cityWrapper(CityModule(cityLoc.id, ctl, _)) } |

      staticRoute("#poi" / "new", NewPoiLoc) ~>
        renderR { ctl => poisWrapper(m => PoiEditor(None, ctl, m)) } |

      dynamicRouteCT("#poi" / long.caseClass[PoiLoc] / "edit") ~>
        dynRenderR { case (poiLoc, ctl) => poisWrapper(m => PoiEditor(Some(poiLoc.id), ctl, m)) }

    ).notFound(redirectToPage(DashboardLoc)(Redirect.Replace))
  }.renderWith(layout)

  //val poiCountWrapper = KidsTravelCircuit.connect(_.pois.map(_.pois.size).toOption)

  // base layout for all pages
  def layout(c: RouterCtl[Loc], r: Resolution[Loc]) = {
    <.div(
      // here we use plain Bootstrap class names as these are specific to the top level layout defined here
      <.nav(^.className := "navbar navbar-inverse navbar-fixed-top",
        <.div(^.className := "container",
          <.div(^.className := "navbar-header", <.span(^.className := "navbar-brand", "KidsTravel")),
          <.div(^.className := "collapse navbar-collapse"
            // connect menu to model, because it needs to update when the number of open todos changes
            // poiCountWrapper(proxy => MainMenu(c, r.page, proxy))
          )
        )
      ),
      // currently active module is shown in this container
      <.div(^.className := "container", r.render())
    )
  }

  @JSExport
  def main(): Unit = {
    log.warn("Application starting")
    // send log messages also to the server
    log.enableServerLogging("/logging")
    log.info("This message goes to server as well")

    // create stylesheet
    GlobalStyles.addToDocument()
    // create the router
    val router = Router(BaseUrl.until_#, routerConfig)
    // tell React to render the router in the document body
    ReactDOM.render(router(), dom.document.getElementById("root"))
  }
}
