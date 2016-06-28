package org.zfin.search.service

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.zfin.ZfinIntegrationSpec

class SearchSuggestionServiceSpec extends ZfinIntegrationSpec {

    public static Logger logger = Logger.getLogger(SearchSuggestionServiceSpec.class)

    @Autowired SearchSuggestionService searchSuggestionService

/*    @Shared SolrClient client
    @Shared SolrQuery query
    @Shared SolrQuery secondQuery

    //sets up for all tests in class
    public def setupSpec() {
    //    client = SolrService.getSolrClient("prototype")
    }

    public def cleanSpec() {
    //    client = null
    }*/

    def "Suggest #suggestion in place of #queryString"() {
        when: "The query is made"
        String result = searchSuggestionService.getSuggestions(queryString)

        then: "An alternate query is suggested"
        result
        result.contains(suggestion)

        where:
        queryString             | suggestion
        "osteogenesis"          | "ossification"
        "krox20"                | "egr2b"

    }


}
