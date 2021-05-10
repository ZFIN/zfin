package org.zfin.framework

import org.hibernate.SessionFactory
import org.zfin.TestConfiguration
import org.zfin.fish.FishSearchCriteria
import org.zfin.fish.FishSearchResult
import org.zfin.fish.presentation.FishSearchFormBean
import org.zfin.fish.presentation.SortBy
import org.zfin.repository.RepositoryFactory
import spock.lang.Specification
import spock.lang.Unroll


class GroovyDBTest extends Specification {

    def setupSpec() {
        TestConfiguration.configure();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    def cleanupSpec() {
        HibernateUtil.closeSession();
    }

    @Unroll
    def "fish search with #value in gene/allele box"() {

        when:
            FishSearchFormBean formBean = new FishSearchFormBean();
            with formBean {
                setFilter1("showAll");
                setMaxDisplayRecords(20);
                setFirstPageRecord(1);
                setSortBy(SortBy.BEST_MATCH.toString());
                setGeneOrFeatureName(value)
            }
            FishSearchCriteria criteria = new FishSearchCriteria(formBean);
            FishSearchResult result = RepositoryFactory.getFishRepository().getFish(criteria);

        then: "results object should not be null"
            result != null

        and: "some results should be found"
            result.getResultsFound() > 0

        where:
            value << ["hsp70 gal4",
                      "MO1-epcam,zgc:110304",
                      "hsp70l:dntbx5a",
                      "Tg(ompb:Rno.Vamp2-EGFP,ompb:ptprsa_C1556S)",
                      "Tg(5xUAS:casp3a,5xUAS:Hsa.HIST1H2BJ-Citrine-YFP,cryaa:RFP)",
                      "Tg(shhb:Gal4TA4, 5xUAS:mRFP)",
                      "popeye's sister"]


    }

}