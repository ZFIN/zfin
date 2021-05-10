package org.zfin.expression

import org.springframework.beans.factory.annotation.Autowired
import org.zfin.ZfinIntegrationSpec
import org.zfin.expression.presentation.ExpressionSearchCriteria
import org.zfin.expression.service.ExpressionSearchService
import spock.lang.Shared
import spock.lang.Unroll


class ExpressionSearchSpec extends ZfinIntegrationSpec{

    @Autowired ExpressionSearchService expressionSearchService

    @Shared ExpressionSearchCriteria criteria


    //sets up for each test
    public void setup() {
        criteria = new ExpressionSearchCriteria()
        criteria.setPage(1)
        criteria.setRows(20)
    }

    public void clean() {
        criteria = null
    }

    @Unroll
    def "A Gene/EST query for #query should find some gene results"() {
        when: "we get expression gene results"
        criteria.setGeneField(geneField)
        expressionSearchService.getGeneResults(criteria)

        then: "the results should not be empty"
        criteria.getNumFound() > 0

        where:
        geneField << ["fgf8a", "cb110", "no tail", "ntl"]
    }

}
