package org.zfin.figure.service
import org.zfin.AbstractZfinIntegrationSpec
import org.zfin.anatomy.DevelopmentStage
import org.zfin.expression.Figure
import org.zfin.figure.FigureData
import org.zfin.figure.presentation.AntibodyTableRow
import org.zfin.figure.presentation.ExpressionTableRow
import org.zfin.figure.presentation.PhenotypeTableRow
import org.zfin.marker.Marker
import org.zfin.mutant.Fish
import org.zfin.mutant.PhenotypeWarehouse
import org.zfin.repository.RepositoryFactory
import spock.lang.Shared
import spock.lang.Unroll

import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository

class FigureViewServiceSpec extends AbstractZfinIntegrationSpec {

    @Shared
    FigureViewService figureViewService = new FigureViewService()

    @Shared
    def figures = FigureData.figures

    //Tests that specify an exact number are generally bad, but because figure data
    //is seldom updated after it's entered, hopefully these will stand the test of time
    @Unroll
    def "#figZdbID should have #count expression rows"() {
        when: "get the figure, get the rows"
        Figure figure = RepositoryFactory.figureRepository.getFigure(figZdbID)
        List<ExpressionTableRow> expressionTableRows = figureViewService.getExpressionTableRows(figure)

        then: "confirm that they have the correct number of rows"
        count == expressionTableRows.size()

        where:
        count | figZdbID
        1     | figures.singleXpatRes
        10    | figures.tenXpatRes
        0     | figures.hasOnlyPhenotype
        8     | figures.hasAllThree
        0     | figures.hasNoData

    }

    //Tests that specifiy an exact number are generally bad, but because figure data
    //is seldom updated after it's entered, hopefully these will stand the test of time
    @Unroll
    def "#figZdbID should have #count antibody rows"() {
        when: "get the figure, get the rows"
        Figure figure = RepositoryFactory.figureRepository.getFigure(figZdbID)
        List<AntibodyTableRow> antibodyTableRows = figureViewService.getAntibodyTableRows(figure)


        then: "confirm that they have the correct number of rows"
        count == antibodyTableRows.size()


        where:
        count | figZdbID
        0     | figures.singleXpatRes
        0     | figures.tenXpatRes
        0     | figures.hasOnlyPhenotype
        16    | figures.hasAllThree
        0     | figures.hasNoData

    }

    //Tests that specify an exact number are generally bad, but because figure data
    //is seldom updated after it's entered, hopefully these will stand the test of time
    @Unroll
    def "#figZdbID should have #count phenotype rows"() {
        when: "get the figure, get the rows"
        List<PhenotypeWarehouse> warehouseList = getPhenotypeRepository().getPhenotypeWarehouse(figZdbID);
        List<PhenotypeTableRow> phenotypeTableRows = figureViewService.getPhenotypeTableRows(warehouseList)

        then: "confirm that they have the correct number of rows"
        count == phenotypeTableRows.size()

        where:
        count | figZdbID
        0     | figures.singleXpatRes
        0     | figures.tenXpatRes
        25    | figures.hasOnlyPhenotype
        12    | figures.hasAllThree
        0     | figures.hasNoData

    }

    @Unroll
    def "#figZdbID should have #count expression fish"() {
        when: "we get the figure"
        Figure figure = RepositoryFactory.figureRepository.getFigure(figZdbID)
        List<Fish> fishList = figureViewService.getExpressionFish(figure)

        then:
        count == fishList.size()

        where:
        count | figZdbID
        0     | figures.hasNoData
        1     | figures.singleXpatRes
        2     | figures.tenXpatRes
        0     | figures.hasOnlyPhenotype
        6     | figures.hasAllThree

    }

    @Unroll
    def "#figZdbID should have #count expression entities"() {
        when: "we get the figure"
        Figure figure = RepositoryFactory.figureRepository.getFigure(figZdbID)
        def entities = figureViewService.getExpressionEntities(figure)

        then:
        count == entities.size()

        where:
        count | figZdbID
        0     | figures.hasNoData
        1     | figures.singleXpatRes
        2     | figures.tenXpatRes
        0     | figures.hasOnlyPhenotype
        7     | figures.hasAllThree
        1     | figures.anatomyNotReturning


    }

