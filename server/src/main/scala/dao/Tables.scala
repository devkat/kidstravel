package dao

import kidstravel.shared.geo.{City, Country, Subdivision}
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
    def subdivisionId = column[Long]("subdivision_id")
    def subdivision = foreignKey("fk_subdivision", subdivisionId, subdivisions)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)
    def * = (id, name, countryCode, subdivisionId) <> (City.tupled, City.unapply)
  }

  val cities = TableQuery[Cities]

}