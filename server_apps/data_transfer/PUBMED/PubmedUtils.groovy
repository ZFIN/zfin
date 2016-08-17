import groovy.util.slurpersupport.GPathResult

class PubmedUtils {
    static GPathResult getFromPubmed(List ids) {
        // pubmed doc says "if more than about 200 UIDs are to be provided, the request should be
        // made using the HTTP POST method" ... okay pubmed, you're such a good guy, we'll play
        // by your rules
        def url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi"
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

    static Process dbaccess (String dbname, String sql) {
        def proc = "dbaccess -a $dbname".execute()
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
}