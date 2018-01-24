package controllers

import javax.inject._

import play.api._
import play.api.mvc._

import scala.util.{Try, Success, Failure}

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model._

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
    // val gazetteResponse = getMockString();

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

    Ok(views.html.index(articles))

  }


  def getMockString(): String = {
    """
      |<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:mir="http://www.mirror.co.uk" xmlns:content="http://purl.org/rss/1.0/modules/content/" xmlns:media="http://search.yahoo.com/mrss/" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd"> <channel> <title>gazettelive - Sport</title>
      |<link>https://www.gazettelive.co.uk/</link>
      |<atom:link href="https://www.gazettelive.co.uk/sport/?service=rss" rel="self" type="application/rss+xml"/> <description>RSS feed from Gazette Live</description>
      |<language>en-gb</language>
      |<copyright>Trinity Mirror 2013</copyright>
      |<pubDate>Thu, 05 Oct 2017 10:58:23 GMT</pubDate>
      |<category>Sport</category>
      |<lastBuildDate>Thu, 05 Oct 2017 10:58:23 GMT</lastBuildDate>
      |<!-- Not mobile -->
      |<item>
      |<title>&#039;Pulis is close to getting Middlesbrough sorted but still needs to work out best attack&#039;</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/pulis-close-getting-middlesbrough-sorted-14193682</link>
      |<description><![CDATA[&#xa; &#x27;Everything else is falling into place&#x27;, writes Eric Paylor, but Pulis is still to suss out his best strike combination&#xa; ]]></description>
      |<author>Eric Paylor</author>
      |<pubDate>Wed, 24 Jan 2018 08:55:16 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/pulis-close-getting-middlesbrough-sorted-14193682</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Tony Pulis , Middlesbrough FC ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14183198.ece/ALTERNATES/s98/JS141344476.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14183198.ece/ALTERNATES/s615/JS141344476.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14183198.ece/ALTERNATES/s615/JS141344476.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14193682</mir:commentsID>
      |</item>
      |<item>
      |<title>Leo Percovich: &#039;Welcome back professor&#039; says Brazil club after coach returns after tragedy</title>
      |<link>https://www.gazettelive.co.uk/news/teesside-news/leo-percovich-welcome-back-professor-14193738</link>
      |<description><![CDATA[The popular former Boro coach lost his two young daughters in the crash in Brazil &#x3a;&#x3a; He has thanked Teessiders for their support]]></description>
      |<author>Mike Brown</author>
      |<pubDate>Tue, 23 Jan 2018 17:12:21 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/news/teesside-news/leo-percovich-welcome-back-professor-14193738</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Middlesbrough FC , Leo Percovich ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14048727.ece/ALTERNATES/s98/KLP_MGA_011015Leo_01JPG.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14048727.ece/ALTERNATES/s615/KLP_MGA_011015Leo_01JPG.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14048727.ece/ALTERNATES/s615/KLP_MGA_011015Leo_01JPG.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14193738</mir:commentsID>
      |</item>
      |<item>
      |<title>Middlesbrough&#039;s winners &amp; losers under Tony Pulis - and the players currently in flux</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/middlesbroughs-winners--losers-under-14193285</link>
      |<description><![CDATA[&#xa; We look at the players who&#x27;ve been rejuvenated since the Welshman&#x27;s appointment &amp; who has fallen rapidly out of favour&#xa; ]]></description>
      |<author>Jonathon Taylor</author>
      |<pubDate>Tue, 23 Jan 2018 16:23:39 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/middlesbroughs-winners--losers-under-14193285</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Ben Gibson , George Friend , Grant Leadbitter , Tony Pulis , Middlesbrough FC ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14193540.ece/ALTERNATES/s98/ecsImgGrant-Leadbitter-Adama-Traore-Ryan-Shotton-Tony-Pulis-Adam-Clayton-Patrick-Bamford-Daniel.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14193540.ece/ALTERNATES/s615/ecsImgGrant-Leadbitter-Adama-Traore-Ryan-Shotton-Tony-Pulis-Adam-Clayton-Patrick-Bamford-Daniel.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14193540.ece/ALTERNATES/s615/ecsImgGrant-Leadbitter-Adama-Traore-Ryan-Shotton-Tony-Pulis-Adam-Clayton-Patrick-Bamford-Daniel.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14193285</mir:commentsID>
      |</item>
      |<item>
      |<title>Footage of Boro fans&#039; fundraising trip to cash-strapped Hartlepool set for TV airing</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/footage-boro-fans-fundraising-trip-14192529</link>
      |<description><![CDATA[&#xa; Boro fans turned out en masse at crisis club Pools for the clash with Wrexham - and the day was captured on camera&#xa; ]]></description>
      |<author>Anthony Vickers</author>
      |<pubDate>Tue, 23 Jan 2018 16:22:17 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/footage-boro-fans-fundraising-trip-14192529</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Middlesbrough FC , Hartlepool United FC ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14182437.ece/ALTERNATES/s98/IMP_MGA_200118mgahartlepool_21JPG.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14182437.ece/ALTERNATES/s615/IMP_MGA_200118mgahartlepool_21JPG.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14182437.ece/ALTERNATES/s615/IMP_MGA_200118mgahartlepool_21JPG.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14192529</mir:commentsID>
      |</item>
      |<item>
      |<title>Middlesbrough transfer rumours: Nottingham Forest&#039;s Clayton hope, Blackburn&#039;s Bradley Dack denial, Leeds plot surprise reunion</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/middlesbrough-transfer-rumours-nottingham-forests-14192206</link>
      |<description><![CDATA[&#xa; The latest on Ashley Fletcher also features in today&#x27;s round-up, while a former Boro man has reportedly been told he&#x27;s free to find a new club&#xa; ]]></description>
      |<author>Dominic Shaw</author>
      |<pubDate>Tue, 23 Jan 2018 14:39:56 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/middlesbrough-transfer-rumours-nottingham-forests-14192206</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Tony Pulis , Middlesbrough FC , Transfer deadline day , Blackburn Rovers FC , Middlesbrough FC Transfer News ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14192698.ece/ALTERNATES/s98/JS127998406.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14192698.ece/ALTERNATES/s615/JS127998406.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14192698.ece/ALTERNATES/s615/JS127998406.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14192206</mir:commentsID>
      |</item>
      |<item>
      |<title>Adam Clayton &#039;top of Nottingham Forest&#039;s wish list&#039;: 5 key questions on Middlesbrough midfielder&#039;s future</title>
      |<link>https://www.gazettelive.co.uk/sport/football/transfer-news/adam-clayton-top-nottingham-forests-14191499</link>
      |<description><![CDATA[The Boro midfielder remains top of Nottingham Forest&#x27;s list of potential January signings&#x3a;&#x3a; These points could hold the key]]></description>
      |<author>Jonathon Taylor</author>
      |<pubDate>Tue, 23 Jan 2018 12:34:46 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/transfer-news/adam-clayton-top-nottingham-forests-14191499</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Aitor Karanka , George Friend , Grant Leadbitter , Steve Gibson , Tony Pulis , Adam Clayton , Middlesbrough FC , Middlesbrough FC Transfer News , Nottingham Forest FC ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14191641.ece/ALTERNATES/s98/The-EFL-Sky-Bet-Championship-Barnsley-v-Middlesbrough-Saturday-14th-October-2017-Oakwell-Bar.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14191641.ece/ALTERNATES/s615/The-EFL-Sky-Bet-Championship-Barnsley-v-Middlesbrough-Saturday-14th-October-2017-Oakwell-Bar.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14191641.ece/ALTERNATES/s615/The-EFL-Sky-Bet-Championship-Barnsley-v-Middlesbrough-Saturday-14th-October-2017-Oakwell-Bar.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14191499</mir:commentsID>
      |</item>
      |<item>
      |<title>Middlesbrough transfers: Tony Pulis faces loan predicament but Boro&#039;s season doesn&#039;t hinge on January arrivals</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/middlesbrough-transfers-tony-pulis-faces-14190681</link>
      |<description><![CDATA[&#xa; In an ideal world, Pulis wants to bring in a temporary signing or two this month, but how will he approach the loan market&#x3f;&#xa; ]]></description>
      |<author>Dominic Shaw</author>
      |<pubDate>Tue, 23 Jan 2018 11:22:58 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/middlesbrough-transfers-tony-pulis-faces-14190681</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Tony Pulis , Middlesbrough FC , Middlesbrough FC Transfer News , Transfer deadline day ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14191118.ece/ALTERNATES/s98/JS141344455.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14191118.ece/ALTERNATES/s615/JS141344455.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14191118.ece/ALTERNATES/s615/JS141344455.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14190681</mir:commentsID>
      |</item>
      |<item>
      |<title>Adam Forshaw exit is the latest evidence of Middlesbrough&#039;s new-found transfer negotiating skills</title>
      |<link>https://www.gazettelive.co.uk/sport/football/transfer-news/adam-forshaw-exit-latest-evidence-14188196</link>
      |<description><![CDATA[Anthony Vickers looks at how Boro are regularly recouping profits on players they are happy to see leave the club]]></description>
      |<author>Anthony Vickers</author>
      |<pubDate>Tue, 23 Jan 2018 06:00:00 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/transfer-news/adam-forshaw-exit-latest-evidence-14188196</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Middlesbrough FC , Middlesbrough FC Transfer News , Adam Forshaw ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14189238.ece/ALTERNATES/s98/KLP_MGA_120817mgaBoro_32JPG.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14189238.ece/ALTERNATES/s615/KLP_MGA_120817mgaBoro_32JPG.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14189238.ece/ALTERNATES/s615/KLP_MGA_120817mgaBoro_32JPG.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14188196</mir:commentsID>
      |</item>
      |<item>
      |<title>Middlesbrough legend Alan Peacock in touching tribute to former team-mate Billy Day</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/middlesbrough-legend-alan-peacock-emotional-14188968</link>
      |<description><![CDATA[&#x27;Me and Brian Clough got a lot of goals - but Billy made it easy for us,&#x27; says striker Alan Peacock after sad death of former team-mate]]></description>
      |<author>Anthony Vickers</author>
      |<pubDate>Mon, 22 Jan 2018 16:57:19 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/middlesbrough-legend-alan-peacock-emotional-14188968</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Middlesbrough FC ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14188567.ece/ALTERNATES/s98/Billy-Day.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14188567.ece/ALTERNATES/s615/Billy-Day.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14188567.ece/ALTERNATES/s615/Billy-Day.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14188968</mir:commentsID>
      |</item>
      |<item>
      |<title>The 18 Middesbrough players who have got on the score sheet so far this season</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/18-middesbrough-players-who-score-14186371</link>
      |<description><![CDATA[&#xa; George Friend&#x27;s Rangers rocket and Adama Traore&#x27;s welcome first strike add up to a healthy spread of scorers&#xa; ]]></description>
      |<author>Anthony Vickers</author>
      |<pubDate>Mon, 22 Jan 2018 16:49:33 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/18-middesbrough-players-who-score-14186371</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Middlesbrough FC ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14183275.ece/ALTERNATES/s98/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14183275.ece/ALTERNATES/s615/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14183275.ece/ALTERNATES/s615/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" type=""
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14186371</mir:commentsID>
      |</item>
      |<item>
      |<title>Tributes paid to Billy Day - Boro&#039;s &#039;flying winger&#039; and former bookmaker</title>
      |<link>https://www.gazettelive.co.uk/news/teesside-news/tributes-paid-billy-day-boros-14188430</link>
      |<description><![CDATA[&#xa; Billy Day was part of the famous &#x27;&pound;50 forward line&#x27; with Alan Peacock, Brian Clough, Derek McLean and Edwin Holliday&#xa; ]]></description>
      |<author>Mike Brown</author>
      |<pubDate>Mon, 22 Jan 2018 16:00:36 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/news/teesside-news/tributes-paid-billy-day-boros-14188430</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Ayresome Park , South Bank , Middlesbrough , Middlesbrough FC ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14188565.ece/ALTERNATES/s98/Billy-Day.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14188565.ece/ALTERNATES/s615/Billy-Day.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14188565.ece/ALTERNATES/s615/Billy-Day.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14188430</mir:commentsID>
      |</item>
      |<item>
      |<title>Middlesbrough transfer rumours: Bradley Dack link, Adam Clayton &amp; Ashley Fletcher latest, McGinn&#039;s contract claim</title>
      |<link>https://www.gazettelive.co.uk/sport/football/transfer-news/middlesbrough-transfer-rumours-bradley-dack-14187857</link>
      |<description><![CDATA[&#xa; The latest on the transfer front as Boro are linked with an in-form Blackburn midfielder &amp; interest hots up in two fringe figures&#xa; ]]></description>
      |<author>Jonathon Taylor</author>
      |<pubDate>Mon, 22 Jan 2018 14:45:31 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/transfer-news/middlesbrough-transfer-rumours-bradley-dack-14187857</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Tony Mowbray , Tony Pulis , Aitor Karanka , Middlesbrough FC , Transfer deadline day , Blackburn Rovers FC , Middlesbrough FC Transfer News ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14187908.ece/ALTERNATES/s98/GettyImages-897705302.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14187908.ece/ALTERNATES/s615/GettyImages-897705302.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14187908.ece/ALTERNATES/s615/GettyImages-897705302.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14187857</mir:commentsID>
      |</item>
      |<item>
      |<title>Table-topping &#039;key starter&#039; Gaston Ramirez has a new adoring fanbase at Sampdoria</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/table-topping-key-starter-gaston-14177611</link>
      |<description><![CDATA[&#xa; Serie A broadcaster Matteo Bonetti tells Dominic Shaw how Ramirez is faring back in Italy with Sampdoria&#xa; ]]></description>
      |<author>Dominic Shaw</author>
      |<pubDate>Mon, 22 Jan 2018 14:26:11 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/table-topping-key-starter-gaston-14177611</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Gaston Ramirez , Middlesbrough FC , Middlesbrough FC Transfer News ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14177205.ece/ALTERNATES/s98/Gaston-Ramirez.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14177205.ece/ALTERNATES/s615/Gaston-Ramirez.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14177205.ece/ALTERNATES/s615/Gaston-Ramirez.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14177611</mir:commentsID>
      |</item>
      |<item>
      |<title>FAQs on The Gazette&#039;s situation with Middlesbrough FC</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/faqs-gazettes-situation-middlesbrough-fc-14185962</link>
      |<description><![CDATA[&#xa; Boro imposed reporting restrictions on two Gazette journalists in July 2017&#xa; ]]></description>
      |<author> gazettelive.co.uk</author>
      |<pubDate>Mon, 22 Jan 2018 14:01:15 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/faqs-gazettes-situation-middlesbrough-fc-14185962</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Middlesbrough FC ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14186052.ece/ALTERNATES/s98/Middlesbrough-v-Sunderland-FA-Cup-Third-Round-Riverside-Stadium.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14186052.ece/ALTERNATES/s615/Middlesbrough-v-Sunderland-FA-Cup-Third-Round-Riverside-Stadium.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14186052.ece/ALTERNATES/s615/Middlesbrough-v-Sunderland-FA-Cup-Third-Round-Riverside-Stadium.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14185962</mir:commentsID>
      |</item>
      |<item>
      |<title>Leo Percovich pens emotional poem dedicated to daughters killed in tragic car crash</title>
      |<link>https://www.gazettelive.co.uk/news/teesside-news/leo-percovich-pens-emotional-poem-14187391</link>
      |<description><![CDATA[Former Middlesbrough goalkeeping coach Leo Percovich also shared a family photograph of his children]]></description>
      |<author>Toni Guillot</author>
      |<pubDate>Mon, 22 Jan 2018 13:46:44 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/news/teesside-news/leo-percovich-pens-emotional-poem-14187391</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Leo Percovich , Middlesbrough FC ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14187572.ece/ALTERNATES/s98/TGR_MGA_220118mgaleo.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14187572.ece/ALTERNATES/s615/TGR_MGA_220118mgaleo.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14187572.ece/ALTERNATES/s615/TGR_MGA_220118mgaleo.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14187391</mir:commentsID>
      |</item>
      |<item>
      |<title>Daniel Ayala&#039;s Middlesbrough turnaround, team spirit rekindled &amp; Martin Braithwaite&#039;s worry: 3 up, 3 down</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/daniel-ayalas-middlesbrough-turnaround-team-14187140</link>
      |<description><![CDATA[&#xa; We look at the main winners and losers from the weekend which includes Dael Fry&#x27;s tricky predicament &amp; d&eacute;j&agrave; vu at Fulham&#xa; ]]></description>
      |<author>Jonathon Taylor</author>
      |<pubDate>Mon, 22 Jan 2018 12:58:52 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/daniel-ayalas-middlesbrough-turnaround-team-14187140</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Aitor Karanka , Ben Gibson , George Friend , Tony Pulis , Middlesbrough FC , Fulham FC , Daniel Ayala , Martin Braithwaite , Dael Fry ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14187138.ece/ALTERNATES/s98/Thomas-Christiansen-Martin-Braithwaite-Ayala-Dael-Fry-Aitor-Karanka.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14187138.ece/ALTERNATES/s615/Thomas-Christiansen-Martin-Braithwaite-Ayala-Dael-Fry-Aitor-Karanka.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14187138.ece/ALTERNATES/s615/Thomas-Christiansen-Martin-Braithwaite-Ayala-Dael-Fry-Aitor-Karanka.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14187140</mir:commentsID>
      |</item>
      |<item>
      |<title>Adama Traore beats Messi in stat, a growling defence and Rudy Gestede&#039;s flick on</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/adama-traore-beats-messi-stat-14185430</link>
      |<description><![CDATA[&#xa; Now the dust has settled on Boro&#x27;s win at Loftus Road, here&#x27;s a few things that may have been missed&#xa; ]]></description>
      |<author>Anthony Vickers</author>
      |<pubDate>Mon, 22 Jan 2018 12:06:28 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/adama-traore-beats-messi-stat-14185430</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Tony Pulis , Middlesbrough FC , Queens Park Rangers FC , Adama Traore ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14186752.ece/ALTERNATES/s98/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14186752.ece/ALTERNATES/s615/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14186752.ece/ALTERNATES/s615/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" type=""
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14185430</mir:commentsID>
      |</item>
      |<item>
      |<title>Middlesbrough&#039;s &#039;ideal combination&#039;, Traore tipped for top &amp; play-off prediction: QPR view on Pulis&#039; Boro</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/middlesbroughs-ideal-combination-traore-tipped-14186612</link>
      |<description><![CDATA[&#xa; Boro delivered what has been described as &#x27;pretty much the complete performance&#x27; at Loftus Road&#xa; ]]></description>
      |<author>Dominic Shaw</author>
      |<pubDate>Mon, 22 Jan 2018 11:48:29 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/middlesbroughs-ideal-combination-traore-tipped-14186612</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Middlesbrough FC , Tony Pulis , Adama Traore ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14186632.ece/ALTERNATES/s98/JS141355309.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14186632.ece/ALTERNATES/s615/JS141355309.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14186632.ece/ALTERNATES/s615/JS141355309.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14186612</mir:commentsID>
      |</item>
      |<item>
      |<title>Middlesbrough transfers: Tony Pulis prepared to play waiting game as January deadline looms large</title>
      |<link>https://www.gazettelive.co.uk/sport/football/transfer-news/middlesbrough-transfers-tony-pulis-prepared-14185776</link>
      |<description><![CDATA[&#xa; The Boro boss hopes to bring in a couple of new faces on loan before the end of the month, but has reiterated his spending stance&#xa; ]]></description>
      |<author>Jonathon Taylor</author>
      |<pubDate>Mon, 22 Jan 2018 09:42:21 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/transfer-news/middlesbrough-transfers-tony-pulis-prepared-14185776</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Aitor Karanka , Steve Gibson , FA Cup , Tony Pulis , Middlesbrough FC , Middlesbrough FC Transfer News ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14185862.ece/ALTERNATES/s98/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14185862.ece/ALTERNATES/s615/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14185862.ece/ALTERNATES/s615/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" type=""
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14185776</mir:commentsID>
      |</item>
      |<item>
      |<title>Boro fan charged after claims he &#039;urinated in QPR goalkeeper&#039;s water bottle&#039;</title>
      |<link>https://www.gazettelive.co.uk/news/teesside-news/boro-fan-charged-after-claims-14183860</link>
      |<description><![CDATA[Met Police tweeted a man will appear in court over the alleged incident during Boro&#x27;s 3-0 win over London club]]></description>
      |<author>Ian Johnson</author>
      |<pubDate>Sun, 21 Jan 2018 13:13:21 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/news/teesside-news/boro-fan-charged-after-claims-14183860</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Cleveland Police , Middlesbrough FC , Queens Park Rangers FC ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14183953.ece/ALTERNATES/s98/Sport.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14183953.ece/ALTERNATES/s615/Sport.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14183953.ece/ALTERNATES/s615/Sport.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14183860</mir:commentsID>
      |</item>
      |<item>
      |<title>&#039;Middlesbrough finally have the old George Friend back after months of anguish in the darkness&#039;</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/middlesbrough-finally-old-george-friend-14182990</link>
      |<description><![CDATA[&#xa; The Boro boss used one word to describe Friend&#x27;s state upon taking charge&#x3a;&#x3a; This now feels like his second coming&#xa; ]]></description>
      |<author>Jonathon Taylor</author>
      |<pubDate>Sun, 21 Jan 2018 09:42:34 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/middlesbrough-finally-old-george-friend-14182990</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[George Friend , Tony Pulis , Middlesbrough FC , Queens Park Rangers FC ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14183275.ece/ALTERNATES/s98/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14183275.ece/ALTERNATES/s615/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14183275.ece/ALTERNATES/s615/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" type=""
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14182990</mir:commentsID>
      |</item>
      |<item>
      |<title>Middlesbrough take heed of warning &amp; coaching beats buying for Tony Pulis: What we learnt</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/middlesbrough-take-heed-warning--14183199</link>
      |<description><![CDATA[Dominic Shaw&#x27;s analysis of Boro&#x27;s win at QPR includes Pulis&#x27; team showing two sides to their game &amp; the need for this level of display to become the norm]]></description>
      |<author>Dominic Shaw</author>
      |<pubDate>Sun, 21 Jan 2018 08:42:10 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/middlesbrough-take-heed-warning--14183199</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Tony Pulis , Middlesbrough FC , Queens Park Rangers FC ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14183212.ece/ALTERNATES/s98/JS141349154-1.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14183212.ece/ALTERNATES/s615/JS141349154-1.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14183212.ece/ALTERNATES/s615/JS141349154-1.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14183199</mir:commentsID>
      |</item>
      |<item>
      |<title>The Anthony Vickers verdict on how Middlesbrough are reaping rewards of Pulis keeping things simple</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/anthony-vickers-verdict-how-middlesbrough-14183194</link>
      |<description><![CDATA[The new boss has stripped away layers of tactical confusion to produce a Boro with a clear shape and purpose, writes Anthony Vickers]]></description>
      |<author>Anthony Vickers</author>
      |<pubDate>Sun, 21 Jan 2018 07:00:18 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/anthony-vickers-verdict-how-middlesbrough-14183194</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Tony Pulis , Middlesbrough FC ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14183196.ece/ALTERNATES/s98/JS141349159.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14183196.ece/ALTERNATES/s615/JS141349159.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14183196.ece/ALTERNATES/s615/JS141349159.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14183194</mir:commentsID>
      |</item>
      |<item>
      |<title>The Loftus Road buzz surrounding Adama Traore &amp; Tony Pulis&#039; belief in Middlesbrough&#039;s matchwinning winger</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/loftus-road-buzz-surrounding-adama-14182374</link>
      |<description><![CDATA[&#xa; Traore took centre-stage on and off the pitch at QPR, writes Dominic Shaw &#x3a;&#x3a; The winger scored his first goal for the club in the 3-0 win&#xa; ]]></description>
      |<author>Dominic Shaw</author>
      |<pubDate>Sat, 20 Jan 2018 18:45:41 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/loftus-road-buzz-surrounding-adama-14182374</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Tony Pulis , Middlesbrough FC , Adama Traore ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14182497.ece/ALTERNATES/s98/JS141355300.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14182497.ece/ALTERNATES/s615/JS141355300.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14182497.ece/ALTERNATES/s615/JS141355300.jpg" type="image/jpeg"
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14182374</mir:commentsID>
      |</item>
      |<item>
      |<title>&#039;Tony Pulis&#039; Middlesbrough are only going one way&#039; - QPR manager hails Boro &amp; Traore</title>
      |<link>https://www.gazettelive.co.uk/sport/football/football-news/tony-pulis-middlesbrough-only-going-14182485</link>
      |<description><![CDATA[The QPR boss heaped the praise on Boro after the visitors cruised to a stunning victory at Loftus Road]]></description>
      |<author>Jonathon Taylor</author>
      |<pubDate>Sat, 20 Jan 2018 18:28:48 GMT</pubDate>
      |<guid>https://www.gazettelive.co.uk/sport/football/football-news/tony-pulis-middlesbrough-only-going-14182485</guid>
      |<category>Sport</category>
      |<media:keywords><![CDATA[Boro Live , Middlesbrough FC , Tony Pulis , Queens Park Rangers FC , George Friend , Mark Hughes , Loftus , Saltburn and East Cleveland ]]></media:keywords>
      |<media:thumbnail url="https://i2-prod.gazettelive.co.uk/incoming/article14182490.ece/ALTERNATES/s98/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" width="96"
      |height="98"/>
      |<enclosure url="https://i2-prod.gazettelive.co.uk/incoming/article14182490.ece/ALTERNATES/s615/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" />
      |<media:content url="https://i2-prod.gazettelive.co.uk/incoming/article14182490.ece/ALTERNATES/s615/Queens-Park-Rangers-v-Middlesbrough-Sky-Bet-Championship.jpg" type=""
      |width="615" height="409" />
      |<mir:hascomments>false</mir:hascomments>
      |<mir:commentsID>gazettelive-14182485</mir:commentsID>
      |</item>
      |</channel>
      |</rss>
    """.stripMargin
  }

}



