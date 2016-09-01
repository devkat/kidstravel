package kidstravel.client.components

import diode.data.{Empty, Pot}
import diode.react.ModelProxy
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ShouldComponentUpdate}
import japgolly.scalajs.react.vdom.prefix_<^._
import kidstravel.client.KidsTravelMain.{CityLoc, Loc}
import kidstravel.client.logger._
import kidstravel.client.services.{FlickrImage, GetCityImage}
import kidstravel.shared.geo.City

object CityTile {

  case class Props(router: RouterCtl[Loc], proxy: ModelProxy[(City, Pot[FlickrImage])])

  class Backend($: BackendScope[Props, Unit]) {

    def load(props: Props) =
      Callback.when(props.proxy()._2.isEmpty) {
        log.info(s"Loading ${props.proxy()._1.name}")
        props.proxy.dispatch(GetCityImage(props.proxy()._1))
      }

    def render(props: Props) = {
      val city = props.proxy()._1
      val imgPot = props.proxy()._2
      println(s"Rendering ${city.name} ($imgPot)")
      <.div(
        ^.`class` := "col-lg-3",
        imgPot.renderEmpty(<.p("Loading …")),
        imgPot.renderPending(_ > 10, _ => <.p("Loading …")),
        imgPot.renderReady(img =>
          <.div(
            ^.backgroundImage := s"url(${img.url})",
            ^.backgroundSize := "cover",
            ^.height := 200.px,
            ^.marginBottom := 15.px,
            <.h3(
              ^.padding := 5.px + " " + 10.px,
              ^.margin := 0.px,
              ^.color := "white",
              ^.backgroundColor := "rgba(0, 0, 0, 0.5)",
              props.router.link(CityLoc(city.id))(city.name)(^.color := "white")
            )
          )
        )
      )
    }
  }
/*
  implicit val cityReuse = Reusability.caseClass[City]
  implicit val flickrImageReuse = Reusability.caseClass[FlickrImage]
*/
  private def component = ReactComponentB[Props]("CityTile").
    renderBackend[Backend].
    componentDidMount(p => p.backend.load(p.props)).
  /*
    configure(Reusability.shouldComponentUpdate).
      */
    shouldComponentUpdate(p => {
      log.info(s"Should update ${p.currentProps.proxy()._1.name}")
      false}).
    build

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[(City, Pot[FlickrImage])]) =
    component(Props(router, proxy))

}
