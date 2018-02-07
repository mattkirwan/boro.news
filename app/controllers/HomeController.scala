package controllers

import java.io.{File, PrintWriter}
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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



  def fetchLatestGazetteArticles(): String = {
    scala.io.Source.fromURL("https://www.gazettelive.co.uk/all-about/middlesbrough-fc?service=rss").mkString
  }

  def storeResponse(resp: String) = {
    val gazetteArticlesFileName: String = new SimpleDateFormat("'./data/gazette/'yyyyMMddHHmm'.txt'").format(new Date())
    val writer = new PrintWriter(new File(gazetteArticlesFileName))
    writer.write(resp)
  }

  def fetchNewArticle(d: LocalDateTime): Boolean = {
    if (LocalDateTime.now().isAfter(d.plusHours(2))) {
      return true
    }
    false
  }

  def index() = Action { implicit request: Request[AnyContent] =>

    val gazetteFiles: Array[File] = new File("./data/gazette").listFiles()

    if (gazetteFiles.isEmpty) {
      println("There are no files...updating")
      // Need to sort something out for this async call because getting latestFileName will obviously fail
      storeResponse(fetchLatestGazetteArticles())
    }

    val latestFileName = gazetteFiles
      .map(f => "[0-9]+".r findFirstIn f.toString )
      .map(f => {f.get.toLong})
      .max.toString

    val latestFileTime: LocalDateTime = LocalDateTime.parse(latestFileName, DateTimeFormatter.ofPattern("yyyyMMddHHmm"))

    val update: Boolean = LocalDateTime.now().isAfter(latestFileTime.plusMinutes(30))

    if (update) {
      println("Updating latest gazette articles...")
      storeResponse(fetchLatestGazetteArticles())
    }

    def parseXML(xml: String): Try[scala.xml.Elem] = {
      Try(scala.xml.XML.loadString(xml))
    }

    val gazetteArticles = parseXML(latestFileName) match {
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


    Ok(views.html.index(articles, ""))

  }

}



