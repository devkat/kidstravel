package kidstravel.client.components
import diode.Action
import diode.data.Pot
import diode.react.ModelProxy
import japgolly.scalajs.react.ReactElement
import japgolly.scalajs.react.extra.router.RouterCtl
import kidstravel.client.KidsTravelMain.Loc
import kidstravel.client.services.{FlickrImage, GetTopCities, UpdateCities}
import kidstravel.shared.geo.City

object CityTiles extends Tiles {

  override type T = (City, Pot[FlickrImage])

  override def getAction = GetTopCities

  override def tileComponent = CityTile.apply

}
