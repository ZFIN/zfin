package org.zfin.util

import org.zfin.AbstractZfinIntegrationSpec
import org.zfin.properties.ZfinPropertiesEnum
import spock.lang.Shared
import spock.lang.Unroll

import java.nio.file.Files

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.expect

class ReportGeneratorSpec extends AbstractZfinIntegrationSpec {

    @Shared ReportGenerator rg
    @Shared StringWriter out
    @Shared ReportGenerator.Format html = ReportGenerator.Format.HTML
    @Shared ReportGenerator.Format txt = ReportGenerator.Format.TXT

    def setup() {
        rg = new ReportGenerator()
        out = new StringWriter()
    }

    @Unroll
    def "#format: empty report"() {
        when:
        rg.write(out, format)
        String report = out.toString()

        then:
        expect report, containsAllStrings(expectedStrings)
        expect report, notContainsAllStrings(unexpectedStrings)

        where:
        format || expectedStrings                | unexpectedStrings
        html   || ["<html>", "<head>", "<body>"] | ["<h1>"]
        txt    || []                             | ["=", '-']
    }

    @Unroll
    def "#format: empty report with title"() {
        when:
        rg.setReportTitle("Hello report")
        rg.write(out, format)
        String report = out.toString()

        then:
        expect report, containsAllStrings(expectedStrings)

        where:
        format || expectedStrings
        html   || ["<title>Hello report</title>", "<h1>Hello report</h1>"]
        txt    || ["Hello report", "============"]
    }

    @Unroll
    def "#format: include timestamp"() {
        when:
        rg.includeTimestamp()
        rg.write(out, format)
        String report = out.toString()

        then:
        report =~ /Report generated \d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/

        where:
        format << [html, txt]
    }

    @Unroll
    def "#format: intro paragraphs"() {
        when:
        rg.addIntroParagraph("first paragraph")
        rg.addIntroParagraph("another paragraph")
        rg.write(out, format)
        String report = out.toString()

        then:
        expect report, containsAllStrings(expectedStrings)

        where:
        format || expectedStrings
        html   || ["<p class=\"intro\">first paragraph</p>", "<p class=\"intro\">another paragraph</p>"]
        txt    || ["first paragraph\n", "another paragraph\n"]
    }

    @Unroll
    def "#format: data table"() {
        when:
        String caption = "my data!"
        List<String> header = ["One", "Two", "Three"]
        List<List<String>> data = [["1", "2", "3"], ["a", "b", "c"]]
        rg.addDataTable(caption, header, data)
        rg.write(out, format)
        String report = out.toString()
        String normalizedSpaceReport = report.replaceAll("[ \n]+", " ")

        then:
        expect normalizedSpaceReport, containsAllStrings(expectedStrings)

        where:
        format || expectedStrings
        html   || ["<h2>my data!</h2>",
                   '<table class="result rowstripes">',
                   "<tr> <th>One</th> <th>Two</th> <th>Three</th> </tr>",
                   "<tr> <td> 1 </td> <td> 2 </td> <td> 3 </td> </tr>",
                   "<tr> <td> a </td> <td> b </td> <td> c </td> </tr>"]
        txt    || ["== my data! ==",
                   "One\tTwo\tThree",
                   "1\t2\t3",
                   "a\tb\tc"]
    }

    @Unroll
    def "#format: data table without caption"() {
        when:
        List<String> header = ["One", "Two", "Three"]
        List<List<String>> data = [["1", "2", "3"], ["a", "b", "c"]]
        rg.addDataTable(header, data)
        rg.write(out, format)
        String report = out.toString()
        String normalizedSpaceReport = report.replaceAll("[ \n]+", " ")

        then:
        expect normalizedSpaceReport, containsAllStrings(expectedStrings)
        expect normalizedSpaceReport, notContainsAllStrings(unexpectedStrings)

        where:
        format || unexpectedStrings | expectedStrings
        html   || ["<h2>"]          | ['<table class="result rowstripes">',
                                       "<tr> <th>One</th> <th>Two</th> <th>Three</th> </tr>",
                                       "<tr> <td> 1 </td> <td> 2 </td> <td> 3 </td> </tr>",
                                       "<tr> <td> a </td> <td> b </td> <td> c </td> </tr>"]
        txt    || ["=="]            | ["One\tTwo\tThree",
                                       "1\t2\t3",
                                       "a\tb\tc"]
    }

    @Unroll
    def "#format: data table without header"() {
        when:
        String caption = "my data!"
        List<List<String>> data = [["1", "2", "3"], ["a", "b", "c"]]
        rg.addDataTable(caption, data)
        rg.write(out, format)
        String report = out.toString()
        String normalizedSpaceReport = report.replaceAll("[ \n]+", " ")

        then:
        expect normalizedSpaceReport, containsAllStrings(expectedStrings)
        expect normalizedSpaceReport, notContainsAllStrings(unexpectedStrings)

        where:
        format || unexpectedStrings | expectedStrings
        html   || ["<th>"]          | ["<h2>my data!</h2>",
                                       '<table class="result rowstripes">',
                                       "<tr> <td> 1 </td> <td> 2 </td> <td> 3 </td> </tr>",
                                       "<tr> <td> a </td> <td> b </td> <td> c </td> </tr>"]
        txt    || []                | ["1\t2\t3",
                                       "a\tb\tc"]
    }

