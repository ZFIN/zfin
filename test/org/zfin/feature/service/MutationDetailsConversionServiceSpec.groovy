package org.zfin.feature.service
import org.zfin.AbstractZfinSpec
import org.zfin.feature.Feature
import org.zfin.gwt.root.dto.FeatureTypeEnum
import spock.lang.Shared

class MutationDetailsConversionServiceSpec extends AbstractZfinSpec {

    @Shared MutationDetailsConversionService converter = new MutationDetailsConversionService()

    def "mutation type field should be populated"() {
        setup:
        def feature = new Feature(type: FeatureTypeEnum.POINT_MUTATION)

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.mutationType == "Point Mutation"
    }

}
