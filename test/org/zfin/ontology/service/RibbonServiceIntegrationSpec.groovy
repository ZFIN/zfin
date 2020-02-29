package org.zfin.ontology.service

import org.zfin.ZfinIntegrationSpec
import org.zfin.framework.api.RibbonSummary
import spock.lang.Shared
import spock.lang.Unroll

class RibbonServiceIntegrationSpec extends ZfinIntegrationSpec {

    @Shared RibbonService ribbonService = new RibbonService()

    @Unroll
    def "#zdbID should have non-zero annotation count for #termID using #handler handler"() {
        when:
        Map<String, Integer> termCounts = ribbonService.getRibbonCounts(handler, zdbID, [termID],[])

        then:
        termCounts
        termCounts.get(termID) != null
        termCounts.get(termID) > 0

        where:
        handler                  | zdbID                  | termID
        "/expression-annotation" | "ZDB-GENE-990415-8"    | "ZFA:0000396"
        "/expression-annotation" | "ZDB-GENE-980526-426"  | "ZFA:0000041"
        "/expression-annotation" | "ZDB-GENE-041001-150"  | "GO:0005737"
        "/expression-annotation" | "ZDB-GENE-041001-150"  | "ZFS:0000004"
        "/go-annotation"         | "ZDB-GENE-990415-8"    | "GO:0005634"
        "/go-annotation"         | "ZDB-GENE-980526-426"  | "GO:0032502"
        "/go-annotation"         | "ZDB-GENE-980526-426"  | "GO:0003677"
        "/go-annotation"         | "ZDB-GENE-980526-178"  | "GO:0008283"
        "/go-annotation"         | "ZDB-GENE-980526-178"  | "GO:0005102"
        "/go-annotation"         | "ZDB-GENE-980526-178"  | "GO:0030154"
    }

    def "#zdbID should have populated GO ribbon summary"() {
        when:
        RibbonSummary ribbonSummary = ribbonService.buildGORibbonSummary(zdbID)

        then:
        ribbonSummary
        ribbonSummary.categories
        ribbonSummary.categories.size() == 3
        ribbonSummary.categories.get(0).groups?.size() > 5
        ribbonSummary.categories.get(1).groups?.size() > 5
        ribbonSummary.categories.get(2).groups?.size() > 5


        where:
        zdbID << ["ZDB-GENE-990415-8", "ZDB-GENE-041001-150", "ZDB-GENE-980526-426", "ZDB-GENE-980526-178"]

    }

    def "#zdbID should have populated Expression ribbon summary"() {
        when:
        RibbonSummary ribbonSummary = ribbonService.buildExpressionRibbonSummary(zdbID)

        then:
        ribbonSummary
        ribbonSummary.categories
        ribbonSummary.categories.size() == 3
        ribbonSummary.categories.get(0).groups?.size() > 5
        ribbonSummary.categories.get(1).groups?.size() >= 4 //this likely should actually be higher than 4 once we have stages populated
        ribbonSummary.categories.get(2).groups?.size() > 5

        where:
        zdbID << ["ZDB-GENE-990415-8", "ZDB-GENE-041001-150", "ZDB-GENE-980526-426", "ZDB-GENE-980526-178"]
    }

}
