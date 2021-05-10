package org.zfin.publication

import org.zfin.AbstractZfinSpec
import spock.lang.Shared

class MeshHeadingSpec extends AbstractZfinSpec {

    @Shared descriptors = [
            yogurt: new MeshTerm(name: "Yogurt", id: "D015014"),
            zebrafish: new MeshTerm(name: "Zebrafish", id: "D015027"),
            nails: new MeshTerm(name: "Nails, Malformed", id: "D009264")
    ]

    @Shared quals = [
            abnormalities: new MeshTerm(name: "abnormalities", id: "Q000002"),
            dosage: new MeshTerm(name: "administration & dosage", id: "Q000008"),
            diet: new MeshTerm(name: "diet therapy", id: "Q000178")
    ]

    def "mesh heading with only descriptor"() {
        when:
        def heading = new MeshHeading(
                descriptor: new MeshHeadingTerm(
                        term: descriptors.yogurt,
                        majorTopic: false
                )
        )

        then:
        heading.displayList == ["Yogurt"]
    }

    def "mesh heading with major topic descriptor"() {
        when:
        def heading = new MeshHeading(
            descriptor: new MeshHeadingTerm(
                    term: descriptors.zebrafish,
                    majorTopic: true
            )
        )

        then:
        heading.displayList == ["Zebrafish*"]
    }

    def "mesh heading with qualifiers"() {
        when:
        def heading = new MeshHeading(
                descriptor: new MeshHeadingTerm(
                        term: descriptors.yogurt,
                        majorTopic: false
                ),
                qualifiers: [
                    new MeshHeadingTerm(
                            term: quals.diet,
                            majorTopic: false
                    ),
                    new MeshHeadingTerm(
                            term: quals.dosage,
                            majorTopic: false
                    )
                ] as SortedSet
        )

        then:
        heading.displayList == ["Yogurt/administration & dosage", "Yogurt/diet therapy"]
    }

    def "mesh heading with major topic qualifiers"() {
        when:
        def heading = new MeshHeading(
                descriptor: new MeshHeadingTerm(
                        term: descriptors.nails,
                        majorTopic: false
                ),
                qualifiers: [
                    new MeshHeadingTerm(
                            term: quals.abnormalities,
                            majorTopic: true
                    ),
                    new MeshHeadingTerm(
                            term: quals.diet,
                            majorTopic: false
                    )
                ] as SortedSet
        )

        then:
        heading.displayList == ["Nails, Malformed/abnormalities*", "Nails, Malformed/diet therapy"]
    }

}
