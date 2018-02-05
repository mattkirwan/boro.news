package controllers

import java.io.{File, PrintWriter}
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import javax.inject._

import play.api._
import play.api.mvc._

import scala.util.{Failure, Success, Try}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model._

import scala.util.matching.Regex.Match

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

    // Evening Gazette
    val gazetteResponse: String = scala.io.Source.fromURL("https://www.gazettelive.co.uk/all-about/middlesbrough-fc?service=rss").mkString




    val gazetteArticlesFileName: String = new SimpleDateFormat("'./data/'yyyyMMddHHmm'_gazette.txt'").format(new Date())

    val writer = new PrintWriter(new File(gazetteArticlesFileName))

    writer.write(gazetteResponse)

    val data = new File("./data").listFiles()
      .map(f => {
        "20180205211".r findFirstIn(f.toString)
      })
      .map(f => { println(f) })


    println(LocalDateTime.now().minusHours(12))


    def parseXML(xml: String): Try[scala.xml.Elem] = {
      Try(scala.xml.XML.loadString(xml))
    }

    val gazetteArticles = parseXML(gazetteResponse) match {
      case Success(out) => for {
        a <- out \\ "item"
        t <- a \ "title"
        l <- a \ "link"
      } yield (l.text -> t.text)
      case Failure(f) => List(("error" -> f.toString))
    }

    // Northern Echo
    val URL_ECHO = "http://www.thenorthernecho.co.uk"
    val echoResponse: String = scala.io.Source.fromURL(URL_ECHO + "/sport/football/middlesbrough/").mkString



    val browser = new JsoupBrowser()
    val doc = browser.parseString(echoResponse)

    val echoArticleElems = doc >?> elementList(".nq-article-card-content a")

    val echoArticles: List[(String, String)] = echoArticleElems.get
      .map( a => ( URL_ECHO + a.attr("href"), a.text.replaceAll(".+\\: ","") ) )
      .filter(_._1.matches("^((?!\\#comments-anchor).)*"))

    val articles = echoArticles ::: gazetteArticles.toList

    // Sky Sports Videos
    val skySportsResponse: String = scala.io.Source.fromURL("http://www.skysports.com/middlesbrough-videos").mkString

    val skyBrowser = new JsoupBrowser()
    val docSky = skyBrowser.parseString(skySportsResponse)

    val beep = skySportsResponse


    Ok(views.html.index(articles, gazetteArticlesFileName))

  }

}



