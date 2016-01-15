package org.zfin.uniquery

import org.apache.log4j.Logger
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.response.QueryResponse
import org.springframework.beans.factory.annotation.Autowired
import org.zfin.ZfinIntegrationSpec
import org.zfin.search.service.QueryManipulationService
import spock.lang.Shared
import spock.lang.Unroll
import org.zfin.search.service.SolrService
import org.zfin.search.Category


/* Test specific queries that rely on rules of text analysis in the solr schema or setup in solrconfig */
class QuerySpec extends ZfinIntegrationSpec {

    public static Logger logger = Logger.getLogger(QuerySpec.class)

    @Autowired
    SolrService solrService

    @Autowired
    QueryManipulationService queryManipulationService

    @Shared SolrClient client
    @Shared SolrQuery query
    @Shared SolrQuery secondQuery

    //sets up for all tests in class
    def setupSpec() {
        client = SolrService.getSolrClient("prototype")
    }

    def cleanSpec() {
        client = null
    }

    //sets up for each test
    def setup() {
        query = new SolrQuery()
        secondQuery = new SolrQuery()
    }

    def clean() {
        query = null
        secondQuery = null
    }

    @Unroll
    def "a query for '#queryString' in '#category' should find some records according to case #fogbugzCase"() {

        when: "Solr is queried"
        query.setQuery(queryManipulationService.processQueryString(queryString))
        query.addFilterQuery("category:\"" + category + "\"")
        QueryResponse response = new QueryResponse()

        try {
            response = client.query(query)
        } catch (Exception e) {
            logger.error(e);
        }

        then: "Query should succeed, result count should be greater than zero"
        response
        response.getResults()
        response.getResults().numFound > 0

        where:
        category                       | queryString                                      | fogbugzCase
        Category.ANTIBODY.name         | "actin ab1-act"                                  | "11329"
        Category.ANTIBODY.name         | "fgf Ab1-fgfbp1"                                 | "11209"
        Category.ANTIBODY.name         | "prk Ab2-prkcz"                                  | "11209"
        Category.MUTANT.name           | "gz12 ZDB-ALT-090424-3"                          | "11228"
        Category.MUTANT.name           | "b566 hox"                                       | "11208"
        Category.MARKER.name           | "hoxa2b DKEY-45E15"                              | "11289"
        Category.MARKER.name           | "hoxa2b BUSM1-31B14"                             | "11289"
        Category.MARKER.name           | "tlx1 CH211-103O12"                              | "11289"
        Category.MARKER.name           | "hoxa2b eu715"                                   | "11289"
        Category.MARKER.name           | "hoxa2b MGC:193940"                              | "11289"
        Category.MARKER.name           | "id:ibd5006  MGC:194301"                         | "11289"
        Category.MARKER.name           | "id:ibd5006  MGC:194262"                         | "11289"
        Category.MARKER.name           | "id:ibd5006  ibd5006"                            | "11289"
        Category.MARKER.name           | "id:ibd5006  CH211-96B20 id:ZDB-BAC-100127-972"  | "11289"
        Category.MARKER.name           | "id:ibd5006  fgf3"                               | "11289"
        Category.MUTANT.name           | "id:ZDB-ALT-000913-2 Contains a 1848 bp insertion" | "0" //priorities meeting discussion
        Category.GENE.name             | "OTTDARG00000020346 ZDB-GENE-030131-3445"        | "11331"
        Category.GENE.name             | "tlx1 CH211-103O12"                              | "11657"
        Category.GENE.name             | "fgf8 CH211-194I8 id:ZDB-GENE-990415-72"         | "11657"
        Category.GENE.name             | "fgf8 CH211-176L1 id:ZDB-GENE-010122-1"          | "11657"
        Category.GENE.name             | "expressed_in_tf:\"medulla oblongata\" type:Gene id:ZDB-GENE-000616-13"      | "0"
        Category.GENE.name             | "expressed_in_tf:\"enteric nervous system\" type:Gene id:ZDB-GENE-980526-90" | "0"
        Category.GENE.name             | "gene:13"                                        | "12504"
        Category.FIGURE.name           | "sa365"                                          | "12028"
        Category.FISH.name             | "t24412 MO4-tp53"                                | "11415"
        Category.FISH.name             | "Tg(5xUAS:casp3a,5xUAS:Hsa.HIST1H2BJ-Citrine,cryaa:RFP)" | "0"
        Category.FISH.name             | "casper mitfa"                                   | "13079"
        Category.FISH.name             | "ZDB-GENO-960809-7"                              | "13315"
        Category.FISH.name             | "sequence_alteration:bw6Tg ZDB-FISH-150901-26882"| "13808"
        Category.PUBLICATION.name      | "kraus 1993"                                     | "11699"
        Category.PUBLICATION.name      | "bohni"                                          | "11699"
        Category.PUBLICATION.name      | "abstract:\"motor control requires circuits\""   | "n/a"
        Category.PUBLICATION.name      | "thesis id:ZDB-PUB-150918-21"                    | "13042"
        Category.CONSTRUCT.name        | "4xnr"                                           | "11810"
        Category.COMMUNITY.name        | "nuss id:ZDB-PERS-960805-412"                    | "11216"
        Category.COMMUNITY.name        | "Nüss id:ZDB-PERS-960805-412"                    | "11216"
        Category.PUBLICATION.name      | "Nüss* id:ZDB-PUB-970602-19"                     | "11216"
        Category.PUBLICATION.name      | "Nuss* id:ZDB-PUB-970602-19"                     | "11216"
        Category.PUBLICATION.name      | "Nüsslein id:ZDB-PUB-970602-19"                  | "11216"
        Category.PUBLICATION.name      | "Nusslein id:ZDB-PUB-970602-19"                  | "11216"
        Category.SEQUENCE_TARGETING_REAGENT.name | "wnt2b mo ng id:ZDB-MRPHLNO-100212-1"       | "12314"
        Category.CONSTRUCT.name        | "Tg(-4.9sox10:LY-GFP) id:ZDB-TGCONSTRCT-120418-22" | "12299"
        Category.CONSTRUCT.name        | "Tg(-0.5vmhc:GFP) id:ZDB-TGCONSTRCT-110204-10"   | "12299"
        Category.CONSTRUCT.name        | "Tg(-1.0ins:EGFP) id:ZDB-TGCONSTRCT-080229-1"    | "12299"
        Category.CONSTRUCT.name        | "TgBAC(pax7a:GFP) id:ZDB-TGCONSTRCT-150108-2"    | "12299"
        Category.CONSTRUCT.name        | "Tg2(krt5;EGFP) id:ZDB-TGCONSTRCT-141020-3"      | "12299"
        Category.CONSTRUCT.name        | "Tg(ttna:MA-EGFP) id:ZDB-TGCONSTRCT-121127-1"    | "12299"
        Category.CONSTRUCT.name        | "ZDB-TGCONSTRCT-110127-19 coding_sequence:EGFP expressed_in_tf:\"dorsal aorta\"" | "12460"
        Category.ANATOMY.name          | "(-)-isopiperitenone reductase activity GO:0052581" | "12299"
        Category.DISEASE.name          | "DOID:10609 ICD10CM:E55"                            | "12560"
        Category.DISEASE.name          | "DOID:10609 UMLS_CUI:C0221468"                      | "12560"
        Category.DISEASE.name          | "DOID:10609 MSH:D012279"                            | "12560"
        Category.DISEASE.name          | "DOID:10609 NCI:C26878"                             | "12560"
        Category.MUTANT.name           | "ZMP:sa2801 sa2801"                                 | "1384"
    }


