package org.zfin.curation.service
import org.zfin.AbstractZfinIntegrationSpec
import org.zfin.TestConfiguration
import org.zfin.curation.Correspondence
import org.zfin.curation.Curation
import org.zfin.curation.PublicationNote
import org.zfin.profile.Person
import org.zfin.publication.Publication
import org.zfin.publication.PublicationTrackingHistory
import org.zfin.publication.PublicationTrackingLocation
import org.zfin.publication.PublicationTrackingStatus
import spock.lang.Shared
import spock.lang.Unroll

import javax.sql.rowset.serial.SerialBlob

class CurationDTOConversionServiceSpec extends AbstractZfinIntegrationSpec {

    @Shared person = [
            patrick: new Person(
                zdbID: "ZDB-PERS-140612-1",
                firstName: "Patrick",
                lastName: "Kalita",
                shortName: "Kalita-P."
            ),
            monte: new Person(
                zdbID: "ZDB-PERS-960805-676",
                firstName: "Monte",
                lastName: "Westerfield",
                shortName: "Westerfield-M.",
                snapshot: new SerialBlob(new byte[0])
            )
    ]

    @Shared curation = [
            open: new Curation(
                zdbID: "ZDB-CUR-030804-31",
                topic: Curation.Topic.GO,
                curator: person.patrick,
                dataFound: true,
                entryDate: new Date(2003, 8, 4, 10, 30, 30),
                openedDate: new Date(2003, 8, 10, 9, 49, 8),
                closedDate: null
            ),
            closed: new Curation(
                zdbID: "ZDB-CUR-030804-32",
                topic: Curation.Topic.NOMENCLATURE,
                curator: person.monte,
                dataFound: true,
                entryDate: new Date(2003, 8, 5, 10, 31, 30),
                openedDate: null,
                closedDate: new Date(2003, 8, 11, 14, 51, 10)
            )
    ]

    def setupSpec() {
        TestConfiguration.setAuthenticatedUser(person.patrick);
    }

    def converter = new CurationDTOConversionService();

    def note = new PublicationNote(
        zdbID: "ZDB-PNOTE-030801-6",
        text: "Hahaha! Great paper!",
        date: new Date(2003, 8, 1, 9, 16, 30)
    )

    def correspondence = new Correspondence(
        publication: new Publication(zdbID: "ZDB-PUB-030527-22"),
        id: 5,
        contactedDate: new Date(2003, 6, 12, 14, 11, 2)
    )

    def "convert PublicationNote to DTO for current logged-in user"() {
        setup:
        note.curator = person.patrick

        when:
        def dto = converter.toPublicationNoteDTO(note)

        then:
        dto.zdbID == note.zdbID
        dto.curator.name == "$person.patrick.firstName $person.patrick.lastName"
        dto.date == note.date
        dto.text == note.text
        dto.editable
    }

    def "convert PublicationNote to DTO for other user"() {
        setup:
        note.curator = person.monte

        when:
        def dto = converter.toPublicationNoteDTO(note)

        then:
        dto.zdbID == note.zdbID
        dto.curator.name == "$person.monte.firstName $person.monte.lastName"
        dto.date == note.date
        dto.text == note.text
        !dto.editable
    }

    def "convert Person to DTO for person without snapshot"() {
        when:
        def dto = converter.toPersonDTO(person.patrick)

        then:
        dto.name == "$person.patrick.firstName $person.patrick.lastName"
        dto.zdbID == person.patrick.zdbID
        dto.imageURL == "/images/LOCAL/smallogo.gif"
    }

    def "convert Person to DTO for person with snapshot"() {
        when:
        def dto = converter.toPersonDTO(person.monte)

        then:
        dto.name == "$person.monte.firstName $person.monte.lastName"
        dto.zdbID == person.monte.zdbID
        dto.imageURL == "/action/profile/image/view/${person.monte.zdbID}.jpg"
    }

    @Unroll
    def "convert Curation to DTO"() {
        when:
        def dto = converter.toCurationDTO(c)

        then:
        dto.zdbID == c.zdbID
        dto.topic == c.topic.toString()
        dto.curator.zdbID == c.curator.zdbID
        dto.dataFound == c.dataFound
        dto.entryDate == c.entryDate
        dto.openedDate == c.openedDate
        dto.closedDate == c.closedDate

        where:
        c << [curation.open, curation.closed]
    }

    def "get list of all curation topics"() {
        setup:
        def emptyTopic = new Curation();
        emptyTopic.setTopic(Curation.Topic.TOXICOLOGY)

        when:
        def dtos = converter.allCurationTopics([curation.open, curation.closed])

        then:
        dtos.size() == Curation.Topic.values().size() - 1 // no Linked Authors
        dtos.contains(converter.toCurationDTO(curation.open))
        dtos.contains(converter.toCurationDTO(emptyTopic))
    }

    def "convert PublicationTrackingHistory to Status DTO"() {
        setup:
        def status = new PublicationTrackingHistory(
                publication: new Publication(zdbID: "ZDB-PUB-123456-7"),
                status: new PublicationTrackingStatus(id: 5, type: PublicationTrackingStatus.Type.CURATING, name: "Curating"),
                location: new PublicationTrackingLocation(id: 2, name: "Bin 2", role: PublicationTrackingLocation.Role.CURATOR),
                owner: person.patrick,
                updater: person.monte,
                date: new GregorianCalendar(2015, 8, 30, 3, 4, 5),
                isCurrent: true
        )

        when:
        def dto = converter.toCurationStatusDTO(status)

        then:
        dto.pubZdbID == status.publication.zdbID
        dto.updateDate == status.date
        dto.status == status.status
        dto.location == status.location
        dto.owner.zdbID == status.owner.zdbID
    }

}
