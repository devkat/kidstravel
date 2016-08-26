package kidstravel.shared.geo

case class City(id: Long, name: String, countryCode: String, subdivisionId: Option[Long])

case class CityLabel(id: Long, name: String, country: String, subdivision: Option[String]) {
  def asString(): String = {
    val subdiv = subdivision.fold("")(d => s" ($d)")
    s"$name$subdiv, $country"
  }
}