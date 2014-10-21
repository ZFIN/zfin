package org.zfin.uniquery

import org.springframework.beans.factory.annotation.Autowired
import org.zfin.ZfinIntegrationSpec
import org.zfin.search.service.RelatedDataService
import org.zfin.search.presentation.SearchResult
import spock.lang.Unroll


class RelatedLinksSpec extends ZfinIntegrationSpec {

    @Autowired
    RelatedDataService relatedDataService

    @Unroll
    def "#id shouldn't have empty relatedLinks"() {
        when:
        SearchResult result = new SearchResult()
        result.setId(id)
        result.setCategory(category)
        def links = relatedDataService.getRelatedDataLinks(result)

        then:
        links.size() > 0;

        where:
        category    | id
        "Gene"      | "ZDB-GENE-980526-561"
        "Construct" | "ZDB-TGCONSTRCT-070117-64"

    }


}
