package kidstravel.client.components
import diode.Action
import diode.react.ModelProxy
import japgolly.scalajs.react.ReactElement
import kidstravel.client.services.{GetTopCities, UpdateCities}
import kidstravel.shared.geo.City

object CityTiles extends Tiles {

  override type T = City

  override def getAction = GetTopCities

  override def tileComponent(proxy: ModelProxy[City]): ReactElement =
    CityTile(proxy)

}
