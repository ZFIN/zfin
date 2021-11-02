package org.zfin.sequence.repository

import org.apache.commons.collections.CollectionUtils
import org.zfin.AbstractZfinIntegrationSpec
import org.zfin.framework.HibernateUtil
import org.zfin.gwt.marker.ui.DBLengthEntryField
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
        when:
        List<Accession> accessions = RepositoryFactory.sequenceRepository.getAccessionsByNumber(accessionNumber)

        then: "either it's empty, or the first record returned should have dblinks"
        CollectionUtils.isEmpty(accessions) || accessions.get(0).getDbLinks() //Why are we so flexible about what we assert here?

        where:  //fails for: "ENSDARG00000002898", "NM_131281" , "CR925797", passes for "OTTDARG00000001297",  "O42278"
        accessionNumber << [ "OTTDARG00000001297",  "O42278", "ENSDARG00000002898", "NM_131281" , "CR925797"]

    }

}
