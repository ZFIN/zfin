package org.zfin.ontology.service

import org.zfin.ZfinIntegrationSpec
import org.zfin.framework.api.RibbonCategory
import org.zfin.framework.api.RibbonSummary
import org.zfin.marker.presentation.ExpressionRibbonDetail
import spock.lang.Shared
import spock.lang.Unroll

class RibbonServiceIntegrationSpec extends ZfinIntegrationSpec {

    @Shared
    RibbonService ribbonService = new RibbonService()
    @Shared
    def expressionCategory = ["anatomy": 0, "stage": 1, "cellular component": 2]

    @Unroll
    def "#zdbID should have non-zero annotation count for #termID using #handler handler"() {
        when:
        Map<String, Integer> termCounts = ribbonService.getRibbonCounts(handler, zdbID, [termID], [])

        then:
        termCounts
        termCounts.get(termID) != null
        termCounts.get(termID) > 0

        where:
        handler                  | zdbID                  | termID
        "/expression-annotation" | "ZDB-GENE-990415-8"    | "ZFA:0000396"
        "/expression-annotation" | "ZDB-GENE-980526-426"  | "ZFA:0000041"
        "/expression-annotation" | "ZDB-GENE-041001-150"  | "GO:0005737"
        "/expression-annotation" | "ZDB-GENE-041001-150"  | "GO:0005575" //GO-CC root
        "/expression-annotation" | "ZDB-GENE-041001-150"  | "ZFS:0000004"
        "/expression-annotation" | "ZDB-GENE-041001-150"  | "ZFS:0000046"
        "/expression-annotation" | "ZDB-GENE-990415-8"    | "ZFS:0000046"
        "/expression-annotation" | "ZDB-GENE-990415-8"    | "ZFS:0000045"
        "/expression-annotation" | "ZDB-GENE-990415-8"    | "ZFS:0000049"
        "/expression-annotation" | "ZDB-GENE-990415-8"    | "ZFS:0000050"
        "/go-annotation"         | "ZDB-GENE-990415-8"    | "GO:0005634"
        "/go-annotation"         | "ZDB-GENE-980526-426"  | "GO:0032502"
        "/go-annotation"         | "ZDB-GENE-980526-426"  | "GO:0003677"
        "/go-annotation"         | "ZDB-GENE-980526-178"  | "GO:0008283"
        "/go-annotation"         | "ZDB-GENE-980526-178"  | "GO:0005102"
        "/go-annotation"         | "ZDB-GENE-980526-178"  | "GO:0030154"
    }

    @Unroll
    def "#geneID and #termID "() {
        when:
        List<ExpressionRibbonDetail> termCounts = ribbonService.buildExpressionRibbonDetail(geneID, termID)

        then:
        termCounts.size() > numberOfRecords

        where:
        geneID              | termID        | numberOfRecords
        // all records
        //"ZDB-GENE-990415-8" | ""            | 200
        // nervous system
        "ZDB-GENE-990415-8" | "ZFA:0000396" | 60
    }

    @Unroll
    def "#zdbID GO ribbon should have categories and groups populated"() {
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

    @Unroll
    def "#zdbID expression ribbon should have categories and groups populated"() {
        when:
        RibbonSummary ribbonSummary = ribbonService.buildExpressionRibbonSummary(zdbID)

        then:
        ribbonSummary
        ribbonSummary.categories
        ribbonSummary.categories.size() == 3
        ribbonSummary.categories.get(0).groups?.size() > 5
        ribbonSummary.categories.get(1).groups?.size() >= 4
        ribbonSummary.categories.get(2).groups?.size() > 5

        where:
        zdbID << ["ZDB-GENE-990415-8", "ZDB-GENE-041001-150", "ZDB-GENE-980526-426", "ZDB-GENE-980526-178"]
    }

    @Unroll
    def "#zdbID expression ribbon should have some annotations for #category"() {
        when:
        RibbonSummary ribbonSummary = ribbonService.buildExpressionRibbonSummary(zdbID)
        RibbonCategory ribbonCategory = ribbonSummary.categories.get(expressionCategory[category])

        Integer allCount = ribbonSummary.subjects[expressionCategory[category]]?.groups[ribbonCategory.id]["ALL"]?.numberOfAnnotations


        then:
        ribbonSummary
        ribbonSummary.categories
        ribbonSummary.categories.size() == 3
        ribbonCategory.groups?.size() > 4
        allCount > 5

        where:
        zdbID                 | category
        "ZDB-GENE-990415-12"  | "anatomy"
        "ZDB-GENE-990415-12"  | "stage"
        "ZDB-GENE-990630-12"  | "cellular component"
        "ZDB-GENE-041001-150" | "anatomy"
        "ZDB-GENE-041001-150" | "stage"
        "ZDB-GENE-041001-150" | "cellular component"
        "ZDB-GENE-980526-426" | "anatomy"
        "ZDB-GENE-980526-426" | "stage"
        "ZDB-GENE-980526-178" | "anatomy"
        "ZDB-GENE-980526-178" | "stage"

    }
}
