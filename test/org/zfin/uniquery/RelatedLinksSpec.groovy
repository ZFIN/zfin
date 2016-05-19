package org.zfin.uniquery
import org.springframework.beans.factory.annotation.Autowired
import org.zfin.ZfinIntegrationSpec
import org.zfin.search.Category
import org.zfin.search.presentation.SearchResult
import org.zfin.search.service.RelatedDataService
import spock.lang.Unroll

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.expect

class RelatedLinksSpec extends ZfinIntegrationSpec {

    @Autowired
    RelatedDataService relatedDataService

    @Unroll
    def "#id shouldn't have empty relatedLinks"() {
        when:
        SearchResult result = new SearchResult(id: id, name: name, category: category.getName())
        def links = relatedDataService.getRelatedDataLinks(result)

        then:
        links.size() > 0;

        where:
        category           | name                         | id
        Category.GENE      | "myogenic differentiation 1" | "ZDB-GENE-980526-561"
        Category.CONSTRUCT | "Tg(-2.4shha-ABC:GFP)"       | "ZDB-TGCONSTRCT-070117-64"

    }

    def "pxnb should not have sequence related link"() {
        when:
        SearchResult result = new SearchResult(id: "ZDB-GENE-130530-697", name: "pxnb", category: Category.GENE.getName())
        List<String> links = relatedDataService.getRelatedDataLinks(result)

        then:
        expect links, everyItem(not(containsString("sequence/view")))
    }

    def "pxna should have sequence related link"() {
        when:
        SearchResult result = new SearchResult(id: "ZDB-GENE-040105-1", name: "pxna", category: Category.GENE.getName())
        List<String> links = relatedDataService.getRelatedDataLinks(result)

        then:
        expect links, hasItem(containsString("sequence/view"))
    }

}
