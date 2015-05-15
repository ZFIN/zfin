package org.zfin.publication

import org.zfin.AbstractZfinSpec
import spock.lang.Shared

class MeshHeadingSpec extends AbstractZfinSpec {

    @Shared descriptors = [
            yogurt: new MeshTerm().with {
                name = "Yogurt"
                id = "D015014"
                return it
            },
            zebrafish: new MeshTerm().with {
                name = "Zebrafish"
                id = "D015027"
                return it
            },
            nails: new MeshTerm().with {
                name = "Nails, Malformed"
                id = "D009264"
                return it
            }
    ]

    @Shared quals = [
            abnormalities: new MeshTerm().with {
                name = "abnormalities"
                id = "Q000002"
                return  it
            },
            dosage: new MeshTerm().with {
                name = "administration & dosage"
                id = "Q000008"
                return it
            },
            diet: new MeshTerm().with {
                name = "diet therapy"
                id = "Q000178"
                return it
            }
    ]

    def "mesh heading with only descriptor"() {
        when:
        def heading = new MeshHeading().with {
            descriptor = new MeshHeadingTerm().with {
                term = descriptors.yogurt
                majorTopic = false
                return it
            }
            return it
        }

        then:
        heading.displayList == ["Yogurt"]
    }

    def "mesh heading with major topic descriptor"() {
        when:
        def heading = new MeshHeading().with {
            descriptor = new MeshHeadingTerm().with {
                term = descriptors.zebrafish
                majorTopic = true
                return it
            }
            return it
        }

        then:
        heading.displayList == ["Zebrafish*"]
    }

    def "mesh heading with qualifiers"() {
        when:
        def heading = new MeshHeading().with {
            descriptor = new MeshHeadingTerm().with {
                term = descriptors.yogurt
                majorTopic = false
                return it
            }
            qualifiers = [
                    new MeshHeadingTerm().with {
                        term = quals.diet
                        majorTopic = false
                        return it
                    },
                    new MeshHeadingTerm().with {
                        term = quals.dosage
                        majorTopic = false
                        return it
                    }
            ] as Set
            return it
        }

        then:
        heading.displayList == ["Yogurt/diet therapy", "Yogurt/administration & dosage"]
    }

    def "mesh heading with major topic qualifiers"() {
        when:
        def heading = new MeshHeading().with {
            descriptor = new MeshHeadingTerm().with {
                term = descriptors.nails
                majorTopic = false
                return it
            }
            qualifiers = [
                    new MeshHeadingTerm().with {
                        term = quals.abnormalities
                        majorTopic = true
                        return it
                    },
                    new MeshHeadingTerm().with {
                        term = quals.diet
                        majorTopic = false
                        return it
                    }
            ] as Set
            return it
        }

        then:
        heading.displayList == ["Nails, Malformed/abnormalities*", "Nails, Malformed/diet therapy"]
    }

}
