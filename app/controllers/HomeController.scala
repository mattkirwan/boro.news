package controllers

import javax.inject._

import play.api._
import play.api.mvc._



/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>

    val responseString: String = get("http://www.gazettelive.co.uk/all-about/middlesbrough-fc?service=rss")

    val xml = scala.xml.XML.loadString(responseString)
    val rss: String = xml.mkString

    val articleTitles = for {
      t <- xml \\ "item" \ "title"
    } yield t.text

    Ok(views.html.index(articleTitles))
  }

  def get(url: String)  = scala.io.Source.fromURL(url).mkString
}



//object MyHttp extends Http {
//  proxyConfig = None,
//  options = HttpConstants.defaultOptions,
//  charset = HttpConstants.utf8,
//  sendBufferSize = 4096,
//  userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:57.0) Gecko/20100101 Firefox/57.0",
//  compress = true
//}