    @Unroll
    def "#figZdbID should have expression entities: #entities"() {
        when: "we get the term list"
        Figure figure = RepositoryFactory.figureRepository.getFigure(figZdbID)
        def entities = figureViewService.getExpressionEntities(figure)*.toString()

        then: "it should be the full list of terms, but exclude terms if expression is not found"
        expectedEntities == entities

        where:
        expectedEntities                           | figZdbID
        ["ectoderm", "hindbrain neural keel"]      | figures.tenXpatRes
        ["abducens motor nucleus", "facial nerve motor nucleus", "Rohon-Beard neuron",
         "secondary motor neuron", "spinal cord",
         "trigeminal motor nucleus", "vagal lobe"] | figures.hasAllThree
        []                                         | figures.hasNoData
        ["adaxial cell nucleus", "somite nucleus"] | figures.hasPostComposedExpression
    }


    @Unroll
    def "#figZdbID expression stages should be #start.abbreviation to #end.abbreviation"() {
        when: "we get the figure"
        Figure figure = RepositoryFactory.figureRepository.getFigure(figZdbID)
        DevelopmentStage start = figureViewService.getExpressionStartStage(figure)
        DevelopmentStage end = figureViewService.getExpressionEndStage(figure)

        then:
        startStageAbbrev == start?.abbreviation
        endStageAbbrev == end?.abbreviation

        where:
        startStageAbbrev | endStageAbbrev | figZdbID
        "26+ somites"    | "26+ somites"  | figures.singleXpatRes
        "Prim-5"         | "Long-pec"     | figures.hasMultiStageRange
        "Long-pec"       | "Long-pec"     | figures.hasAllThree
        null             | null           | figures.hasNoData

    }

    @Unroll
    def "#figZdbID list of phenotype fish should have #count fish"() {
        when: "we get the figure"
        Figure figure = RepositoryFactory.figureRepository.getFigure(figZdbID)
        List<Fish> fishList = figureViewService.getPhenotypeFish(figure)

        then:
        count == fishList.size()

        where:
        count | figZdbID
        0     | figures.hasNoData
        0     | figures.singleXpatRes
        11    | figures.hasOnlyPhenotype
        3     | figures.hasAllThree


    }

    @Unroll
    def "#figZdbID phenotype stages should be #startStageZdbID to #endStageZdbID"() {
        when: "we get the figure, along with start and end stages for all of it's phenotype data"
        Figure figure = RepositoryFactory.figureRepository.getFigure(figZdbID)
        DevelopmentStage start = figureViewService.getPhenotypeStartStage(figure)
        DevelopmentStage end = figureViewService.getPhenotypeEndStage(figure)

        then: "the start and end stages should match"
        startStageAbbrev == start?.abbreviation
        endStageAbbrev == end?.abbreviation

        where:
        startStageAbbrev | endStageAbbrev | figZdbID
        null             | null           | figures.singleXpatRes
        "Prim-5"         | "Prim-15"      | figures.hasMultiStageRange
        "Long-pec"       | "Long-pec"     | figures.hasAllThree
        null             | null           | figures.hasNoData

    }

    @Unroll
    def "#figZdbID should have #count knockdown reagents(str) in expression summary"() {
        when: "we get the figure"
        Figure figure = RepositoryFactory.figureRepository.getFigure(figZdbID)
        def entities = figureViewService.getExpressionSTR(figure)

        then:
        count == entities.size()

        where:
        count | figZdbID
        0     | figures.hasNoData
        1     | figures.hasAllThree
        4     | figures.tenXpatRes


    }

    @Unroll
    def "#figZdbID should have #count knockdown reagents(str) in phenotype summary"() {
        when: "we get the figure"
        Figure figure = RepositoryFactory.figureRepository.getFigure(figZdbID)
        def entities = figureViewService.getPhenotypeSTR(figure)

        then:
        count == entities.size()

        where:
        count | figZdbID
        0     | figures.hasNoData
        1     | figures.hasAllThree
        2     | figures.hasPhenotypeKnockdownReagents
        4     | figures.hasMultiStageRange
    }

    @Unroll
    def "#figZdbID should show #count conditions in expression summary"() {
        when: "get the figure condition list"
        Figure figure = RepositoryFactory.figureRepository.getFigure(figZdbID)
        def conditions = figureViewService.getExpressionCondition(figure)

        then:
        count == conditions.size()

        where:
        count | figZdbID
        0     | figures.tenXpatRes      // has only 'standard or control'
        0     | figures.hasPhenotypeKnockdownReagents    // has only experiments with reagents

    }

    @Unroll
    def "#figZdbID should show #count conditions in phenotype summary"() {
        when: "get the figure condition list"
        Figure figure = RepositoryFactory.figureRepository.getFigure(figZdbID)
        def conditions = figureViewService.getPhenotypeCondition(figure)

        then:
        count <= conditions.size()

        where:
        figZdbID            || count
        'ZDB-FIG-151029-27' || 2
    }

}
