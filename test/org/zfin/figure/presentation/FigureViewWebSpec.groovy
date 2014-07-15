package org.zfin.figure.presentation

import org.hibernate.SessionFactory
import org.zfin.AbstractZfinSmokeSpec
import org.zfin.expression.Figure
import org.zfin.figure.FigureData
import org.zfin.figure.service.FigureViewService
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.repository.RepositoryFactory
import spock.lang.Shared
import spock.lang.Unroll

/**
 * Created to test figure view page
 */
class FigureViewWebSpec extends AbstractZfinSmokeSpec {

    //Figures to test, named for why they're interesting
    @Shared
    def figures = FigureData.figures

    @Unroll
    def "#figZdbID figure page should load, link to all-figure-view, then all-figure-view should load ok"() {
        when: "navigate to the figure page"
        to FigureViewPage, figZdbID  //this passes figZdbID as an argument to the page, in the url

        then: "page should load"
        at FigureViewPage

        when: "click on the additional figures link"
        additionalFiguresLink.click()

        then:
        at AllFigureViewPage

        where: "all of the pages, except for the ones that don't have any additional figures... "
        figZdbID << figures.values().findAll{it != figures.hasOnlyPhenotype && it != figures.hasHugeAntibodyTable}
    }


    @Unroll
    def "Thisse all figure page should load for pub: #pubZdbID & probe: #probeZdbID, should have submitted by links"() {
        when: "get the pub & probe and open up the page"
        to AllFigureViewPage, pubZdbID, probeZdbID: probeZdbID

        then: "the page should come up"
        at AllFigureViewPage

        and: "There should be 3 links to the people who submitted"
        submittedByLinks.size() == 3

        where:
        pubZdbID = "ZDB-PUB-010810-1"
        probeZdbID = "ZDB-EST-031204-27"


    }

    @Unroll
    def "#figZdbID page with qualifier should have qualifier column"() {
        when:
        to FigureViewPage, figZdbID

        then:
        expressionQualifierColumnValues.size() > 1

        where:
        figZdbID = figures.tenXpatRes
    }

    @Unroll
    def "#figZdbID page without qualifier should not have qualifier column"() {
        when:
        to FigureViewPage, figZdbID

        then:
        !expressionQualifierColumnValues

        where:
        figZdbID = figures.singleXpatRes
    }

}
