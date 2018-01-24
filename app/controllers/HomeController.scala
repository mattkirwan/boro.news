package controllers

import javax.inject._

import play.api._
import play.api.mvc._

import scala.util.{Try, Success, Failure}

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

    val responseString: String = scala.io.Source.fromURL("https://www.gazettelive.co.uk/all-about/middlesbrough-fc?service=rss").mkString

    def parseXML(xml: String): Try[scala.xml.Elem] = {
      Try(scala.xml.XML.loadString(xml))
    }

    val articles = parseXML(responseString) match {
      case Success(out) => for {
        a <- out \\ "item"
        t <- a \ "title"
        l <- a \ "link"
      } yield Map(l.text -> t.text)
      case Failure(f) => Seq(Map("error" -> f.toString))
    }

    Ok(views.html.index(articles))

  }

}

