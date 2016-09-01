package kidstravel.client.services

import org.scalajs.dom.ext.Ajax
import upickle.default._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

case class FlickrImage(id: String, farm: Int, server: String, secret: String) {
  lazy val url = s"https://farm$farm.staticflickr.com/$server/${id}_${secret}_z.jpg"
}

case class FlickrResponseContent(photo: List[FlickrImage])

case class FlickrResponse(photos: FlickrResponseContent)

object FlickrService {

  val endpoint = "https://api.flickr.com/services/rest/"
  val apiKey = "5990f1cb36590c021fe1c450771b17ef"
  //val apiSecret = "a84fc9fd1d461bf9"

  import kidstravel.client.util.UrlBuilder._

  //def search(keyword: String): Future[FlickrImage] = Future {  FlickrImage("1", 1, "1", "1") }

  def search(keyword: String): Future[FlickrImage] = {
    val url = endpoint &
      ("method" -> "flickr.photos.search") &
      ("format" -> "json") &
      ("nojsoncallback" -> "1") &
      ("api_key" -> apiKey) &
      ("text" -> keyword)
    Ajax.get(url).map { xhr =>
      try {
        read[FlickrResponse](xhr.responseText).photos.photo.head
      } catch {
        case t @ upickle.Invalid.Data(js, msg) =>
          println(s"JSON: $js\nMsg: $msg")
          throw t
      }
    }
  }

}
