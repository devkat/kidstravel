package kidstravel.client.util

import scala.language.implicitConversions

case class UrlParamKey(key: String) {
  def ->(value: String) = UrlParam(key, value)
}

case class UrlParam(key: String, value: String)

case class UrlBuilder(baseUrl: String, params: Seq[UrlParam]) {

  def &(param: UrlParam) = this.copy(params = this.params :+ param)

}

object UrlBuilder {

  implicit def toKey(key: String): UrlParamKey =
    UrlParamKey(key)

  implicit def fromBase(base: String): UrlBuilder =
    UrlBuilder(base, Seq.empty)

  implicit def urlBuilderToString(urlBuilder: UrlBuilder): String = {
    val paramStr = urlBuilder.params.map(p => s"${p.key}=${p.value}").mkString("&")
    val query = if (paramStr.isEmpty) "" else s"?$paramStr"
    s"${urlBuilder.baseUrl}$query"
  }

}