    @Unroll
    def "a query for '#queryString' in '#category' should find NO records according to case #fogbugzCase"() {

        when: "Solr is queried"
        query.setQuery(queryManipulationService.processQueryString(queryString))
        if (category) {
            query.addFilterQuery("category:\"" + category + "\"")
        }
        QueryResponse response = new QueryResponse()

        try {
            response = client.query(query)
        } catch (Exception e) {
            logger.error(e);
        }

        then: "Query should succeed, the results should be null/empty"
        response
        !response.getResults()

        where:
        category                  | queryString                          | fogbugzCase
        Category.EXPRESSIONS.name | "MGC:56505 zebrafish_gene:[* TO *]"  | "12346"
        Category.EXPRESSIONS.name | "MGC:56505 gcdha"                    | "12346"
        Category.PUBLICATION.name | "curator:\"David Fashena\" curator:\"Sabrina Toro\" ZDB-PUB-100504-14"  | "13571"
        Category.PUBLICATION.name | "Doug Howe id:ZDB-PUB-990119-20"     | "13813"
        null                      | "sheturnedmeintoanewt"               | "12988"


    }

        @Unroll
    def "a query in category #category for '#queryA' and a query for '#queryB' should return the same results"() {
        when: "Solr is queried for both queries"

        query.setQuery(queryA)
        secondQuery.setQuery(queryB)
        if (category) {
            query.addFilterQuery("category:\"" + category + "\"")
            secondQuery.addFilterQuery("category:\"" + category + "\"")
        }

        QueryResponse response = new QueryResponse()
        QueryResponse secondResponse = new QueryResponse()

        try {
            response = client.query(query)
            secondResponse = client.query(secondQuery)
        } catch (Exception e) {
            logger.error(e);
        }

    	then: "Result counts of the the two queries should match, as should the first few documents"
        response
        response.getResults()
        secondResponse
        secondResponse.getResults()
        response.getResults().numFound == secondResponse.getResults().numFound

    	where:
        category                    | queryA                         |  queryB
        Category.PHENOTYPE.name     | "small eyes"                   |  "small eye"              //Case 11266
        Category.PHENOTYPE.name     | "eye ectopic"                  |  "ectopic eyes"           //Case 11266
        Category.PHENOTYPE.name     | "vasculature torn"             |  "vasculature ruptured"   //Case 11266
        ""                          | "znf zmp mouse"                |  "znf zmp Mouse"          //Case 11330
    }

    def "a query for #queryString should bring back #resultName as the first result"() {
        when: "Solr is queried for this string"
        query.setQuery(queryManipulationService.processQueryString(queryString))
        QueryResponse response = new QueryResponse()

        try {
            response = client.query(query)
        } catch (Exception e) {
            logger.error(e);
        }

        then: "the first result matches the specified name"
        response?.results?.first()?.name == resultName

        where:
        queryString | resultName
        "fgf8a"     | "fgf8a"
        "pax2a"     | "pax2a"

    }


}
