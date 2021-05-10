package org.zfin.feature

import org.zfin.AbstractZfinSpec
import org.zfin.feature.repository.FeatureService
import org.zfin.publication.MeshHeading
import org.zfin.publication.MeshHeadingTerm
import org.zfin.publication.MeshTerm
import spock.lang.Shared

class FeatureServiceSpec extends AbstractZfinSpec {

    def "Get TermID from evidence Code #evidenceCode"() {
        when:
        def presentation = FeatureService.getFeatureGenomeLocationEvidenceCodeTerm(evidenceCode)

        then:
        presentation == termID

        where:
        evidenceCode | termID
        'TAS'        | 'ZDB-TERM-170419-250'
        'IC'         | 'ZDB-TERM-170419-251'
        'IEA'        | 'ZDB-TERM-170419-312'
        'uuiy'       | null
    }

}
