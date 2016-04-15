package org.zfin.feature.service
import org.zfin.AbstractZfinSpec
import org.zfin.feature.DnaMutationTerm
import org.zfin.feature.Feature
import org.zfin.feature.FeatureDnaMutationDetail
import org.zfin.gwt.root.dto.FeatureTypeEnum
import org.zfin.ontology.GenericTerm
import spock.lang.Shared
import spock.lang.Unroll

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

    def 'dna statement should show gene localization for point mutation'() {
        setup:
        def feature = new Feature(
                type: FeatureTypeEnum.POINT_MUTATION,
                featureDnaMutationDetail: new FeatureDnaMutationDetail(
                        dnaMutationTerm: new DnaMutationTerm(displayName: 'A>G'),
                        exonNumber: 6
                )
        )

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.dnaChangeStatement == 'A>G in exon 6'
    }

    @Unroll
    def 'gene localization with term #termOboId, exon #exon, intron #intron'() {
        setup:
        def dnaChange = new FeatureDnaMutationDetail(
                exonNumber: exon,
                intronNumber: intron,
                geneLocalizationTerm: new GenericTerm(oboID: termOboId)
        )

        expect:
        converter.geneLocalizationWithPreposition(dnaChange) == display

        where:
        termOboId    | exon | intron || display
        null         | 1    | null   || "in exon 1"
        null         | null | 2      || "in intron 2"
        "SO:0000163" | null | null   || "in splice donor site"
        "SO:0000163" | 1    | null   || "in splice donor site of exon 1"
        "SO:0000163" | null | 2      || "in splice donor site of intron 2"
        "SO:0000164" | null | null   || "in splice acceptor site"
        "SO:0000164" | 1    | null   || "in splice acceptor site of exon 1"
        "SO:0000164" | null | 2      || "in splice acceptor site of intron 2"
        "SO:0001421" | null | null   || "at splice junction"
        "SO:0001421" | 3    | 3      || "at exon 3 - intron 3 splice junction"
        "SO:0001421" | 4    | 5      || "at exon 4 - intron 5 splice junction"
        "SO:0001421" | 7    | 6      || "at intron 6 - exon 7 splice junction"
        "SO:0000167" | 3    | 4      || "in promotor"
        "SO:0000318" | 3    | 4      || "in start codon"
        "SO:0000204" | 3    | 4      || "in 5' UTR"
        "SO:0000205" | 3    | 4      || "in 3' UTR"
        "SO:0000165" | 3    | 4      || "in enhancer"
    }

    def 'dna statement should show location for point mutation'() {
        setup:
        def feature = new Feature(
                type: FeatureTypeEnum.POINT_MUTATION,
                featureDnaMutationDetail: new FeatureDnaMutationDetail(
                        dnaMutationTerm: new DnaMutationTerm(displayName: 'A>G'),
                        dnaPositionStart: 48
                )
        )

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.dnaChangeStatement == 'A>G at position 48'
    }

    @Unroll
    def 'position statement with start #start, end #end'() {
        setup:
        def dnaChange = new FeatureDnaMutationDetail(
                dnaPositionStart: start,
                dnaPositionEnd: end
        )

        expect:
        converter.positionStatement(dnaChange) == display

        where:
        start | end  || display
        null  | null || ""
        null  | 38   || ""
        75    | null || "at position 75"
        181   | 371  || "from position 181 to 371"
    }

}
