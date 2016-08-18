package kidstravel.client.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import kidstravel.client.components.Bootstrap.Button
import kidstravel.shared.poi.Poi

import scalacss.ScalaCssReact._

object PoiList {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class PoiListProps(
                            pois: Seq[Poi],
                            stateChange: Poi => Callback,
                            editPoi: Poi => Callback,
                            deletePoi: Poi => Callback
  )

  private val PoiList = ReactComponentB[PoiListProps]("PoiList")
    .render_P(p => {
      val style = bss.listGroup
      def renderItem(poi: Poi) = {
        <.li(
          <.span(poi.name),
          Button(Button.Props(p.editPoi(poi), addStyles = Seq(bss.pullRight, bss.buttonXS)), "Edit"),
          Button(Button.Props(p.deletePoi(poi), addStyles = Seq(bss.pullRight, bss.buttonXS)), "Delete")
        )
      }
      <.ul(style.listGroup)(p.pois map renderItem)
    })
    .build

  def apply(items: Seq[Poi], stateChange: Poi => Callback, editItem: Poi => Callback, deleteItem: Poi => Callback) =
    PoiList(PoiListProps(items, stateChange, editItem, deleteItem))
}
