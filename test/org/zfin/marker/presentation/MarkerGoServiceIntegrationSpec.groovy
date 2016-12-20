package org.zfin.marker.presentation

import org.apache.commons.collections.CollectionUtils
import org.zfin.ZfinIntegrationSpec
import org.zfin.marker.Marker
import org.zfin.marker.service.MarkerGoService
import org.zfin.repository.RepositoryFactory
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Unroll

/**
 * Created by kschaper on 12/16/14.
 */
class MarkerGoServiceIntegrationSpec extends ZfinIntegrationSpec {

    @Shared MarkerGoService markerGoService

    //todo: this should be autowired!
    public def setupSpec() {
        markerGoService = new MarkerGoService()
    }
    public def cleanupSpec() {
        markerGoService = null
    }

    @Unroll
    def "#abbreviation should have some MarkerGoEvidenceCode records"() {
        when:
        Marker marker = RepositoryFactory.markerRepository.getMarkerByAbbreviation(abbreviation)

        then:
        marker
        CollectionUtils.isNotEmpty(marker.goTermEvidence)

        where:
        abbreviation << ["fgf8a","pax2a","bmp2a"]

    }

    @Ignore
    def "#abbreviation should get a non-empty collection of MarkerGoViewTableRows"() {
        when:
        Marker marker = RepositoryFactory.markerRepository.getMarkerByAbbreviation(abbreviation)

        then:
        marker
        CollectionUtils.isNotEmpty(markerGoService.getMarkerGoViewTableRows(marker))

        where:
        abbreviation << ["fgf8a","pax2a","bmp2a"]


    }

}
