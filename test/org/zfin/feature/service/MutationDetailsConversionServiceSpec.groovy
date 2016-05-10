package org.zfin.feature.service

import org.zfin.AbstractZfinSpec
import org.zfin.feature.*
import org.zfin.gwt.root.dto.FeatureTypeEnum
import org.zfin.sequence.ForeignDB
import org.zfin.sequence.ReferenceDatabase
import spock.lang.Shared
import spock.lang.Unroll

class MutationDetailsConversionServiceSpec extends AbstractZfinSpec {

    @Shared
    MutationDetailsConversionService converter = new MutationDetailsConversionService()

    @Shared
    GeneLocalizationTerm spliceDonor = new GeneLocalizationTerm(zdbID: "ZDB-TERM-130401-166", displayName: "splice donor site")
    @Shared
    GeneLocalizationTerm spliceAcceptor = new GeneLocalizationTerm(zdbID: "ZDB-TERM-130401-167", displayName: "splice acceptor site")
    @Shared
    GeneLocalizationTerm spliceJunction = new GeneLocalizationTerm(zdbID: "ZDB-TERM-130401-1417", displayName: "splice junction")
    @Shared
    GeneLocalizationTerm promoter = new GeneLocalizationTerm(zdbID: "ZDB-TERM-130401-170", displayName: "promoter")
    @Shared
    GeneLocalizationTerm enhancer = new GeneLocalizationTerm(zdbID: "ZDB-TERM-130401-168", displayName: "enhancer")
    @Shared
    GeneLocalizationTerm fivePrimeUTR = new GeneLocalizationTerm(zdbID: "ZDB-TERM-130401-207", displayName: "5' UTR")
    @Shared
    GeneLocalizationTerm threePrimeUTR = new GeneLocalizationTerm(zdbID: "ZDB-TERM-130401-208", displayName: "3' UTR")
    @Shared
    GeneLocalizationTerm startCodon = new GeneLocalizationTerm(zdbID: "ZDB-TERM-130401-321", displayName: "start codon")
    @Shared
    GeneLocalizationTerm exonLoc = new GeneLocalizationTerm(zdbID: "ZDB-TERM-130401-150", displayName: "exon");
    @Shared
    GeneLocalizationTerm intronLoc = new GeneLocalizationTerm(zdbID: "ZDB-TERM-130401-191", displayName: "intron");

