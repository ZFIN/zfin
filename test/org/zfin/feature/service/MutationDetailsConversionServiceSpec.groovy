package org.zfin.feature.service
import org.zfin.AbstractZfinSpec
import org.zfin.feature.DnaMutationTerm
import org.zfin.feature.Feature
import org.zfin.feature.FeatureDnaMutationDetail
import org.zfin.gwt.root.dto.FeatureTypeEnum
import spock.lang.Shared

class MutationDetailsConversionServiceSpec extends AbstractZfinSpec {

    @Shared MutationDetailsConversionService converter = new MutationDetailsConversionService()

    def 'mutation type field should be populated'() {
        setup:
        def feature = new Feature(type: FeatureTypeEnum.POINT_MUTATION)

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.mutationType == 'Point Mutation'
    }

    def 'dna statement should show nucleotide change for point mutation'() {
        setup:
        def feature = new Feature(
                type: FeatureTypeEnum.POINT_MUTATION,
                featureDnaMutationDetail: new FeatureDnaMutationDetail(
                        dnaMutationTerm: new DnaMutationTerm(displayName: 'A>G')
                )
        )

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.dnaChangeStatement == 'A>G'
    }

    def 'dna statement should show exon localiazation'() {
        setup:
        def feature = new Feature(
                type: FeatureTypeEnum.POINT_MUTATION,
                featureDnaMutationDetail: new FeatureDnaMutationDetail(
                        dnaMutationTerm: new DnaMutationTerm(displayName: 'A>G'),
                        exonNumber: 4
                )
        )

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.dnaChangeStatement == 'A>G in exon 4'
    }

}
