import groovy.util.slurpersupport.GPathResult
import java.time.LocalDate;

class PubmedUtils {

    private static final Map<String, Integer> MONTHS = [
            'Jan': 1,
            'Feb': 2,
            'Mar': 3,
            'Apr': 4,
            'May': 5,
            'Jun': 6,
            'Jul': 7,
            'Aug': 8,
            'Sep': 9,
            'Oct': 10,
            'Nov': 11,
            'Dec': 12
    ]

    static GPathResult getFromPubmed(ids) {
        // pubmed doc says "if more than about 200 UIDs are to be provided, the request should be
        // made using the HTTP POST method" ... okay pubmed, you're such a good guy, we'll play
        // by your rules
        def url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi"
        def query = "db=pubmed&id=${ids.join(",")}&retmode=xml"
        def connection = new URL(url).openConnection()
        connection.setRequestMethod("POST")
        connection.setDoOutput(true)
        def writer = new OutputStreamWriter(connection.outputStream)
        writer.write(query)
        writer.flush()
        writer.close()
        connection.connect()
        new XmlSlurper().parse(connection.inputStream)
    }

    static Iterator<GPathResult> searchPubmed(query, daysBack = 1000) {
        def url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/" +
                "esearch.fcgi?db=pubmed&term=${URLEncoder.encode(query, "UTF-8")}" +
                "&usehistory=y&reldate=${daysBack}&datetype=edat"
        def searchResult = new XmlSlurper().parse(url)
        Integer count = searchResult.Count.toInteger()
        String queryKey = searchResult.QueryKey.text()
        String webEnv = searchResult.WebEnv.text()
        println("count: ${count}\nquery: ${queryKey}\nwebEnv: ${webEnv}")
        return new ArticleIterator(webEnv, queryKey, count)
    }

    static LocalDate getPublicationDate(GPathResult pubmedArticle) {
        // based on https://www.nlm.nih.gov/bsd/licensee/elements_article_source.html
        String pubModel = pubmedArticle.MedlineCitation.Article.@PubModel
        switch (pubModel) {
            case 'Print':
                return extractPubDate(pubmedArticle)
            case 'Print-Electronic':
                return extractPubDate(pubmedArticle)
            case 'Electronic':
                return extractArticleDate(pubmedArticle)
            case 'Electronic-Print':
                return extractArticleDate(pubmedArticle)
            case 'Electronic-eCollection':
                return extractArticleDate(pubmedArticle)
            default:
                return null
        }
    }

    static Process dbaccess (String dbname, String sql) {
        def usePostgres = System.getenv()['USE_POSTGRES']
        println usePostgres 
        sql = sql .replace("\n","")
        sql = sql .replace("\\copy","\n  \\copy")
        println sql

        def proc
        if (usePostgres == 'true')
            proc = "psql -d $dbname -a".execute()
        else
            proc = "dbaccess -a $dbname".execute()
        proc.getOutputStream().with {
            write(sql.bytes)
            close()
        }
        proc.waitFor()
        proc.getErrorStream().eachLine { println(it) }
        if (proc.exitValue()) {
            throw new RuntimeException("dbaccess call failed")
        }
        proc
    }

    private static LocalDate extractPubDate(GPathResult pubmedArticle) {
        GPathResult pubDate = pubmedArticle.MedlineCitation.Article.Journal.JournalIssue.PubDate

        GPathResult yearNode = pubDate.Year
        if (yearNode.isEmpty()) {
            String medlineDate = pubDate.MedlineDate.text()
            return parseMedlineDateString(medlineDate)
        }
        Integer year = yearNode.toInteger()

        GPathResult monthNode = pubDate.Month
        Integer month = (monthNode.isEmpty() ? 1 : MONTHS[monthNode.text()]) ?: monthNode.toInteger()

        GPathResult dayNode = pubDate.Day
        Integer day = dayNode.isEmpty() ? 1 : dayNode.toInteger()

        return LocalDate.of(year, month, day)
    }

    private static LocalDate extractArticleDate(GPathResult pubmedArticle) {
        GPathResult articleDate = pubmedArticle.MedlineCitation.Article.ArticleDate
        return LocalDate.of(
                articleDate.Year.toInteger(),
                articleDate.Month.toInteger(),
                articleDate.Day.toInteger()
        )
    }

    private static LocalDate parseMedlineDateString(String medlineDate) {
        // check for just a year
        def matches = (medlineDate =~ /^(\d{4})$/)
        if (matches.matches()) {
            int year = Integer.parseInt(matches.group(1))
            int month = 1
            int day = 1
            return LocalDate.of(year, month, day)
        }

        // check for 2012 Nov-Dec
        matches = (medlineDate =~ /^(\d{4})\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s*[-\/]\s*(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)( 1)?$/)
        if (matches.matches()) {
            int year = Integer.parseInt(matches.group(1))
            int month = MONTHS[matches.group(2)]
            int day = 1
            return LocalDate.of(year, month, day)
        }

        // check for 1999 Dec 16-30
        matches = (medlineDate =~ /^(\d{4})\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s*(\d{1,2})\s*-\s*(\d{1,2})$/)
        if (matches.matches()) {
            int year = Integer.parseInt(matches.group(1))
            int month = MONTHS[matches.group(2)]
            int day = Integer.parseInt(matches.group(3))
            return LocalDate.of(year, month, day)
        }

        // time to give up
        println("unable to parse medline date: " + medlineDate)
        return null
    }

    static class ArticleIterator implements Iterator<GPathResult> {
        private LinkedList<GPathResult> queue = new LinkedList<>()
        private String webEnv
        private String queryKey
        private int count
        private int start = 0
        private final int max = 500

        ArticleIterator(String webEnv, String queryKey, int count) {
            this.webEnv = webEnv
            this.queryKey = queryKey
            this.count = count
        }

        @Override
        boolean hasNext() {
            return !queue.isEmpty() || (start < count)
        }

        @Override
        GPathResult next() {
            if (queue.isEmpty()) {
                def fetchUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/" +
                        "efetch.fcgi?db=pubmed&query_key=${queryKey}&WebEnv=${webEnv}" +
                        "&retmode=xml&retstart=${start}&retmax=${max}"
                println(fetchUrl)
                new XmlSlurper().parse(fetchUrl).PubmedArticle.each { queue.push(it) }
                start += max
            }
            return queue.pop()
        }
    }
}
