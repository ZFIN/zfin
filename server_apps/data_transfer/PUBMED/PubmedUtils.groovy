import groovy.util.slurpersupport.GPathResult

import java.time.LocalDate
import java.util.zip.GZIPInputStream

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


    static GPathResult getFullText(pmcId) {
        def url = "https://www.ncbi.nlm.nih.gov/pmc/oai/oai.cgi?verb=GetRecord&identifier=oai:pubmedcentral.nih.gov:$pmcId&metadataPrefix=pmc"
        getParser().parse(url)
    }

    static GPathResult getPdfMetaDataRecord(pmcId){
        def url = "https://www.ncbi.nlm.nih.gov/pmc/utils/oa/oa.fcgi"
        def query = "id="+pmcId
        def connection = new URL(url).openConnection()
        connection.setDoOutput(true)
        connection.connect()
        def writer = new OutputStreamWriter(connection.outputStream)
        writer.write(query)
        writer.flush()
        writer.close()
        connection.connect()
        getParser().parse(connection.inputStream)
    }

//
//    static GPathResult getPDFandImagesTarballsByDate(date) {
//        def url = "https://www.ncbi.nlm.nih.gov/pmc/utils/oa/oa.fcgi"
//        def query = "from="+date
//        def connection = new URL(url).openConnection()
//        connection.setDoOutput(true)
//        connection.connect()
//        def writer = new OutputStreamWriter(connection.outputStream)
//        writer.write(query)
//        writer.flush()
//        writer.close()
//        connection.connect()
//        new XmlSlurper().parse(connection.inputStream)
//    }
//
//    static GPathResult getResumptionSet(token) {
//        def url = "https://www.ncbi.nlm.nih.gov/pmc/utils/oa/oa.fcgi"
//        def query = "resumptionToken="+token
//        def connection = new URL(url).openConnection()
//        connection.setDoOutput(true)
//        connection.connect()
//        def writer = new OutputStreamWriter(connection.outputStream)
//        writer.write(query)
//        writer.flush()
//        writer.close()
//        connection.connect()
//        new XmlSlurper().parse(connection.inputStream)
//    }

    static gunzip(String file_input, String file_output) {
        FileInputStream fis = new FileInputStream(file_input)
        FileOutputStream fos = new FileOutputStream(file_output)
        GZIPInputStream gzis = new GZIPInputStream(fis)
        byte[] buffer = new byte[1024]
        int len = 0

        while ((len = gzis.read(buffer)) > 0) {
            fos.write(buffer, 0, len)
        }
        fos.close()
        fis.close()
        gzis.close()
    }

    static GPathResult getPubFromPubmed(id) {
        // pubmed doc says "if more than about 200 UIDs are to be provided, the request should be
        // made using the HTTP POST method" ... okay pubmed, you're such a good guy, we'll play
        // by your rules
        def url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi"
        def query = "db=pubmed&api_key=47c9eadd39b0bcbfac58e3e911930d143109&id=${id}&retmode=xml"
        def connection = new URL(url).openConnection()
        connection.setRequestMethod("POST")
        connection.setDoOutput(true)
        def writer = new OutputStreamWriter(connection.outputStream)
        writer.write(query)
        writer.flush()
        writer.close()
        connection.connect()
        getParser().parse(connection.inputStream)
    }


    static GPathResult getFromPubmed(ids) {
        // pubmed doc says "if more than about 200 UIDs are to be provided, the request should be
        // made using the HTTP POST method" ... okay pubmed, you're such a good guy, we'll play
        // by your rules
        def url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi"
        def query = "db=pubmed&api_key=47c9eadd39b0bcbfac58e3e911930d143109&id=${ids.join(",")}&retmode=xml"

        def connection = new URL(url).openConnection()
        connection.setRequestMethod("POST")
        connection.setDoOutput(true)
        def writer = new OutputStreamWriter(connection.outputStream)
        writer.write(query)
        writer.flush()
        writer.close()
        connection.connect()
        getParser().parse(connection.inputStream)
    }

    static Iterator<GPathResult> searchPubmed(query, daysBack = 500) {
        def url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/" +
                "esearch.fcgi?db=pubmed&api_key=47c9eadd39b0bcbfac58e3e911930d143109&term=${URLEncoder.encode(query, "UTF-8")}" +
                "&usehistory=y&reldate=${daysBack}&datetype=edat"
        def searchResult = getParser().parse(url)
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

    static Boolean pubIsActive(GPathResult pubmedArticle) {
        String pubStatus = pubmedArticle.PubmedData.PublicationStatus
        if (pubStatus =='ppublish' || pubStatus == 'epublish'){
            return true
        }
        else {
            return false
        }
    }


    static Process dbaccess(String dbname, String sql) {
        sql = sql.replace("\n", "")
        sql = sql.replace("\\copy", "\n  \\copy")
        println sql

        def proc
        proc = "psql -d $dbname -a".execute()
        proc.getOutputStream().with {
            write(sql.bytes)
            close()
        }
        proc.waitFor()
        proc.getErrorStream().eachLine { println(it) }
        if (proc.exitValue()) {
            throw new RuntimeException("psql call failed")
        }
        proc
    }

    static Process psql(String dbname, String sql) {
        return dbaccess(dbname, sql)
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
        private final int max = 1000

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
                fetch()
            }
            return queue.pop()
        }

        private void fetch() {
            def attempt = 0
            def fetchUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/" +
                    "efetch.fcgi?db=pubmed&api_key=47c9eadd39b0bcbfac58e3e911930d143109&query_key=${queryKey}&WebEnv=${webEnv}" +
                    "&retmode=xml&retstart=${start}&retmax=${max}"
            while (attempt < 3) {
                attempt += 1
                println("attempt" + "$attempt: $fetchUrl")
                try {
                    def articles = getParser().parse(fetchUrl).PubmedArticle
                    if (articles.size() > 0) {
                        println("articleSize: " + articles.size())
                        articles.each { queue.push(it) }
                        start += max
                        return
                    }
                    println("No articles")
                } catch (IOException ignore) {
                    println("Caught IOException!")
                }
            }
            println("fetching done")
            throw new RuntimeException("Giving up after 3 attempt to fetch from NCBI")
        }
    }

        private static XmlSlurper getParser() {
            XmlSlurper parser = new XmlSlurper()
            parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
            parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
       	    return parser;
        }
}
