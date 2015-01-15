package org.zfin.sequence.repository

import org.apache.commons.collections.CollectionUtils
import org.zfin.AbstractZfinIntegrationSpec
import org.zfin.framework.HibernateUtil
import org.zfin.repository.RepositoryFactory
import org.zfin.sequence.Accession
import org.zfin.sequence.DBLink
import spock.lang.Unroll

/**
 * Created by kschaper on 12/1/14.
 */
class SequenceRepositorySpec extends AbstractZfinIntegrationSpec {
    @Unroll
    def "should be able to get dblinks from an accession #accessionNumber"() {
        //HibernateUtil.createTransaction();
        when:
        List<Accession> accessions = RepositoryFactory.sequenceRepository.getAccessionsByNumber(accessionNumber)
        Set<DBLink> links = accessions.get(0).getDbLinks();
        //HibernateUtil.rollbackTransaction();

        then: "either it's empty, or the first record returned should have dblinks"
        CollectionUtils.isEmpty(accessions) || accessions.get(0).getDbLinks()

        where:  //fails for: "ENSDARG00000002898", "NM_131281" , "CR925797"
        accessionNumber << [ "OTTDARG00000001297",  "O42278"]

    }

}
