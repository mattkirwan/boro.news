package controllers

import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject._

import play.api.mvc._

import scala.util.{Failure, Success, Try}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._


@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>

    // Gazette Articles
    val gazetteFiles: Array[File] = new File("./data/gazette").listFiles()

    if (gazetteFiles.isEmpty || checkLatestFile("gazette")) {
      fetchAndStoreLatestArticles("gazette", "https://www.gazettelive.co.uk/all-about/middlesbrough-fc?service=rss")
    }

    val gazetteFilePath = getLatestFilename("gazette")
    val gazetteArticles = parseGazetteXml(gazetteFilePath)


    // Northern Echo Articles
    val echoFiles: Array[File] = new File("./data/echo").listFiles()

    if (echoFiles.isEmpty || checkLatestFile("echo")) {
      fetchAndStoreLatestArticles("echo", "http://www.thenorthernecho.co.uk/sport/football/middlesbrough/")
    }

    val echoFilePath = getLatestFilename("echo")
    val echoArticles = parseEchoHtml(echoFilePath)

    val articles = echoArticles ::: gazetteArticles

//    // Sky Sports Videos
//    val skySportsResponse: String = scala.io.Source.fromURL("http://www.skysports.com/middlesbrough-videos").mkString
//
//    val skyBrowser = new JsoupBrowser()
//    val docSky = skyBrowser.parseString(skySportsResponse)
//
//    val beep = skySportsResponse
    
    Ok(views.html.index(articles, ""))

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

  def fetchAndStoreLatestArticles(site: String, url: String): Unit = {
    println("Fetching and storing the latest "+site+" articles...")
    val articles = fetchURLResponse(url)
    val filename = generateNewFilename(site)
    writeArticlesToFile(filename, articles)
  }

  def getLatestFilename(site: String): String = {
    new File("./data/" + site).listFiles().last.toString
  }

  def parseGazetteXml(filePath: String): List[(String, String)] = {
    val gazetteArticles = Try(scala.xml.XML.loadFile(filePath)) match {
      case Success(out) => for {
        a <- out \\ "item"
        t <- a \ "title"
        l <- a \ "link"
      } yield l.text -> t.text
      case Failure(f) => Seq( "error" -> f.toString )
    }
    gazetteArticles.toList
  }

  def parseEchoHtml(filePath: String): List[(String, String)] = {

    val browser = new JsoupBrowser()
    val doc = browser.parseString(scala.io.Source.fromFile(filePath, "UTF-8").getLines.mkString)

    val echoArticleElems = doc >?> elementList(".nq-article-card-content a")

    echoArticleElems.get
      .map( a => ( "http://www.thenorthernecho.co.uk" + a.attr("href"), a.text.replaceAll(".+\\: ","") ) )
      .filter(_._1.matches("^((?!\\#comments-anchor).)*"))

  }

}



