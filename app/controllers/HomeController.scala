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

    println(xml.getClass)

    val articles: Seq[Map[String, String]]= for {
      a <- xml \\ "item"
      t <- a \ "title"
      l <- a \ "link"
    } yield Map(l.text -> t.text)

    Ok(views.html.index(articles))
  }

  def get(url: String)  = scala.io.Source.fromURL(url).mkString
}
