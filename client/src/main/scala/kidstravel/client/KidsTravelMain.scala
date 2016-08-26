package kidstravel.client

import japgolly.scalajs.react.ReactDOM
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._
import kidstravel.client.components.GlobalStyles
import kidstravel.client.modules.{Dashboard, MainMenu, PoiModule}
import kidstravel.client.services.KidsTravelCircuit
import kidstravel.client.logger._
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

  case object PoiLoc extends Loc

  // configure the router
  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    val poiWrapper = KidsTravelCircuit.connect(_.pois)
    val cityWrapper = KidsTravelCircuit.connect(_.cityCandidates)

    // wrap/connect components to the circuit
    (
      staticRoute(root, DashboardLoc) ~> renderR(ctl => cityWrapper(proxy => Dashboard(ctl, proxy))) |
      staticRoute("#todo", PoiLoc) ~> renderR(ctl => poiWrapper(PoiModule(_)))
      ).notFound(redirectToPage(DashboardLoc)(Redirect.Replace))
  }.renderWith(layout)

  val poiCountWrapper = KidsTravelCircuit.connect(_.pois.map(_.pois.size).toOption)

  // base layout for all pages
  def layout(c: RouterCtl[Loc], r: Resolution[Loc]) = {
    <.div(
      // here we use plain Bootstrap class names as these are specific to the top level layout defined here
      <.nav(^.className := "navbar navbar-inverse navbar-fixed-top",
        <.div(^.className := "container",
          <.div(^.className := "navbar-header", <.span(^.className := "navbar-brand", "SPA Tutorial")),
          <.div(^.className := "collapse navbar-collapse",
            // connect menu to model, because it needs to update when the number of open todos changes
            poiCountWrapper(proxy => MainMenu(c, r.page, proxy))
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