    @Unroll
    def "#format: data table only data"() {
        when:
        List<List<String>> data = [["1", "2", "3"], ["a", "b", "c"]]
        rg.addDataTable(data)
        rg.write(out, format)
        String report = out.toString()
        String normalizedSpaceReport = report.replaceAll("[ \n]+", " ")

        then:
        expect normalizedSpaceReport, containsAllStrings(expectedStrings)
        expect normalizedSpaceReport, notContainsAllStrings(unexpectedStrings)

        where:
        format || unexpectedStrings | expectedStrings
        html   || ["<h2>", "<th>"]  | ['<table class="result rowstripes">',
                                       "<tr> <td> 1 </td> <td> 2 </td> <td> 3 </td> </tr>",
                                       "<tr> <td> a </td> <td> b </td> <td> c </td> </tr>"]
        txt    || ["=="]            | ["1\t2\t3",
                                       "a\tb\tc"]
    }

    @Unroll
    def "#format: expand links in table"() {
        when:
        List<List<String>> data = [["1", "2", "3"], ["a", "ZDB-GENE-121212-12", "c"]]
        rg.addDataTable(data)
        rg.write(out, format)
        String report = out.toString()

        then:
        expect report, containsAllStrings(expectedStrings)

        where:
        format || expectedStrings
        html   || ["<a href=\"http://${ZfinPropertiesEnum.DOMAIN_NAME}/ZDB-GENE-121212-12\">ZDB-GENE-121212-12</a>"]
        txt    || ['\tZDB-GENE-121212-12\t']
    }

    @Unroll
    def "#format: table with no data should not be shown"() {
        when:
        String caption = "the caption"
        List<String> header = Arrays.asList("One", "Two", "Three")
        List<List<String>> data = []
        rg.addDataTable(caption, header, data)
        rg.write(out, format)
        String report = out.toString()

        then:
        expect report, containsAllStrings(expectedStrings)
        expect report, notContainsAllStrings(unexpectedStrings)

        where:
        format || expectedStrings          | unexpectedStrings
        html   || ["<h2>the caption</h2>"] | ["<table", "One"]
        txt    || ["== the caption =="]    | ["-", "|", "One"]
    }

    @Unroll
    def "#format: error messages"() {
        when:
        rg.addErrorMessage("Oh no! An error!")
        rg.addErrorMessage(new RuntimeException("Something went wrong!"))
        rg.write(out, format)
        String report = out.toString()

        then:
        expect report, containsAllStrings(expectedStrings)

        where:
        format || expectedStrings
        html   || ['<pre class="error">Oh no! An error!</pre>', 'RuntimeException', 'Something went wrong!']
        txt    || ['=== Errors ===', 'Oh no! An error!', 'RuntimeException', 'Something went wrong!']
    }

    @Unroll
    def "#format: `code snippet"() {
        when:
        String code = "SELECT *\n" +
                "FROM whatever\n" +
                "WHERE something LIKE \"%another%\";"
        rg.addCodeSnippet(code)
        rg.write(out, format)
        String report = out.toString()

        then:
        expect report, containsAllStrings(expectedStrings)

        where:
        format || expectedStrings
        html   || ['<pre class="code">', "SELECT *\n", "FROM whatever\n", 'WHERE something LIKE "%another%";', '</pre>']
        txt    || ['=== Code being executed ===', "SELECT *\n", "FROM whatever\n", 'WHERE something LIKE "%another%";']
    }

    @Unroll
    def "#format: summary table"() {
        when:
        rg.addSummaryTable("Here is what happened", ["The Good": 1, "The Bad": 5, "The Ugly": 10])
        rg.write(out, format)
        String report = out.toString().replaceAll(/[ \n]+/, ' ')

        then:
        expect report, containsAllStrings(expectedStrings)

        where:
        format || expectedStrings
        html   || ['<h2>Here is what happened</h2>',
                   '<table class="summary rowstripes">',
                   '<tr> <th>The Good</th> <td>1</td> </tr>',
                   '<tr> <th>The Bad</th> <td>5</td> </tr>',
                   '<tr> <th>The Ugly</th> <td>10</td> </tr>']
        txt    || ['=== Here is what happened ===',
                   'The Good\t1',
                   'The Bad\t5',
                   'The Ugly\t10']
    }
    
    def "one of everything"() {
        when:
        rg.setReportTitle("What an amazing report!")
        rg.includeTimestamp()
        rg.addIntroParagraph("Hi, this is the first paragraph.")
        rg.addIntroParagraph("Yet another paragraph.")

        rg.addSummaryTable("Here is what happened", ["The Good": 1, "The Bad": 5, "The Ugly": 10])

        rg.addDataTable("Look at this data!",
                ["Uno", "Dos", "Tres"],
                [["ZDB-GENE-121212-12", "2", "3"], ["ZDB-GENE-121212-13", "b", "c"]])

        rg.addDataTable("What's this? A ragged table?",
                ["Foo", "Bar"],
                [["Lorem ipsum dolor", "sit amet"],
                 ["Pellentesque", "at tellus tristique dui", "hendrerit ultrices vel ac neque"],
                 ["Nulla aliquet", "ipsum consectetur"]])

        rg.addErrorMessage("Oh no! An error!")
        rg.addErrorMessage(new RuntimeException("Something went wrong!"))

        String code = "SELECT *\n" +
                "  FROM whatever\n" +
                "  WHERE something LIKE \"%another%\";"
        rg.addCodeSnippet(code)

        def tempDir = Files.createTempDirectory("ReportGeneratorSpec").toFile()
        rg.writeFiles(tempDir, "generated-report")

        then:
        new File(tempDir, "generated-report.txt").size() > 0
        new File(tempDir, "generated-report.html").size() > 0

        cleanup:
        tempDir.deleteDir()
    }

    /*
     *                HELPER STUFF
     */

    private static def containsAllStrings(strings) {
         allOf(strings.collect { str -> containsString(str)})
    }

    private static def notContainsAllStrings(strings) {
        allOf(strings.collect { str -> not(containsString(str)) })
    }

}