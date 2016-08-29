package kidstravel.client.components
import diode.Action
import kidstravel.client.services.{GetTopCities, UpdateCities}
import kidstravel.shared.geo.City

object CityTiles extends Tiles {

  override type T = City

  override def getAction = GetTopCities

}
