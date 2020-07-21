package org.zfin.ontology.service

import org.apache.solr.client.solrj.SolrQuery
import org.springframework.beans.factory.annotation.Autowired
import org.zfin.ZfinIntegrationSpec
import org.zfin.expression.repository.ExpressionRepository
import org.zfin.framework.api.JsonResultResponse
import org.zfin.framework.api.Pagination
import org.zfin.framework.api.RibbonCategory
import org.zfin.framework.api.RibbonSubjectGroupCounts
import org.zfin.framework.api.RibbonSummary
import org.zfin.marker.presentation.ExpressionDetail
import org.zfin.marker.presentation.ExpressionRibbonDetail
import org.zfin.marker.presentation.PhenotypeRibbonSummary
import org.zfin.search.FieldName
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Unroll

class RibbonServiceIntegrationSpec extends ZfinIntegrationSpec {

    @Autowired
    RibbonService ribbonService

    @Autowired
    ExpressionRepository expressionRepository

    @Shared
    def expressionCategory = ["anatomy": 0, "stage": 1, "cellular component": 2]

    @Unroll
    def "#zdbID should have non-zero annotation count for #termID using #handler handler"() {
        when:
        SolrQuery query = new SolrQuery();
        query.setRequestHandler(handler);
        query.addFilterQuery(FieldName.GENE_ZDB_ID.getName() + ":" + zdbID);
        Map<String, RibbonSubjectGroupCounts> termCounts = ribbonService.getRibbonCounts(query, [termID])

        then:
        termCounts
        termCounts.get(termID) != null
        termCounts.get(termID).numberOfAnnotations > 0

        where:
        handler                  | zdbID                  | termID
        "/phenotype-annotation"  | "ZDB-GENE-990415-72"   | "ZFS:0100000"
        "/phenotype-annotation"  | "ZDB-GENE-990415-72"   | "ZFS:0000050"
        "/phenotype-annotation"  | "ZDB-GENE-990415-72"   | "ZFS:0000030"
        "/phenotype-annotation"  | "ZDB-GENE-990415-72"   | "ZFS:0100000"
        "/phenotype-annotation"  | "ZDB-GENE-990415-72"   | "ZFA:0000559"
        "/phenotype-annotation"  | "ZDB-GENE-990415-72"   | "ZFA:0000008"
        "/phenotype-annotation"  | "ZDB-GENE-990415-72"   | "ZFA:0001138"
        "/phenotype-annotation"  | "ZDB-GENE-030131-9008" | "GO:0033153"
        "/phenotype-annotation"  | "ZDB-GENE-030131-9008" | "GO:0071707"
        "/phenotype-annotation"  | "ZDB-GENE-030131-9008" | "GO:0044238"
        "/phenotype-annotation"  | "ZDB-GENE-030131-9008" | "GO:0046649"
        "/expression-annotation" | "ZDB-GENE-990415-8"    | "ZFA:0000396"
        "/expression-annotation" | "ZDB-GENE-980526-426"  | "ZFA:0000041"
        "/expression-annotation" | "ZDB-GENE-041001-150"  | "GO:0005737"
        "/expression-annotation" | "ZDB-GENE-041001-150"  | "GO:0005575" //GO-CC root
        "/expression-annotation" | "ZDB-GENE-041001-150"  | "ZFS:0000004"
        "/expression-annotation" | "ZDB-GENE-041001-150"  | "ZFS:0000046"
        "/expression-annotation" | "ZDB-GENE-990415-8"    | "ZFS:0000046"
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
    def "at least #min found for #geneID and #ribbonTermID with includeReporter: #includeReporter and onlyDirectSubmitted: #onlyDirectSubmitted "() {
        when:
        List<ExpressionRibbonDetail> termCounts = ribbonService.buildExpressionRibbonDetail(geneID, ribbonTermID, includeReporter, onlyDirectlySubmitted, isOther)

        then:
        termCounts
        termCounts.size() > min

        where:
        geneID                | ribbonTermID  | min | includeReporter | onlyDirectlySubmitted | isOther
        "ZDB-GENE-990415-8"   | "ZFA:0000396" | 60  | false           | false                 | false
        "ZDB-GENE-050419-145" | "ZFA:0000396" | 1   | false           | false                 | false
    }

    @Unroll
    def "#geneID expression detail response with #termID filter should return more than #numberOfRecords "() {
        when:
        JsonResultResponse<ExpressionDetail> response = ribbonService.buildExpressionDetail(geneID, supertermID, subtermID, includeReporter, onlyDirectSubmission, new Pagination())

        then:
        response
        response.getTotal() >= numberOfRecords

        where:
        geneID                  |   supertermID        | subtermID      | includeReporter | onlyDirectSubmission | numberOfRecords
          "ZDB-GENE-050419-145" | "ZFA:0009280"        | "GO:0097450"   | true            | false                | 1
          "ZDB-GENE-050419-145" | "ZFA:0000107"        | null           | true            | false                | 2
    }

    @Unroll
    def "All figures for #supertermID #subtermID expression detail for #geneID should be directly annotated"() {
        when:
        JsonResultResponse<ExpressionDetail> response = ribbonService.buildExpressionDetail(geneID, supertermID, subtermID, includeReporter, onlyDirectSubmission, new Pagination())

        Set<String> superterms = new HashSet<>()
        Set<String> subterms = new HashSet<>()

        response.results.each { result ->
            result.entities.each { entity ->
                superterms.add(entity.superterm.oboID)
                if (entity.subterm) { subterms.add(entity.subterm.oboID) }
            }
        }

        then:
        response
        response.getResults()
        superterms
        if (subtermID) { subterms && !subterms.isEmpty() }
        superterms.contains(supertermID)
        if (subtermID) { subterms.contains(subtermID) }


        where:
        geneID                | supertermID        | subtermID     | includeReporter | onlyDirectSubmission
        "ZDB-GENE-990415-8"   | "ZFA:0001135"      | "ZFA:0009052" | false           | false
        "ZDB-GENE-990415-72"  | "ZFA:0000648"      | null          | false           | false
        "ZDB-GENE-050419-145" | "ZFA:0009280"      | "GO:0097450"  | true            | false
    }

    @Unroll
    def "#geneID phenotype detail response with #termID filter should return more than #numberOfRecords "() {
        when:
        JsonResultResponse<PhenotypeRibbonSummary> response = ribbonService.buildPhenotypeSummary(geneID, termID, new Pagination(), isOther, excludeEaps)

        then:
        response
        response.getTotal() > numberOfRecords

        where:
        geneID               | termID        | isOther | excludeEaps | numberOfRecords
        // all records
        "ZDB-GENE-990415-30" | ""            | false   | false       | 15
        // pax2a             anatomical entity
        "ZDB-GENE-990415-8"  | "ZFA:0100000" | false   | false       | 100
        // pax2a              nervous system
        "ZDB-GENE-990415-8"  | "ZFA:0000396" | false   | false       | 60
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
        RibbonSummary ribbonSummary = ribbonService.buildExpressionRibbonSummary(zdbID, false, false)

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
        RibbonSummary ribbonSummary = ribbonService.buildExpressionRibbonSummary(zdbID, false, false)
        RibbonCategory ribbonCategory = ribbonSummary.categories.get(expressionCategory[category])

        Integer allCount = ribbonSummary.subjects[0].groups[ribbonCategory.id]["ALL"]?.numberOfAnnotations


        then:
        ribbonSummary
        ribbonSummary.categories
        ribbonSummary.categories.size() == 3
        ribbonCategory.groups?.size() > 4
        allCount > 5

        where:
        zdbID                 | category
        "ZDB-GENE-990415-30"  | "anatomy"
        "ZDB-GENE-990415-30"  | "stage"
        "ZDB-GENE-990415-30"  | "cellular component"
        "ZDB-GENE-040624-5"   | "anatomy"
        "ZDB-GENE-040624-5"   | "stage"
        "ZDB-GENE-040624-5"   | "cellular component"
        "ZDB-GENE-980526-426" | "anatomy"
        "ZDB-GENE-980526-426" | "stage"
        "ZDB-GENE-980526-178" | "anatomy"
        "ZDB-GENE-980526-178" | "stage"

    }
}