    @Shared
    TranscriptConsequence missense = new TranscriptConsequence(zdbID: "ZDB-TERM-130401-1577", displayName: "missense", order: 1)
    @Shared
    TranscriptConsequence gain = new TranscriptConsequence(zdbID: "ZDB-TERM-130401-1568", displayName: "gain", order: 6)
    @Shared
    TranscriptConsequence loss = new TranscriptConsequence(zdbID: "ZDB-TERM-130401-1567", displayName: "loss", order: 6)
    @Shared
    TranscriptConsequence splicingVariant = new TranscriptConsequence(zdbID: "ZDB-TERM-130401-1563", displayName: "splicing variant", order: 3)
    @Shared
    TranscriptConsequence frameshift = new TranscriptConsequence(zdbID: "ZDB-TERM-130401-1581", displayName: "frameshift", order: 7)


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
                        geneLocalizationTerm: localization,
                        exonNumber: exon,
                        intronNumber: intron,
                        dnaPositionStart: position, // curation interface sets both fields
                        dnaPositionEnd: position,   // to the same value in this case
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
        localization   | exon | intron | position | db        | accession || display
        null           | null | null   | null     | null      | null      || 'A>G'
        exonLoc        | 4    | null   | null     | null      | null      || 'A>G in Exon 4'
        spliceJunction | 6    | 7      | 1010     | null      | null      || 'A>G at position 1010 at Exon 6 - Intron 7 Splice Junction'
        null           | null | null   | 392      | null      | null      || 'A>G at position 392'
        null           | null | null   | 1829     | 'GENBANK' | 'C1032'   || 'A>G at position 1829 in GENBANK:C1032'
        null           | null | null   | null     | 'GENBANK' | '9999'    || 'A>G in GENBANK:9999'
    }

    @Unroll
    def 'gene localization with term #localization, exon #exon, intron #intron'() {
        setup:
        def dnaChange = new FeatureDnaMutationDetail(
                exonNumber: exon,
                intronNumber: intron,
                geneLocalizationTerm: localization
        )

        expect:
        converter.geneLocalizationWithPreposition(dnaChange) == display

        where:
        localization   | exon | intron || display
        null           | 1    | null   || ""
        null           | null | 2      || ""
        exonLoc        | 1    | null   || "in Exon 1"
        intronLoc      | null | 2      || "in Intron 2"
        spliceDonor    | null | null   || "in Splice Donor Site"
        spliceDonor    | 1    | null   || "in Splice Donor Site of Exon 1"
        spliceDonor    | null | 2      || "in Splice Donor Site of Intron 2"
        spliceAcceptor | null | null   || "in Splice Acceptor Site"
        spliceAcceptor | 1    | null   || "in Splice Acceptor Site of Exon 1"
        spliceAcceptor | null | 2      || "in Splice Acceptor Site of Intron 2"
        spliceJunction | null | null   || "at Splice Junction"
        spliceJunction | 3    | 3      || "at Exon 3 - Intron 3 Splice Junction"
        spliceJunction | 4    | 5      || "at Exon 4 - Intron 5 Splice Junction"
        spliceJunction | 7    | 6      || "at Intron 6 - Exon 7 Splice Junction"
        promoter       | 3    | 4      || "in Promoter"
        startCodon     | 3    | 4      || "in Start Codon"
        fivePrimeUTR   | 3    | 4      || "in 5' UTR"
        threePrimeUTR  | 3    | 4      || "in 3' UTR"
        enhancer       | 3    | 4      || "in Enhancer"
    }

    @Unroll
    def 'dna position statement with start #start, end #end'() {
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
        22    | 22   || "at position 22"
        181   | 371  || "from position 181 to 371"
    }

    @Unroll
    def 'dna reference sequence statement with database #db, accession #accession'() {
        setup:
        def dnaChange = new FeatureDnaMutationDetail(
                referenceDatabase: db == null ? null : new ReferenceDatabase(foreignDB: new ForeignDB(displayName: db)),
                dnaSequenceReferenceAccessionNumber: accession
        )

        expect:
        converter.referenceSequenceStatement(dnaChange, false) == display

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
                        geneLocalizationTerm: localization,
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
        null         | null | null   | null     | null      | null      || '10 bp deleted'
        intronLoc    | null | 2      | null     | null      | null      || '10 bp deleted in Intron 2'
        spliceDonor  | 6    | null   | 1010     | null      | null      || '10 bp deleted at position 1010 in Splice Donor Site of Exon 6'
        null         | null | null   | 482      | null      | null      || '10 bp deleted at position 482'
        null         | null | null   | 1829     | 'GENBANK' | 'C1032'   || '10 bp deleted at position 1829 in GENBANK:C1032'
        null         | null | null   | null     | 'GENBANK' | '9999'    || '10 bp deleted in GENBANK:9999'
    }

    @Unroll
    def 'dna statement for insertions'() {
        setup:
        def feature = new Feature(
                type: FeatureTypeEnum.INSERTION,
                featureDnaMutationDetail: new FeatureDnaMutationDetail(
                        numberAddedBasePair: 13,
                        geneLocalizationTerm: localization,
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
        null         | null | null   | null     | null      | null      || '13 bp inserted'
        exonLoc      | 12   | null   | null     | null      | null      || '13 bp inserted in Exon 12'
        fivePrimeUTR | 6    | null   | 1010     | null      | null      || '13 bp inserted at position 1010 in 5\' UTR'
        null         | null | null   | 832      | null      | null      || '13 bp inserted at position 832'
        intronLoc    | null | 5      | 1829     | 'GENBANK' | 'C1032'   || '13 bp inserted at position 1829 in GENBANK:C1032 in Intron 5'
        null         | null | null   | null     | 'GENBANK' | '9999'    || '13 bp inserted in GENBANK:9999'
    }

    @Unroll
    def 'dna statement for indels'() {
        setup:
        def feature = new Feature(
                type: FeatureTypeEnum.INDEL,
                featureDnaMutationDetail: new FeatureDnaMutationDetail(
                        numberAddedBasePair: added,
                        numberRemovedBasePair: removed,
                        geneLocalizationTerm: localization,
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
        18    | null    | null         | null | null   | null     | null      | null      || 'Net 18 bp inserted'
        null  | 21      | null         | null | null   | null     | null      | null      || 'Net 21 bp deleted'
        34    | 17      | null         | null | null   | null     | null      | null      || '34 bp inserted / 17 bp deleted'
        34    | 17      | exonLoc      | 2    | null   | null     | null      | null      || '34 bp inserted / 17 bp deleted in Exon 2'
        34    | 17      | spliceDonor  | null | null   | 1010     | null      | null      || '34 bp inserted / 17 bp deleted at position 1010 in Splice Donor Site'
        34    | 17      | null         | null | null   | 832      | null      | null      || '34 bp inserted / 17 bp deleted at position 832'
        34    | 17      | intronLoc    | null | 5      | 1829     | 'GENBANK' | 'C1032'   || '34 bp inserted / 17 bp deleted at position 1829 in GENBANK:C1032 in Intron 5'
        34    | 17      | null         | null | null   | null     | 'GENBANK' | '9999'    || '34 bp inserted / 17 bp deleted in GENBANK:9999'
    }

    @Unroll
    def 'dna statement for transgenics'() {
        setup:
        def feature = new Feature(
                type: FeatureTypeEnum.TRANSGENIC_INSERTION,
                featureDnaMutationDetail: new FeatureDnaMutationDetail(
                        geneLocalizationTerm: localization,
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
        intronLoc    | null | 5      | null     | null     | null      || "Insertion in Intron 5"
        promoter     | null | null   | null     | null     | null      || "Insertion in Promoter"
        null         | null | null   | 8849     | null     | null      || "Insertion at position 8849"
        null         | null | null   | null     | 'FOOBAR' | '998A'    || "Insertion in FOOBAR:998A"
    }

    @Unroll
    def 'transcript consequence statement with term #term, exon #exon, intron #intron'() {
        setup:
        def transcriptConsequence = new FeatureTranscriptMutationDetail(
                transcriptConsequence: term,
                exonNumber: exon,
                intronNumber: intron
        )

        expect:
        converter.transcriptConsequenceStatement(transcriptConsequence) == display

        where:
        term     | exon | intron || display
        null     | null | null   || ''
        missense | null | null   || 'Missense'
        missense | 1    | null   || 'Missense in Exon 1'
        missense | null | 2      || 'Missense in Intron 2'
        missense | 3    | 4      || 'Missense in Exon 3'
        gain     | null | null   || 'Gain'
        gain     | 1    | null   || 'Gain of Exon 1'
        gain     | null | 2      || 'Gain of Intron 2'
        loss     | 3    | null   || 'Loss of Exon 3'
        loss     | null | 4      || 'Loss of Intron 4'
    }

    @Unroll
    def 'transcript sequence statement should show all terms in correct order'() {
        setup:
        def detailSet = new TreeSet([
                new FeatureTranscriptMutationDetail(transcriptConsequence: missense),
                new FeatureTranscriptMutationDetail(transcriptConsequence: gain, intronNumber: 5),
                new FeatureTranscriptMutationDetail(transcriptConsequence: splicingVariant, exonNumber: 3),
                new FeatureTranscriptMutationDetail(transcriptConsequence: frameshift)])
        def feature = new Feature(featureTranscriptMutationDetailSet: detailSet)

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.transcriptChangeStatement == 'Missense, Splicing Variant in Exon 3, Gain of Intron 5, Frameshift'
    }

    @Unroll
    def 'protein position statement with start #start, end #end'() {
        setup:
        def proteinConsequence = new FeatureProteinMutationDetail(
                proteinPositionStart: start,
                proteinPositionEnd: end
        )

        expect:
        converter.positionStatement(proteinConsequence) == display

        where:
        start | end   || display
        null  | null  || ""
        null  | 12    || ""
        9911  | null  || "at position 9911"
        28281 | 28282 || "from position 28281 to 28282"
    }

    @Unroll
    def 'protein reference sequence statement with database #db, accession #accession'() {
        setup:
        def proteinConsequence = new FeatureProteinMutationDetail(
                referenceDatabase: db == null ? null : new ReferenceDatabase(foreignDB: new ForeignDB(displayName: db)),
                proteinSequenceReferenceAccessionNumber: accession
        )

        expect:
        converter.referenceSequenceStatement(proteinConsequence, false) == display

        where:
        db         | accession || display
        null       | null      || ""
        null       | "13111"   || ""
        "PROTEINZ" | "ZZ11"    || "in PROTEINZ:ZZ11"
    }

    @Unroll
    def 'amino acid change statement with wild type AA #wtAA, mutant AA #mutantAA, #addedAA added, #removedAA removed'() {
        setup:
        def proteinConsequence = new FeatureProteinMutationDetail(
                wildtypeAminoAcid: wtAA == null ? null : new AminoAcidTerm(displayName: wtAA),
                mutantAminoAcid: mutantAA == null ? null : new AminoAcidTerm(displayName: mutantAA),
                numberAminoAcidsAdded: addedAA,
                numberAminoAcidsRemoved: removedAA
        )

        expect:
        converter.aminoAcidChangeStatement(proteinConsequence) == display

        where:
        wtAA  | mutantAA | addedAA | removedAA || display
        null  | null     | null    | null      || ''
        'Trp' | null     | null    | null      || 'Trp>STOP'
        null  | 'Met'    | null    | null      || ''
        'Phe' | 'Gly'    | null    | null      || 'Phe>Gly'
        null  | null     | 5       | null      || '5 AA added'
        null  | null     | null    | 8         || '8 AA missing'
        null  | null     | 3       | 9         || '3 AA added / 9 AA missing'
        'Sec' | 'Ala'    | 1       | 2         || 'Sec>Ala, 1 AA added / 2 AA missing' // does this case even make sense? well, just in case.
    }

    @Unroll
    def 'protein consequence statement'() {
        setup:
        def feature = new Feature(
                featureProteinMutationDetail: new FeatureProteinMutationDetail(
                        wildtypeAminoAcid: wtAA == null ? null : new AminoAcidTerm(displayName: wtAA),
                        mutantAminoAcid: mutantAA == null ? null : new AminoAcidTerm(displayName: mutantAA),
                        numberAminoAcidsAdded: addedAA,
                        numberAminoAcidsRemoved: removedAA,
                        proteinConsequence: term == null ? null : new ProteinConsequence(displayName: term),
                        proteinPositionStart: start,
                        proteinPositionEnd: end,
                        referenceDatabase: db == null ? null : new ReferenceDatabase(foreignDB: new ForeignDB(displayName: db)),
                        proteinSequenceReferenceAccessionNumber: accession
                )
        )

        when:
        def presentation = converter.convert(feature)

        then:
        presentation.proteinChangeStatement == display

        where:
        wtAA  | mutantAA | addedAA | removedAA | term                      | start | end  | db       | accession || display
        null  | null     | null    | null      | null                      | null  | null | null     | null      || ''
        'Gln' | 'Pro'    | null    | null      | null                      | null  | null | null     | null      || 'Gln>Pro'
        'Tyr' | null     | null    | null      | null                      | 400   | null | null     | null      || 'Tyr>STOP at position 400'
        null  | null     | 10      | null      | null                      | 312   | 322  | null     | null      || '10 AA added from position 312 to 322'
        null  | null     | null    | 14        | null                      | null  | null | 'PROTDB' | '10000'   || '14 AA missing in PROTDB:10000'
        null  | null     | null    | null      | 'elongated polypeptide'   | null  | null | null     | null      || 'Elongated Polypeptide'
        'Gln' | 'Tyr'    | null    | null      | 'amino acid substitution' | 90    | null | 'FooDB'  | '848484'  || 'Amino Acid Substitution: Gln>Tyr at position 90 in FooDB:848484'
    }
}
