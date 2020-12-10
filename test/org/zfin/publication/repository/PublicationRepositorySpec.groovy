package org.zfin.publication.repository

import org.zfin.ZfinIntegrationSpec
import org.zfin.marker.Marker
import org.zfin.publication.Publication
import org.zfin.repository.RepositoryFactory
import spock.lang.Shared
import spock.lang.Unroll
import spock.lang.Ignore

class PublicationRepositorySpec extends ZfinIntegrationSpec {

    @Shared
    PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository()

    @Unroll
    def "#pubZdbID should have #count Markers"() {
        when: "get the publication"
        Publication publication = publicationRepository.getPublication(pubZdbID)

        then: "marker count for pub should match"
        (count as Long) <= publicationRepository.getMarkerCount(publication)
        where:
        count | pubZdbID
        15    | "ZDB-PUB-080701-3"
        1     | "ZDB-PUB-101004-27"
        2     | "ZDB-PUB-071219-4"
    }

    @Unroll
    def "#dataZdbId should show attribution to #pubZdbId according to issue #issue"() {
        when: "we get the publications for the id"
        List<Publication> publications = publicationRepository.getPubsForDisplay(dataZdbId)
        List<String> publicationIds = publications*.getZdbID()

        then: "the specified pub should be in the list"
        publications
        publicationIds
        publicationIds.contains(pubZdbId)

        where:
        dataZdbId          | pubZdbId              | issue
        "ZDB-ALT-000405-2" | "ZDB-PUB-060413-1"    | "INF-3165"
        "ZDB-ALT-010621-2" | "ZDB-PUB-130115-1"    | "INF-3165"
        "ZDB-ALT-010621-6" | "ZDB-PUB-130115-1"    | "INF-3165"
        "ZDB-ALT-000405-2" | "ZDB-PUB-060503-2"    | "INF-3165"
        "ZDB-ALT-000712-2" | "ZDB-PUB-080110-2"    | "INF-3165"
        "ZDB-ALT-040924-2" | "ZDB-PUB-060503-2"    | "INF-3165"
        "ZDB-ALT-000712-2" | "ZDB-PUB-080110-2"    | "INF-3165"

    }


}
