package org.zfin.publication.repository

import org.zfin.ZfinIntegrationSpec
import org.zfin.marker.Marker
import org.zfin.publication.Publication
import org.zfin.repository.RepositoryFactory
import spock.lang.Shared
import spock.lang.Unroll


class PublicationRepositorySpec extends ZfinIntegrationSpec {

    @Shared
    PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository()

    @Unroll
    def "#pubZdbId should have #count Markers"() {
        when: "get the publication"
        Publication publication = publicationRepository.getPublication(pubZdbID)

        then: "marker count for pub should match"
        (count as Long) == publicationRepository.getMarkerCount(publication)
        where:
        count | pubZdbID
        15    | "ZDB-PUB-080701-3"
        1     | "ZDB-PUB-101004-27"
        2     | "ZDB-PUB-071219-4"
    }

    @Unroll
    def "#pubZdbId markers should be exactly: #markerIDs"() {
        when: "get the publication and marker list"
        Publication publication = publicationRepository.getPublication(pubZdbId)
        List<Marker> markers = publicationRepository.getMarkers(publication)

        then: "marker list should be not null, the correct size, and contain the correct list"
        markers != null
        markerIDs.size() == markers.size()
        markerIDs.containsAll(markers*.zdbID)

        where:
        pubZdbId            | markerIDs
        "ZDB-PUB-101004-27" | ["ZDB-GENE-020318-1"]
        "ZDB-PUB-071219-4"  | ["ZDB-GENE-000329-3","ZDB-GENE-020318-1"]
    }


}
