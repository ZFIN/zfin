import groovy.xml.slurpersupport.GPathResult
import groovy.xml.XmlSlurper
import org.zfin.infrastructure.TokenStorage

import java.time.LocalDate

class PubmedUtils {

    private static final int MAX_RETRIES = 5
    private static final long INITIAL_RETRY_DELAY_MS = 2000
    private static final long MAX_RETRY_DELAY_MS = 60000
    private static final Set<Integer> RETRYABLE_HTTP_CODES = [429, 500, 502, 503, 504] as Set

    private static String ncbiApiKey = null
    private static boolean apiKeyStatusLogged = false

    static String getNcbiApiKey() {
        if (ncbiApiKey == null) {
            try {
                def tokenStorage = new TokenStorage()
                Optional<String> token = tokenStorage.getValue(TokenStorage.ServiceKey.NCBI_API_TOKEN)
                if (token.isPresent()) {
                    ncbiApiKey = token.get()
                } else {
                    ncbiApiKey = ""
                }
            } catch (Exception e) {
                println("[NCBI API Key] Warning: could not read from TokenStorage: ${e.message}")
                ncbiApiKey = ""
            }
        }
        return ncbiApiKey
    }

    private static void logApiKeyStatus() {
        if (!apiKeyStatusLogged) {
            def key = getNcbiApiKey()
            if (key) {
                println("[NCBI API Key] Using API key from TokenStorage (NCBI_API_TOKEN) for eutils calls")
            } else {
                println("[NCBI API Key] WARNING: No NCBI API key found in TokenStorage. eutils calls will be unauthenticated (lower rate limits).")
                println("[NCBI API Key] To set a key, run: zfin-util token-storage write NCBI_API_TOKEN <your-key>")
            }
            println("[NCBI API Key] PMC OAI/OA endpoints do not use an API key")
            apiKeyStatusLogged = true
        }
    }

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

