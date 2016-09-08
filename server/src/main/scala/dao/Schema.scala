package dao

import kidstravel.shared.geo.{City, Country, Subdivision}
import kidstravel.shared.poi.Poi
import slick.driver.H2Driver.api._
import slick.lifted.Tag

object Schema {

  class Countries(tag: Tag) extends Table[Country](tag, "country") {
    def code = column[String]("code", O.PrimaryKey)
    def name = column[String]("name")
    def * = (code, name) <> (Country.tupled, Country.unapply)
  }

  val countries = TableQuery[Countries]

  class Subdivisions(tag: Tag) extends Table[Subdivision](tag, "subdivision") {
    def id = column[Long]("id", O.PrimaryKey)
    def name = column[String]("name")
    def countryCode = column[String]("country_code")
    def country = foreignKey("fk_country", countryCode, countries)(_.code, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)
    def * = (id, name, countryCode) <> (Subdivision.tupled, Subdivision.unapply)
  }

  val subdivisions = TableQuery[Subdivisions]

  class Cities(tag: Tag) extends Table[City](tag, "city") {
    def id = column[Long]("id", O.PrimaryKey)
    def name = column[String]("name")
    def countryCode = column[String]("country_code")
    def country = foreignKey("fk_country", countryCode, countries)(_.code, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)
    def subdivisionId = column[Option[Long]]("subdivision_id")
    def subdivision = foreignKey("fk_subdivision", subdivisionId, subdivisions)(_.id.?, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)
    def population = column[Option[Long]]("population")
    def * = (id, name, countryCode, subdivisionId, population) <> (City.tupled, City.unapply)
  }

  val cities = TableQuery[Cities]

  class Pois(tag: Tag) extends Table[Poi](tag, "poi") {
    def id = column[Long]("id", O.PrimaryKey)
    def name = column[String]("name")
    def * = (id, name) <> (Poi.tupled, Poi.unapply)
  }

  val pois = TableQuery[Pois]


}