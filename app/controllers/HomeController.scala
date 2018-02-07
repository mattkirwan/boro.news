package controllers

import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

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

  def index() = Action { implicit request: Request[AnyContent] =>

    val files: Array[File] = new File("./data/gazette").listFiles()

    if (files.isEmpty || checkLatestFile("gazette")) {
      fetchAndStoreLatestArticles("gazette")
    }

    val filePath = getLatestFilename("gazette")

    val gazetteArticles = parseXML(filePath) match {
      case Success(out) => for {
        a <- out \\ "item"
        t <- a \ "title"
        l <- a \ "link"
      } yield l.text -> t.text
      case Failure(f) => List( "error" -> f.toString )
    }

    
//    // Northern Echo
//    val URL_ECHO = "http://www.thenorthernecho.co.uk"
//    val echoResponse: String = scala.io.Source.fromURL(URL_ECHO + "/sport/football/middlesbrough/").mkString
//
//
//
//    val browser = new JsoupBrowser()
//    val doc = browser.parseString(echoResponse)
//
//    val echoArticleElems = doc >?> elementList(".nq-article-card-content a")
//
//    val echoArticles: List[(String, String)] = echoArticleElems.get
//      .map( a => ( URL_ECHO + a.attr("href"), a.text.replaceAll(".+\\: ","") ) )
//      .filter(_._1.matches("^((?!\\#comments-anchor).)*"))
//
//    val articles = echoArticles ::: gazetteArticles.toList
//
//    // Sky Sports Videos
//    val skySportsResponse: String = scala.io.Source.fromURL("http://www.skysports.com/middlesbrough-videos").mkString
//
//    val skyBrowser = new JsoupBrowser()
//    val docSky = skyBrowser.parseString(skySportsResponse)
//
//    val beep = skySportsResponse


    Ok(views.html.index(gazetteArticles.toList, ""))

  }

  def fetchURLResponse(url: String): String = {
    scala.io.Source.fromURL(url).mkString
  }

  def writeArticlesToFile(path: String, txt: String): Unit = {
    Files.write(Paths.get(path), txt.getBytes(StandardCharsets.UTF_8))
  }

  def generateNewFilename(site: String): String = {
    new SimpleDateFormat("'./data/"+site+"/'yyyyMMddHHmm'.xml'").format(new Date())
  }

  def checkLatestFile(site: String): Boolean = {
    val latestFiles = new File("./data/" + site).listFiles()
    val latestFileName = latestFiles
      .map(f => "[0-9]+".r findFirstIn f.toString )
      .map(f => {f.get.toLong})
      .max.toString // partition here for last filename?

    val latestFileTime: LocalDateTime = LocalDateTime.parse(latestFileName, DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
    LocalDateTime.now().isAfter(latestFileTime.plusMinutes(1))
  }

  def fetchAndStoreLatestArticles(site: String): Unit = {
    println("Fetching and storing the latest "+site+" articles...")
    val articles = fetchURLResponse("https://www.gazettelive.co.uk/all-about/middlesbrough-fc?service=rss")
    val filename = generateNewFilename(site)
    writeArticlesToFile(filename, articles)
  }

  def parseXML(filePath: String): Try[scala.xml.Elem] = {
    Try(scala.xml.XML.loadFile(filePath))
  }

  def getLatestFilename(site: String): String = {
    new File("./data/" + site).listFiles().last.toString
  }

}