    /**
     * Fetch a URL with retry and exponential backoff for transient HTTP errors (429, 5xx).
     * Returns an InputStream on success, throws on exhausted retries.
     */
    static InputStream fetchWithRetry(String url, String method = "GET", String postBody = null) {
        logApiKeyStatus()
        long delay = INITIAL_RETRY_DELAY_MS
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                println("[HTTP] $method $url" + (postBody != null ? " (with POST body)" : ""))
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection()
                connection.setRequestMethod(method)
                connection.setConnectTimeout(30000)
                connection.setReadTimeout(60000)
                if (postBody != null) {
                    connection.setDoOutput(true)
                    connection.outputStream.withWriter("UTF-8") { it.write(postBody) }
                }
                int responseCode = connection.getResponseCode()
                if (responseCode >= 200 && responseCode < 300) {
                    return connection.getInputStream()
                }
                if (RETRYABLE_HTTP_CODES.contains(responseCode)) {
                    println("[Retry] HTTP $responseCode from $url (attempt $attempt/$MAX_RETRIES). Waiting ${delay}ms before retry...")
                    Thread.sleep(delay)
                    delay = Math.min(delay * 2, MAX_RETRY_DELAY_MS)
                    continue
                }
                throw new IOException("Server returned HTTP response code: $responseCode for URL: $url")
            } catch (SocketTimeoutException | ConnectException e) {
                if (attempt == MAX_RETRIES) {
                    throw e
                }
                println("[Retry] ${e.class.simpleName} for $url (attempt $attempt/$MAX_RETRIES). Waiting ${delay}ms before retry...")
                Thread.sleep(delay)
                delay = Math.min(delay * 2, MAX_RETRY_DELAY_MS)
            }
        }
        throw new IOException("Failed to fetch $url after $MAX_RETRIES attempts")
    }

    /**
     * Parse XML from a URL with retry logic.
     */
    static GPathResult parseUrlWithRetry(String url, String method = "GET", String postBody = null) {
        def inputStream = fetchWithRetry(url, method, postBody)
        getParser().parse(inputStream)
    }

    /**
     * Returns "&api_key=..." if key is available, empty string otherwise.
     */
    private static String apiKeyParam() {
        def key = getNcbiApiKey()
        return key ? "&api_key=${key}" : ""
    }

    private static final String PMC_OAI_BASE_URL = "https://pmc.ncbi.nlm.nih.gov/api/oai/v1/mh/"
    private static final String PMC_S3_BASE_URL = "https://pmc-oa-opendata.s3.amazonaws.com"

    static GPathResult getFullText(pmcId) {
        // OAI endpoint does not support api_key parameter
        def url = "${PMC_OAI_BASE_URL}?verb=GetRecord&identifier=oai:pubmedcentral.nih.gov:$pmcId&metadataPrefix=pmc"
        parseUrlWithRetry(url)
    }

    /**
     * List all files available in the PMC Open Access S3 bucket for a given PMC ID.
     * Returns a list of S3 keys for the latest version of the article.
     * PMC IDs in the DB are stored as "PMC123456"; S3 keys are "PMC123456.1/file.ext".
     */
    static List<String> listS3Files(String pmcId) {
        def allKeys = []
        String continuationToken = null

        while (true) {
            def url = "${PMC_S3_BASE_URL}/?list-type=2&prefix=${pmcId}."
            if (continuationToken) {
                url += "&continuation-token=${URLEncoder.encode(continuationToken, 'UTF-8')}"
            }
            def result = parseUrlWithRetry(url)
            result.Contents.each { content ->
                allKeys << content.Key.text()
            }
            if (result.IsTruncated.text() == 'true') {
                continuationToken = result.NextContinuationToken.text()
            } else {
                break
            }
        }

        if (allKeys.isEmpty()) {
            return []
        }

        // Find the latest version by extracting version numbers from prefixes
        def versionGroups = allKeys.groupBy { key ->
            // Extract prefix like "PMC123456.2" from "PMC123456.2/file.ext"
            key.split('/')[0]
        }
        def latestPrefix = versionGroups.keySet().sort { a, b ->
            def versionA = a.split('\\.').last() as int
            def versionB = b.split('\\.').last() as int
            versionA <=> versionB
        }.last()

        return versionGroups[latestPrefix]
    }

    /**
     * Download a file from the PMC S3 bucket to a local path.
     */
    static void downloadS3File(String s3Key, String localPath) {
        def url = "${PMC_S3_BASE_URL}/${s3Key}"
        def inputStream = fetchWithRetry(url)
        new File(localPath).withOutputStream { out ->
            out << inputStream
        }
    }

    static GPathResult getPubFromPubmed(id) {
        // pubmed doc says "if more than about 200 UIDs are to be provided, the request should be
        // made using the HTTP POST method" ... okay pubmed, you're such a good guy, we'll play
        // by your rules
        def url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi"
        def query = "db=pubmed${apiKeyParam()}&id=${id}&retmode=xml"
        parseUrlWithRetry(url, "POST", query)
    }


    static GPathResult getFromPubmed(ids) {
        // pubmed doc says "if more than about 200 UIDs are to be provided, the request should be
        // made using the HTTP POST method" ... okay pubmed, you're such a good guy, we'll play
        // by your rules
        def url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi"
        def query = "db=pubmed${apiKeyParam()}&id=${ids.join(",")}&retmode=xml"
        parseUrlWithRetry(url, "POST", query)
    }

    static Iterator<GPathResult> searchPubmed(query, daysBack = 500) {
        def url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/" +
                "esearch.fcgi?db=pubmed${apiKeyParam()}&term=${URLEncoder.encode(query, "UTF-8")}" +
                "&usehistory=y&reldate=${daysBack}&datetype=edat"
        def searchResult = parseUrlWithRetry(url)
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
            def fetchUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/" +
                    "efetch.fcgi?db=pubmed${apiKeyParam()}&query_key=${queryKey}&WebEnv=${webEnv}" +
                    "&retmode=xml&retstart=${start}&retmax=${max}"
            println("Fetching: $fetchUrl")
            def articles = parseUrlWithRetry(fetchUrl).PubmedArticle
            if (articles.size() > 0) {
                println("articleSize: " + articles.size())
                articles.each { queue.push(it) }
                start += max
                return
            }
            println("No articles returned")
            throw new RuntimeException("No articles returned from NCBI fetch")
        }
    }

        private static XmlSlurper getParser() {
            XmlSlurper parser = new XmlSlurper()
            parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
            parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
       	    return parser;
        }
}
