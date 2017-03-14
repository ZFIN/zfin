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
        SearchResult result = new SearchResult(id: id, name: name, categories: [category.name])
        def links = relatedDataService.getRelatedDataLinks(result)

        then:
        links.size() > 0;

        where:
        category           | name                         | id
        Category.GENE      | "myogenic differentiation 1" | "ZDB-GENE-980526-561"
        Category.CONSTRUCT | "Tg(-2.4shha-ABC:GFP)"       | "ZDB-TGCONSTRCT-070117-64"

    }

    def "20kx18 should not have sequence related link"() {
        when:
        SearchResult result = new SearchResult(id: "ZDB-GENE-990706-1", name: "20kx18", categories: [Category.GENE.name])
        List<String> links = relatedDataService.getRelatedDataLinks(result)

        then:
        expect links, everyItem(not(containsString(RelatedDataService.SEQUENCES)))
    }

    def "pxna should have sequence related link"() {
        when:
        SearchResult result = new SearchResult(id: "ZDB-GENE-040105-1", name: "pxna", categories: [Category.GENE.name])
        List<String> links = relatedDataService.getRelatedDataLinks(result)

        then:
        expect links, hasItem(containsString(RelatedDataService.SEQUENCES))
    }

    def "fli3 should not have genome browser link"() {
        when:
        SearchResult result = new SearchResult(id: "ZDB-GENE-980526-376", name: "fli3", categories: [Category.GENE.name])
        List<String> links = relatedDataService.getRelatedDataLinks(result)

        then:
        expect links, everyItem(not(containsString(RelatedDataService.GENOME_BROWSER)))
    }

    def "Diamond-Blackfan anemia should have related genes"() {
        when:
        SearchResult result = new SearchResult(id: "DOID:1339", name: "Diamond-Blackfan anemia", categories: [Category.DISEASE.name])
        List<String> links = relatedDataService.getRelatedDataLinks(result)

        then:
        expect links, hasItem(containsString(RelatedDataService.RELATED_GENE))
    }

    def "kleptomania should have related genes"() {
        when:
        SearchResult result = new SearchResult(id: "DOID:12400", name: "kleptomania", categories: [Category.DISEASE.name])
        List<String> links = relatedDataService.getRelatedDataLinks(result)

        then:
        expect links, everyItem(not(containsString(RelatedDataService.RELATED_GENE)))
    }
}
