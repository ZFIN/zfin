package org.zfin.curation.service
import org.zfin.AbstractZfinIntegrationSpec
import org.zfin.TestConfiguration
import org.zfin.curation.Correspondence
import org.zfin.curation.Curation
import org.zfin.curation.PublicationNote
import org.zfin.profile.Person
import org.zfin.publication.Publication
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
        def dto = converter.toCuratorDTO(person.patrick)

        then:
        dto.name == "$person.patrick.firstName $person.patrick.lastName"
        dto.zdbID == person.patrick.zdbID
        dto.imageURL == "/images/LOCAL/smallogo.gif"
    }

    def "convert Person to DTO for person with snapshot"() {
        when:
        def dto = converter.toCuratorDTO(person.monte)

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

    def "convert Correspondence to DTO with no reply received"() {
        when:
        def dto = converter.toCorrespondenceDTO(correspondence)

        then:
        dto.pub == correspondence.publication.zdbID
        dto.curator.zdbID == person.patrick.zdbID
        dto.id == correspondence.id
        dto.openedDate == correspondence.contactedDate
        !dto.replyReceived
        dto.closedDate == null
    }

    def "convert Correspondence to DTO with reply received"() {
        setup:
        correspondence.respondedDate = new Date()

        when:
        def dto = converter.toCorrespondenceDTO(correspondence)

        then:
        dto.pub == correspondence.publication.zdbID
        dto.curator.zdbID == person.patrick.zdbID
        dto.id == correspondence.id
        dto.openedDate == correspondence.contactedDate
        dto.replyReceived
        dto.closedDate == correspondence.respondedDate
    }

    def "convert Correspondence to DTO closed with no reply"() {
        setup:
        correspondence.giveUpDate = new Date()

        when:
        def dto = converter.toCorrespondenceDTO(correspondence)

        then:
        dto.pub == correspondence.publication.zdbID
        dto.curator.zdbID == person.patrick.zdbID
        dto.id == correspondence.id
        dto.openedDate == correspondence.contactedDate
        !dto.replyReceived
        dto.closedDate == correspondence.giveUpDate
    }

    def "convert Publication to Status DTO"() {
        setup:
        def pub = new Publication(
            closeDate: new GregorianCalendar(),
            indexed: true,
            indexedDate: new GregorianCalendar(),
            zdbID: "ZDB-PUB-122334-1",
            type: Publication.Type.JOURNAL
        )

        when:
        def dto = converter.toCurationStatusDTO(pub)

        then:
        dto.closedDate == pub.closeDate
        dto.indexed == pub.indexed
        dto.indexedDate == pub.indexedDate
        dto.pubZdbID == pub.zdbID
        dto.curationAllowed
    }

}
