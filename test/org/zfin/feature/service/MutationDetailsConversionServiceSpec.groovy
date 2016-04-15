package org.zfin.feature.service

import org.zfin.AbstractZfinSpec
import org.zfin.feature.DnaMutationTerm
import org.zfin.feature.Feature
import org.zfin.feature.FeatureDnaMutationDetail
import org.zfin.feature.FeatureTranscriptMutationDetail
import org.zfin.feature.TranscriptConsequence
import org.zfin.gwt.root.dto.FeatureTypeEnum
import org.zfin.ontology.GenericTerm
import org.zfin.sequence.ForeignDB
import org.zfin.sequence.ReferenceDatabase
import spock.lang.Shared
import spock.lang.Unroll

class MutationDetailsConversionServiceSpec extends AbstractZfinSpec {

    @Shared
    MutationDetailsConversionService converter = new MutationDetailsConversionService()

    def 'mutation type field should be populated'() {
        setup:
        def feature = new Feature(type: FeatureTypeEnum.POINT_MUTATION)

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.mutationType == 'Point Mutation'
    }

    @Unroll
    def 'dna statement point mutations'() {
        setup:
        def feature = new Feature(
                type: FeatureTypeEnum.POINT_MUTATION,
                featureDnaMutationDetail: new FeatureDnaMutationDetail(
                        geneLocalizationTerm: localization == null ? null : new GenericTerm(oboID: localization),
                        exonNumber: exon,
                        intronNumber: intron,
                        dnaPositionStart: position,
                        referenceDatabase: db == null ? null : new ReferenceDatabase(foreignDB: new ForeignDB(displayName: db)),
                        dnaSequenceReferenceAccessionNumber: accession,
                        dnaMutationTerm: new DnaMutationTerm(displayName: 'A>G')
                )
        )

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.dnaChangeStatement == display

        where:
        localization | exon | intron | position | db        | accession || display
        null         | null | null   | null     | null      | null      || 'A>G'
        null         | 4    | null   | null     | null      | null      || 'A>G in exon 4'
        'SO:0001421' | 6    | 7      | 1010     | null      | null      || 'A>G at exon 6 - intron 7 splice junction at position 1010'
        null         | null | null   | 392      | null      | null      || 'A>G at position 392'
        null         | null | null   | 1829     | 'GENBANK' | 'C1032'   || 'A>G at position 1829 in GENBANK:C1032'
        null         | null | null   | null     | 'GENBANK' | '9999'    || 'A>G in GENBANK:9999'
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

    @Unroll
    def 'reference sequence statement with database #db, accession #accession'() {
        setup:
        def refDb = null
        if (db != null) {
            refDb = new ReferenceDatabase(
                    foreignDB: new ForeignDB(displayName: db)
            )
        }
        def dnaChange = new FeatureDnaMutationDetail(
                referenceDatabase: refDb,
                dnaSequenceReferenceAccessionNumber: accession
        )

        expect:
        converter.referenceSequenceStatement(dnaChange) == display

        where:
        db        | accession || display
        null      | null      || ""
        null      | "32"      || ""
        "GENBANK" | "2242"    || "in GENBANK:2242"
    }

    @Unroll
    def 'dna statement for deletions'() {
        setup:
        def feature = new Feature(
                type: FeatureTypeEnum.DELETION,
                featureDnaMutationDetail: new FeatureDnaMutationDetail(
                        numberRemovedBasePair: 10,
                        geneLocalizationTerm: localization == null ? null : new GenericTerm(oboID: localization),
                        exonNumber: exon,
                        intronNumber: intron,
                        dnaPositionStart: position,
                        referenceDatabase: db == null ? null : new ReferenceDatabase(foreignDB: new ForeignDB(displayName: db)),
                        dnaSequenceReferenceAccessionNumber: accession
                )
        )

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.dnaChangeStatement == display

        where:
        localization | exon | intron | position | db        | accession || display
        null         | null | null   | null     | null      | null      || '-10 bp'
        null         | null | 2      | null     | null      | null      || '-10 bp in intron 2'
        'SO:0000163' | 6    | null   | 1010     | null      | null      || '-10 bp in splice donor site of exon 6 at position 1010'
        null         | null | null   | 482      | null      | null      || '-10 bp at position 482'
        null         | null | null   | 1829     | 'GENBANK' | 'C1032'   || '-10 bp at position 1829 in GENBANK:C1032'
        null         | null | null   | null     | 'GENBANK' | '9999'    || '-10 bp in GENBANK:9999'
    }

    @Unroll
    def 'dna statement for insertions'() {
        setup:
        def feature = new Feature(
                type: FeatureTypeEnum.INSERTION,
                featureDnaMutationDetail: new FeatureDnaMutationDetail(
                        numberAddedBasePair: 13,
                        geneLocalizationTerm: localization == null ? null : new GenericTerm(oboID: localization),
                        exonNumber: exon,
                        intronNumber: intron,
                        dnaPositionStart: position,
                        referenceDatabase: db == null ? null : new ReferenceDatabase(foreignDB: new ForeignDB(displayName: db)),
                        dnaSequenceReferenceAccessionNumber: accession
                )
        )

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.dnaChangeStatement == display

        where:
        localization | exon | intron | position | db        | accession || display
        null         | null | null   | null     | null      | null      || '+13 bp'
        null         | 12   | null   | null     | null      | null      || '+13 bp in exon 12'
        'SO:0000204' | 6    | null   | 1010     | null      | null      || '+13 bp in 5\' UTR at position 1010'
        null         | null | null   | 832      | null      | null      || '+13 bp at position 832'
        null         | null | 5      | 1829     | 'GENBANK' | 'C1032'   || '+13 bp in intron 5 at position 1829 in GENBANK:C1032'
        null         | null | null   | null     | 'GENBANK' | '9999'    || '+13 bp in GENBANK:9999'
    }

    @Unroll
    def 'dna statement for indels'() {
        setup:
        def feature = new Feature(
                type: FeatureTypeEnum.INDEL,
                featureDnaMutationDetail: new FeatureDnaMutationDetail(
                        numberAddedBasePair: added,
                        numberRemovedBasePair: removed,
                        geneLocalizationTerm: localization == null ? null : new GenericTerm(oboID: localization),
                        exonNumber: exon,
                        intronNumber: intron,
                        dnaPositionStart: position,
                        referenceDatabase: db == null ? null : new ReferenceDatabase(foreignDB: new ForeignDB(displayName: db)),
                        dnaSequenceReferenceAccessionNumber: accession
                )
        )

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.dnaChangeStatement == display

        where:
        added | removed | localization | exon | intron | position | db        | accession || display
        18    | null    | null         | null | null   | null     | null      | null      || 'net +18 bp'
        null  | 21      | null         | null | null   | null     | null      | null      || 'net -21 bp'
        34    | 17      | null         | null | null   | null     | null      | null      || '+34/-17 bp'
        34    | 17      | null         | 2    | null   | null     | null      | null      || '+34/-17 bp in exon 2'
        34    | 17      | 'SO:0000163' | null | null   | 1010     | null      | null      || '+34/-17 bp in splice donor site at position 1010'
        34    | 17      | null         | null | null   | 832      | null      | null      || '+34/-17 bp at position 832'
        34    | 17      | null         | null | 5      | 1829     | 'GENBANK' | 'C1032'   || '+34/-17 bp in intron 5 at position 1829 in GENBANK:C1032'
        34    | 17      | null         | null | null   | null     | 'GENBANK' | '9999'    || '+34/-17 bp in GENBANK:9999'
    }

    @Unroll
    def 'dna statement for transgenics'() {
        setup:
        def feature = new Feature(
                type: FeatureTypeEnum.TRANSGENIC_INSERTION,
                featureDnaMutationDetail: new FeatureDnaMutationDetail(
                        geneLocalizationTerm: localization == null ? null : new GenericTerm(oboID: localization),
                        exonNumber: exon,
                        intronNumber: intron,
                        dnaPositionStart: position,
                        referenceDatabase: db == null ? null : new ReferenceDatabase(foreignDB: new ForeignDB(displayName: db)),
                        dnaSequenceReferenceAccessionNumber: accession
                )
        )

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.dnaChangeStatement == display

        where:
        localization | exon | intron | position | db       | accession || display
        null         | null | null   | null     | null     | null      || ""
        null         | null | 5      | null     | null     | null      || "Insertion in intron 5"
        'SO:0000167' | null | null   | null     | null     | null      || "Insertion in promotor"
        null         | null | null   | 8849     | null     | null      || "Insertion at position 8849"
        null         | null | null   | null     | 'FOOBAR' | '998A'    || "Insertion in FOOBAR:998A"
    }

    @Unroll
    def 'transcript consequence statement with term #term, exon #exon, intron #intron'() {
        setup:
        def transcriptConsequence = new FeatureTranscriptMutationDetail(
                transcriptConsequence: term == null ? null : new TranscriptConsequence(displayName: 'missense_variant'),
                exonNumber: exon,
                intronNumber: intron
        )

        expect:
        converter.transcriptConsequenceStatement(transcriptConsequence) == display

        where:
        term               | exon | intron || display
        null               | null | null   || ''
        'missense_variant' | null | null   || 'missense_variant'
        'missense_variant' | 1    | null   || 'missense_variant in exon 1'
        'missense_variant' | null | 2      || 'missense_variant in intron 2'
        'missense_variant' | 3    | 4      || 'missense_variant in exon 3'
    }

    @Unroll
    def 'transcript sequence statement should show all terms in correct order'() {
        setup:
        def detailSet = new TreeSet([
                new FeatureTranscriptMutationDetail(
                        transcriptConsequence: new TranscriptConsequence(displayName: 'missense_variant', order: 1)),
                new FeatureTranscriptMutationDetail(
                        transcriptConsequence: new TranscriptConsequence(displayName: 'intron_gain_variant', order: 6),
                        intronNumber: 5
                ),
                new FeatureTranscriptMutationDetail(
                        transcriptConsequence: new TranscriptConsequence(displayName: 'splicing_variant', order: 3),
                        exonNumber: 3
                ),
                new FeatureTranscriptMutationDetail(
                        transcriptConsequence: new TranscriptConsequence(displayName: '3_prime_UTR_variant', order: 2)),

        ])
        def feature = new Feature(featureTranscriptMutationDetailSet: detailSet)

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.transcriptChangeStatement == 'missense_variant, 3_prime_UTR_variant, splicing_variant in exon 3, intron_gain_variant in intron 5'
    }
}
