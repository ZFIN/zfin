import React, {useEffect, useRef, useState} from 'react';
import SaveToast, {SaveEvent} from '../components/zirc/SaveToast';
import {Autocomplete, FieldDef, FieldRow, FieldsTable, Section, valueToInputString} from '../components/zirc/FormPrimitives';
import {
    ASSAY_FIELD_DEFS,
    ASSAY_TYPE_OPTIONS,
    LESION_FIELD_DEFS,
    LESION_TYPE_OPTIONS,
    visibleAssayFields,
    visibleLesionFields,
} from '../components/zirc/typeMatrices';

// ─── Wire types ────────────────────────────────────────────────────────────

interface GeneWire {
    id: number | null;
    sortOrder: number | null;
    mutatedGeneZdbId: string | null;
    mutatedGeneAbbreviation: string | null;
    linkageGroup: string | null;
    genbankGenomicDna: string | null;
    genbankCdna: string | null;
    sectionComplete: boolean | null;
}

interface LesionWire {
    id: number | null;
    sortOrder: number | null;
    lesionType: string | null;
    lesionSizeBp: number | null;
    nucleotideChange: string | null;
    deletedSequence: string | null;
    insertedSequence: string | null;
    transgeneSequence: string | null;
    locationInline: string | null;
    fivePrimeFlank: string | null;
    threePrimeFlank: string | null;
    hasLargeVariant: boolean | null;
    mutatedAminoAcids: string | null;
    mutatedAminoAcidsHgvs: string | null;
    additionalInfo: string | null;
}

interface GenotypingAssayWire {
    id: number | null;
    sortOrder: number | null;
    assayType: string | null;
    forwardPrimer: string | null;
    reversePrimer: string | null;
    expectedWtPcr: string | null;
    expectedMutPcr: string | null;
    restrictionEnzymeName: string | null;
    restrictionEnzymeCatalog: string | null;
    enzymeCleaves: string[] | null;
    expectedWtDigest: string | null;
    expectedMutDigest: string | null;
    additionalInfo: string | null;
    sequencingPrimer: string | null;
    dcapsMismatchPrimer: string | null;
    wtSpecificPrimer: string | null;
    mutSpecificPrimer: string | null;
    commonPrimer: string | null;
    kaspGenomicSequence: string | null;
    sslpMarkerName: string | null;
    sslpDistance: string | null;
    sslpGenomicLocation: string | null;
    sslpInducedBackground: string | null;
    sslpOutcrossedBackground: string | null;
    sslpInducedPcr: string | null;
    sslpOutcrossedPcr: string | null;
    chromatogramFilesAvailable: boolean | null;
    gelImagesAvailable: boolean | null;
    resultImagesAvailable: boolean | null;
    meltCurveFilesAvailable: boolean | null;
}

interface PhenotypeWire {
    id: number | null;
    sortOrder: number | null;
    description: string | null;
    hpfStart: number | null;
    hpfEnd: number | null;
    /** Server-managed cache derived from hpfStart. Read-only on the client. */
    stage: string | null;
    zfinImagePermission: boolean | null;
    zircImagePermission: boolean | null;
    nonMendelianPercentage: number | null;
    nonMendelianComment: string | null;
    segregation: string[] | null;
    type: string[] | null;
}

interface MutationDTO {
    id: number;
    lineSubmissionId: string;
    sortOrder: number | null;
    alleleDesignation: string | null;
    alleleInZfin: boolean | null;
    mutagenesisStage: string | null;
    mutagenesisProtocol: string | null;
    mutagenesisProtocolOther: string | null;
    molecularlyCharacterized: boolean | null;
    mutationType: string | null;
    homozygousLethal: boolean | null;
    lethalityStageTypical: string | null;
    lethalitySpecificTimepoint: string | null;
    lethalityWindowStart: string | null;
    lethalityWindowEnd: string | null;
    lethalityAdditionalInfo: string | null;
    zfinRecordEstablished: boolean | null;
    cellGenomicFeature: string | null;
    mutationDiscoverer: string | null;
    mutationInstitution: string | null;
    genes?: GeneWire[] | null;
    lesions?: LesionWire[] | null;
    genotypingAssays?: GenotypingAssayWire[] | null;
    phenotypes?: PhenotypeWire[] | null;
    publications?: string[] | null;
}

type ScalarField = Exclude<keyof MutationDTO,
    'id' | 'lineSubmissionId' | 'sortOrder'
    | 'genes' | 'lesions' | 'genotypingAssays' | 'phenotypes' | 'publications'>;

interface MutationEditProps {
    mutationId: string;
}

// ─── Scalar field defs ─────────────────────────────────────────────────────

const MUTAGENESIS_STAGE_OPTIONS = [
    {value: 'adult_females', label: 'Adult females'},
    {value: 'adult_males',   label: 'Adult males'},
    {value: 'embryos',       label: 'Embryos'},
    {value: 'sperm',         label: 'Sperm'},
];

const MUTAGENESIS_PROTOCOL_OPTIONS = [
    {value: 'crispr',              label: 'CRISPR'},
    {value: 'ems',                 label: 'EMS'},
    {value: 'enu',                 label: 'ENU'},
    {value: 'g_rays',              label: 'g-rays'},
    {value: 'spontaneous',         label: 'Spontaneous'},
    {value: 'talen',               label: 'TALEN'},
    {value: 'tmp',                 label: 'TMP'},
    {value: 'zinc_finger_nuclease', label: 'Zinc finger nuclease'},
    {value: 'other',               label: 'Other'},
];

const GENERAL_FIELDS: FieldDef<ScalarField>[] = [
    {field: 'alleleInZfin',             label: 'Allele in ZFIN',         type: 'bool',     idPrefix: 'mut'},
    // Two FieldDefs share `field: 'alleleDesignation'` but are gated on the
    // `alleleInZfin` checkbox: when checked, render the marker autocomplete;
    // when not, free text. `rowKey` keeps React happy about distinct keys.
    {rowKey: 'alleleDesignationAuto', field: 'alleleDesignation',
        label: 'Allele Designation', type: 'autocomplete', idPrefix: 'mut',
        autocompleteUrl: '/action/zirc/markers/search',
        placeholder: 'Search ZFIN markers…',
        visible: v => v.alleleInZfin === 'true'},
    {rowKey: 'alleleDesignationFree', field: 'alleleDesignation',
        label: 'Allele Designation', type: 'text', idPrefix: 'mut',
        visible: v => v.alleleInZfin !== 'true'},
    {field: 'mutagenesisStage',         label: 'Mutagenesis Stage',      type: 'select',   idPrefix: 'mut',
        options: MUTAGENESIS_STAGE_OPTIONS},
    {field: 'mutagenesisProtocol',      label: 'Mutagenesis Protocol',   type: 'select',   idPrefix: 'mut',
        options: MUTAGENESIS_PROTOCOL_OPTIONS},
    // Conditional companion when "Other" is picked above.
    {field: 'mutagenesisProtocolOther', label: 'Other Protocol',         type: 'text',     idPrefix: 'mut',
        visible: v => v.mutagenesisProtocol === 'other'},
    {field: 'molecularlyCharacterized', label: 'Molecularly Characterized', type: 'bool',  idPrefix: 'mut'},
    {field: 'mutationType',             label: 'Mutation Type',          type: 'text',     idPrefix: 'mut'},
    {field: 'zfinRecordEstablished',    label: 'ZFIN Record Established', type: 'bool',    idPrefix: 'mut'},
    // Per the form spec: only show the ZDB feature # input when "ZFIN
    // record established" is Yes.
    {field: 'cellGenomicFeature',       label: 'ZDB Genomic Feature #',  type: 'text',     idPrefix: 'mut',
        visible: v => v.zfinRecordEstablished === 'true'},
    {field: 'mutationDiscoverer',       label: 'Discoverer',             type: 'text',     idPrefix: 'mut'},
    {field: 'mutationInstitution',      label: 'Institution',            type: 'text',     idPrefix: 'mut'},
];

const LETHALITY_FIELDS: FieldDef<ScalarField>[] = [
    {field: 'homozygousLethal',           label: 'Homozygous Lethal',     type: 'bool',     idPrefix: 'mut'},
    {field: 'lethalityStageTypical',      label: 'Stage (Typical)',       type: 'text',     idPrefix: 'mut'},
    {field: 'lethalitySpecificTimepoint', label: 'Specific Timepoint',    type: 'text',     idPrefix: 'mut'},
    {field: 'lethalityWindowStart',       label: 'Window Start',          type: 'text',     idPrefix: 'mut'},
    {field: 'lethalityWindowEnd',         label: 'Window End',            type: 'text',     idPrefix: 'mut'},
    {field: 'lethalityAdditionalInfo',    label: 'Additional Info',       type: 'textarea', idPrefix: 'mut'},
];

const ALL_MUTATION_FIELDS: FieldDef<ScalarField>[] = [...GENERAL_FIELDS, ...LETHALITY_FIELDS];

// ─── Row identity + tiny parsers ───────────────────────────────────────────

let nextRowSeq = 1;
function freshRowId(prefix: string): string {
    return `${prefix}-${nextRowSeq++}`;
}

function trimOrNull(s: string): string | null {
    return s.trim() ? s.trim() : null;
}

function parseIntOrNull(s: string): number | null {
    if (!s.trim()) {
        return null;
    }
    const n = parseInt(s, 10);
    return Number.isFinite(n) ? n : null;
}

function parseFloatOrNull(s: string): number | null {
    if (!s.trim()) {
        return null;
    }
    const n = parseFloat(s);
    return Number.isFinite(n) ? n : null;
}

function csvToArray(s: string): string[] {
    return s.split(',').map(t => t.trim()).filter(t => t.length > 0);
}

function arrayToCsv(a: string[] | null | undefined): string {
    return (a ?? []).join(', ');
}

// ─── Per-collection row shapes + converters ───────────────────────────────

interface GeneRow {
    rowId: string;
    id: number | null;
    mutatedGeneZdbId: string;
    /** Server-supplied display echo. */
    mutatedGeneAbbreviation: string;
    linkageGroup: string;
    genbankGenomicDna: string;
    genbankCdna: string;
    /** '' / 'true' / 'false' — optional curator flag for "section done". */
    sectionComplete: '' | 'true' | 'false';
}

const GENE_ADAPTER: ChildAdapter<GeneWire, GeneRow> = {
    label: 'Genes',
    endpoint: 'save-genes',
    extractWire: dto => dto.genes,
    emptyRow: () => ({
        rowId: freshRowId('gene'),
        id: null,
        mutatedGeneZdbId: '',
        mutatedGeneAbbreviation: '',
        linkageGroup: '',
        genbankGenomicDna: '',
        genbankCdna: '',
        sectionComplete: '',
    }),
    wireToRow: w => ({
        rowId: freshRowId('gene'),
        id: w.id,
        mutatedGeneZdbId: w.mutatedGeneZdbId ?? '',
        mutatedGeneAbbreviation: w.mutatedGeneAbbreviation ?? '',
        linkageGroup: w.linkageGroup ?? '',
        genbankGenomicDna: w.genbankGenomicDna ?? '',
        genbankCdna: w.genbankCdna ?? '',
        sectionComplete: w.sectionComplete === true ? 'true'
            : w.sectionComplete === false ? 'false'
                : '',
    }),
    rowToWire: r => ({
        id: r.id,
        sortOrder: null,
        mutatedGeneZdbId: trimOrNull(r.mutatedGeneZdbId),
        mutatedGeneAbbreviation: null, // server-supplied; client doesn't echo back
        linkageGroup: trimOrNull(r.linkageGroup),
        genbankGenomicDna: trimOrNull(r.genbankGenomicDna),
        genbankCdna: trimOrNull(r.genbankCdna),
        sectionComplete: r.sectionComplete === 'true' ? true
            : r.sectionComplete === 'false' ? false
                : null,
    }),
    getId: r => r.id,
};

interface LesionRow {
    rowId: string;
    id: number | null;
    lesionType: string;
    lesionSizeBp: string;
    nucleotideChange: string;
    deletedSequence: string;
    insertedSequence: string;
    transgeneSequence: string;
    locationInline: string;
    fivePrimeFlank: string;
    threePrimeFlank: string;
    /** '' / 'true' / 'false' — same as bool radios elsewhere. */
    hasLargeVariant: '' | 'true' | 'false';
    mutatedAminoAcids: string;
    mutatedAminoAcidsHgvs: string;
    additionalInfo: string;
}

const LESION_ADAPTER: ChildAdapter<LesionWire, LesionRow> = {
    label: 'Lesions',
    endpoint: 'save-lesions',
    extractWire: dto => dto.lesions,
    emptyRow: () => ({
        rowId: freshRowId('lesion'),
        id: null,
        lesionType: '',
        lesionSizeBp: '',
        nucleotideChange: '',
        deletedSequence: '',
        insertedSequence: '',
        transgeneSequence: '',
        locationInline: '',
        fivePrimeFlank: '',
        threePrimeFlank: '',
        hasLargeVariant: '',
        mutatedAminoAcids: '',
        mutatedAminoAcidsHgvs: '',
        additionalInfo: '',
    }),
    wireToRow: w => ({
        rowId: freshRowId('lesion'),
        id: w.id,
        lesionType: w.lesionType ?? '',
        lesionSizeBp: w.lesionSizeBp == null ? '' : String(w.lesionSizeBp),
        nucleotideChange: w.nucleotideChange ?? '',
        deletedSequence: w.deletedSequence ?? '',
        insertedSequence: w.insertedSequence ?? '',
        transgeneSequence: w.transgeneSequence ?? '',
        locationInline: w.locationInline ?? '',
        fivePrimeFlank: w.fivePrimeFlank ?? '',
        threePrimeFlank: w.threePrimeFlank ?? '',
        hasLargeVariant: w.hasLargeVariant === true ? 'true'
            : w.hasLargeVariant === false ? 'false'
                : '',
        mutatedAminoAcids: w.mutatedAminoAcids ?? '',
        mutatedAminoAcidsHgvs: w.mutatedAminoAcidsHgvs ?? '',
        additionalInfo: w.additionalInfo ?? '',
    }),
    rowToWire: r => ({
        id: r.id,
        sortOrder: null,
        lesionType: trimOrNull(r.lesionType),
        lesionSizeBp: parseIntOrNull(r.lesionSizeBp),
        nucleotideChange: trimOrNull(r.nucleotideChange),
        deletedSequence: trimOrNull(r.deletedSequence),
        insertedSequence: trimOrNull(r.insertedSequence),
        transgeneSequence: trimOrNull(r.transgeneSequence),
        locationInline: trimOrNull(r.locationInline),
        fivePrimeFlank: trimOrNull(r.fivePrimeFlank),
        threePrimeFlank: trimOrNull(r.threePrimeFlank),
        hasLargeVariant: r.hasLargeVariant === 'true' ? true
            : r.hasLargeVariant === 'false' ? false
                : null,
        mutatedAminoAcids: trimOrNull(r.mutatedAminoAcids),
        mutatedAminoAcidsHgvs: trimOrNull(r.mutatedAminoAcidsHgvs),
        additionalInfo: trimOrNull(r.additionalInfo),
    }),
    getId: r => r.id,
};

interface GenotypingAssayRow {
    rowId: string;
    id: number | null;
    assayType: string;
    forwardPrimer: string;
    reversePrimer: string;
    expectedWtPcr: string;
    expectedMutPcr: string;
    restrictionEnzymeName: string;
    restrictionEnzymeCatalog: string;
    /** Subset of {'WT', 'MUT'} — the enzyme can cleave either or both. */
    enzymeCleavesWt: boolean;
    enzymeCleavesMut: boolean;
    expectedWtDigest: string;
    expectedMutDigest: string;
    additionalInfo: string;
    sequencingPrimer: string;
    dcapsMismatchPrimer: string;
    wtSpecificPrimer: string;
    mutSpecificPrimer: string;
    commonPrimer: string;
    kaspGenomicSequence: string;
    sslpMarkerName: string;
    sslpDistance: string;
    sslpGenomicLocation: string;
    sslpInducedBackground: string;
    sslpOutcrossedBackground: string;
    sslpInducedPcr: string;
    sslpOutcrossedPcr: string;
    /** '' / 'true' / 'false' — same as bool radios elsewhere. */
    chromatogramFilesAvailable: '' | 'true' | 'false';
    gelImagesAvailable: '' | 'true' | 'false';
    resultImagesAvailable: '' | 'true' | 'false';
    meltCurveFilesAvailable: '' | 'true' | 'false';
}

function triBoolFromWire(b: boolean | null): '' | 'true' | 'false' {
    return b === true ? 'true' : b === false ? 'false' : '';
}

function triBoolToWire(s: '' | 'true' | 'false'): boolean | null {
    return s === 'true' ? true : s === 'false' ? false : null;
}

const ASSAY_ADAPTER: ChildAdapter<GenotypingAssayWire, GenotypingAssayRow> = {
    label: 'Genotyping Assays',
    endpoint: 'save-genotyping-assays',
    extractWire: dto => dto.genotypingAssays,
    emptyRow: () => ({
        rowId: freshRowId('assay'),
        id: null,
        assayType: '',
        forwardPrimer: '',
        reversePrimer: '',
        expectedWtPcr: '',
        expectedMutPcr: '',
        restrictionEnzymeName: '',
        restrictionEnzymeCatalog: '',
        enzymeCleavesWt: false,
        enzymeCleavesMut: false,
        expectedWtDigest: '',
        expectedMutDigest: '',
        additionalInfo: '',
        sequencingPrimer: '',
        dcapsMismatchPrimer: '',
        wtSpecificPrimer: '',
        mutSpecificPrimer: '',
        commonPrimer: '',
        kaspGenomicSequence: '',
        sslpMarkerName: '',
        sslpDistance: '',
        sslpGenomicLocation: '',
        sslpInducedBackground: '',
        sslpOutcrossedBackground: '',
        sslpInducedPcr: '',
        sslpOutcrossedPcr: '',
        chromatogramFilesAvailable: '',
        gelImagesAvailable: '',
        resultImagesAvailable: '',
        meltCurveFilesAvailable: '',
    }),
    wireToRow: w => ({
        rowId: freshRowId('assay'),
        id: w.id,
        assayType: w.assayType ?? '',
        forwardPrimer: w.forwardPrimer ?? '',
        reversePrimer: w.reversePrimer ?? '',
        expectedWtPcr: w.expectedWtPcr ?? '',
        expectedMutPcr: w.expectedMutPcr ?? '',
        restrictionEnzymeName: w.restrictionEnzymeName ?? '',
        restrictionEnzymeCatalog: w.restrictionEnzymeCatalog ?? '',
        enzymeCleavesWt: (w.enzymeCleaves ?? []).includes('wt'),
        enzymeCleavesMut: (w.enzymeCleaves ?? []).includes('mut'),
        expectedWtDigest: w.expectedWtDigest ?? '',
        expectedMutDigest: w.expectedMutDigest ?? '',
        additionalInfo: w.additionalInfo ?? '',
        sequencingPrimer: w.sequencingPrimer ?? '',
        dcapsMismatchPrimer: w.dcapsMismatchPrimer ?? '',
        wtSpecificPrimer: w.wtSpecificPrimer ?? '',
        mutSpecificPrimer: w.mutSpecificPrimer ?? '',
        commonPrimer: w.commonPrimer ?? '',
        kaspGenomicSequence: w.kaspGenomicSequence ?? '',
        sslpMarkerName: w.sslpMarkerName ?? '',
        sslpDistance: w.sslpDistance ?? '',
        sslpGenomicLocation: w.sslpGenomicLocation ?? '',
        sslpInducedBackground: w.sslpInducedBackground ?? '',
        sslpOutcrossedBackground: w.sslpOutcrossedBackground ?? '',
        sslpInducedPcr: w.sslpInducedPcr ?? '',
        sslpOutcrossedPcr: w.sslpOutcrossedPcr ?? '',
        chromatogramFilesAvailable: triBoolFromWire(w.chromatogramFilesAvailable),
        gelImagesAvailable: triBoolFromWire(w.gelImagesAvailable),
        resultImagesAvailable: triBoolFromWire(w.resultImagesAvailable),
        meltCurveFilesAvailable: triBoolFromWire(w.meltCurveFilesAvailable),
    }),
    rowToWire: r => ({
        id: r.id,
        sortOrder: null,
        assayType: trimOrNull(r.assayType),
        forwardPrimer: trimOrNull(r.forwardPrimer),
        reversePrimer: trimOrNull(r.reversePrimer),
        expectedWtPcr: trimOrNull(r.expectedWtPcr),
        expectedMutPcr: trimOrNull(r.expectedMutPcr),
        restrictionEnzymeName: trimOrNull(r.restrictionEnzymeName),
        restrictionEnzymeCatalog: trimOrNull(r.restrictionEnzymeCatalog),
        enzymeCleaves: [
            ...(r.enzymeCleavesWt ? ['wt'] : []),
            ...(r.enzymeCleavesMut ? ['mut'] : []),
        ],
        expectedWtDigest: trimOrNull(r.expectedWtDigest),
        expectedMutDigest: trimOrNull(r.expectedMutDigest),
        additionalInfo: trimOrNull(r.additionalInfo),
        sequencingPrimer: trimOrNull(r.sequencingPrimer),
        dcapsMismatchPrimer: trimOrNull(r.dcapsMismatchPrimer),
        wtSpecificPrimer: trimOrNull(r.wtSpecificPrimer),
        mutSpecificPrimer: trimOrNull(r.mutSpecificPrimer),
        commonPrimer: trimOrNull(r.commonPrimer),
        kaspGenomicSequence: trimOrNull(r.kaspGenomicSequence),
        sslpMarkerName: trimOrNull(r.sslpMarkerName),
        sslpDistance: trimOrNull(r.sslpDistance),
        sslpGenomicLocation: trimOrNull(r.sslpGenomicLocation),
        sslpInducedBackground: trimOrNull(r.sslpInducedBackground),
        sslpOutcrossedBackground: trimOrNull(r.sslpOutcrossedBackground),
        sslpInducedPcr: trimOrNull(r.sslpInducedPcr),
        sslpOutcrossedPcr: trimOrNull(r.sslpOutcrossedPcr),
        chromatogramFilesAvailable: triBoolToWire(r.chromatogramFilesAvailable),
        gelImagesAvailable: triBoolToWire(r.gelImagesAvailable),
        resultImagesAvailable: triBoolToWire(r.resultImagesAvailable),
        meltCurveFilesAvailable: triBoolToWire(r.meltCurveFilesAvailable),
    }),
    getId: r => r.id,
};

interface PhenotypeRow {
    rowId: string;
    id: number | null;
    description: string;
    /** Timing as integer hpf in storage. The UI exposes a hpf/dpf unit
     *  toggle as a display convenience; on commit, dpf entries are
     *  multiplied by 24 before going on the wire. End is optional —
     *  empty means a single-point observation. */
    hpfStart: string;
    hpfEnd: string;
    /** Local-only display unit for the input. Doesn't go on the wire. */
    hpfUnit: 'hpf' | 'dpf';
    /** Server-derived from hpfStart. Rendered read-only. */
    stage: string;
    /** '' / 'true' / 'false' — same as bool radios elsewhere. */
    zfinImagePermission: '' | 'true' | 'false';
    zircImagePermission: '' | 'true' | 'false';
    nonMendelianPercentage: string;
    nonMendelianComment: string;
    /** Comma-separated for now; checkbox-group polish deferred. */
    segregationCsv: string;
    typeCsv: string;
}

const PHENOTYPE_ADAPTER: ChildAdapter<PhenotypeWire, PhenotypeRow> = {
    label: 'Phenotypes',
    endpoint: 'save-phenotypes',
    extractWire: dto => dto.phenotypes,
    emptyRow: () => ({
        rowId: freshRowId('phen'),
        id: null,
        description: '',
        hpfStart: '',
        hpfEnd: '',
        hpfUnit: 'hpf',
        stage: '',
        zfinImagePermission: '',
        zircImagePermission: '',
        nonMendelianPercentage: '',
        nonMendelianComment: '',
        segregationCsv: '',
        typeCsv: '',
    }),
    wireToRow: w => ({
        rowId: freshRowId('phen'),
        id: w.id,
        description: w.description ?? '',
        hpfStart: w.hpfStart == null ? '' : String(w.hpfStart),
        hpfEnd: w.hpfEnd == null ? '' : String(w.hpfEnd),
        hpfUnit: 'hpf',
        stage: w.stage ?? '',
        zfinImagePermission: w.zfinImagePermission === true ? 'true'
            : w.zfinImagePermission === false ? 'false'
                : '',
        zircImagePermission: w.zircImagePermission === true ? 'true'
            : w.zircImagePermission === false ? 'false'
                : '',
        nonMendelianPercentage: w.nonMendelianPercentage == null ? '' : String(w.nonMendelianPercentage),
        nonMendelianComment: w.nonMendelianComment ?? '',
        segregationCsv: arrayToCsv(w.segregation),
        typeCsv: arrayToCsv(w.type),
    }),
    rowToWire: r => ({
        id: r.id,
        sortOrder: null,
        description: trimOrNull(r.description),
        hpfStart: parseIntOrNull(r.hpfStart),
        hpfEnd: parseIntOrNull(r.hpfEnd),
        // Stage is server-managed; client doesn't echo it back.
        stage: null,
        zfinImagePermission: r.zfinImagePermission === 'true' ? true
            : r.zfinImagePermission === 'false' ? false
                : null,
        zircImagePermission: r.zircImagePermission === 'true' ? true
            : r.zircImagePermission === 'false' ? false
                : null,
        nonMendelianPercentage: parseFloatOrNull(r.nonMendelianPercentage),
        nonMendelianComment: trimOrNull(r.nonMendelianComment),
        segregation: csvToArray(r.segregationCsv),
        type: csvToArray(r.typeCsv),
    }),
    getId: r => r.id,
};

interface PublicationRow {
    rowId: string;
    publication: string;
}

// Publications wire as a flat string[] (the @ElementCollection on the
// server side handles the per-row sort-order column under the hood).
const PUBLICATION_ADAPTER: ChildAdapter<string, PublicationRow> = {
    label: 'Publications',
    endpoint: 'save-publications',
    extractWire: dto => dto.publications,
    emptyRow: () => ({
        rowId: freshRowId('pub'),
        publication: '',
    }),
    wireToRow: w => ({
        rowId: freshRowId('pub'),
        publication: w ?? '',
    }),
    rowToWire: r => r.publication.trim(),
};

// ─── Generic child-collection hook ────────────────────────────────────────

interface ChildAdapter<W, R extends {rowId: string}> {
    label: string;
    /** Final segment of the POST URL: full URL is `/action/zirc/mutation/{id}/{endpoint}`. */
    endpoint: string;
    extractWire: (dto: MutationDTO) => W[] | null | undefined;
    wireToRow: (w: W) => R;
    rowToWire: (r: R) => W;
    emptyRow: () => R;
    /** Optional persistent identifier accessor. When the apply() reconciler
     *  finds the same id on both sides, it preserves the local rowId so any
     *  per-row UI state (collapse, focus) survives the save roundtrip.
     *  Adapters whose rows don't carry an id (e.g. publications, where each
     *  row is just a string) can omit this; reconciliation falls back to
     *  pairing by list index. */
    getId?: (r: R) => number | null;
}

interface ChildCollection<R extends {rowId: string}> {
    rows: R[];
    apply: (dto: MutationDTO) => void;
    add: () => void;
    /** Like {@link add}, but seeds the new row with the given patch first.
     *  Used by the "pick type first" modal flow where the new row appears
     *  with the type the curator picked already set. */
    addWithPatch: (patch: Partial<R>) => void;
    remove: (rowId: string) => void;
    change: (rowId: string, patch: Partial<R>) => void;
    /** Commit the current rowsRef to the server (no-op if unchanged since last apply). */
    commit: () => void;
}

/**
 * State + commit machinery for one of a mutation's child collections
 * (genes, lesions, genotyping assays, phenotypes). The hook runs the same
 * replace-all save shape as the LineSubmissionEdit sections — diffing on
 * the client by JSON-equality of the wire-format payload, deferring to
 * the parent's serialization queue, and refreshing local state from the
 * server's response after each save.
 */
function useChildCollection<W, R extends {rowId: string}>(
    adapter: ChildAdapter<W, R>,
    deps: {
        mutationId: string;
        enqueueSave: <T>(fn: () => Promise<T>) => Promise<T>;
        emit: (event: Omit<SaveEvent, 'seq'>) => void;
    },
): ChildCollection<R> {
    const [rows, setRowsState] = useState<R[]>([]);
    const [committedJSON, setCommittedJSON] = useState<string>('[]');
    const rowsRef = useRef<R[]>([]);

    function setRows(next: R[]) {
        rowsRef.current = next;
        setRowsState(next);
    }

    function apply(dto: MutationDTO) {
        const wire = adapter.extractWire(dto) ?? [];
        const prev = rowsRef.current;
        const prevById = new Map<number, R>();
        if (adapter.getId) {
            for (const r of prev) {
                const id = adapter.getId(r);
                if (id != null) {
                    prevById.set(id, r);
                }
            }
        }
        // Reconcile incoming rows against the previous state so rowIds
        // stay stable across save roundtrips:
        //   (1) match by id when both sides have one
        //   (2) for new rows that don't yet have an id, pair with the
        //       prev row at the same index if it had no id either
        //       (covers the just-saved-for-the-first-time case)
        //   (3) otherwise mint a fresh rowId
        const next = wire.map((w, idx) => {
            const fresh = adapter.wireToRow(w);
            const id = adapter.getId ? adapter.getId(fresh) : null;
            if (id != null && prevById.has(id)) {
                return {...fresh, rowId: prevById.get(id)!.rowId};
            }
            const prevAtIdx = prev[idx];
            if (prevAtIdx && (!adapter.getId || adapter.getId(prevAtIdx) == null)) {
                return {...fresh, rowId: prevAtIdx.rowId};
            }
            return fresh;
        });
        setRows(next);
        // Snapshot in the same canonical form rowToWire produces so future
        // commits can dedupe by exact JSON equality.
        setCommittedJSON(JSON.stringify(next.map(adapter.rowToWire)));
    }

    async function commit() {
        const wire = rowsRef.current.map(adapter.rowToWire);
        const wireJSON = JSON.stringify(wire);
        if (wireJSON === committedJSON) {
            return;
        }
        deps.emit({status: 'saving', label: adapter.label});
        try {
            await deps.enqueueSave(async () => {
                const url = `/action/zirc/mutation/${deps.mutationId}/${adapter.endpoint}`;
                const resp = await fetch(url, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: wireJSON,
                });
                if (!resp.ok) {
                    let detail = `HTTP ${resp.status}`;
                    try {
                        const err = await resp.json();
                        if (err && err.detail) {
                            detail = err.detail;
                        }
                    } catch { /* swallow */ }
                    throw new Error(detail);
                }
                const data = await resp.json() as MutationDTO;
                apply(data);
            });
            deps.emit({status: 'saved', label: adapter.label});
        } catch (e) {
            deps.emit({status: 'error', label: adapter.label,
                message: e instanceof Error ? e.message : 'Save failed'});
        }
    }

    function add() {
        // Don't commit on add — empty rows would either fail validation or
        // be saved as empty; let the user fill in fields and blur first.
        setRows([...rowsRef.current, adapter.emptyRow()]);
    }

    function addWithPatch(patch: Partial<R>) {
        setRows([...rowsRef.current, {...adapter.emptyRow(), ...patch}]);
    }

    function remove(rowId: string) {
        setRows(rowsRef.current.filter(r => r.rowId !== rowId));
        commit();
    }

    function change(rowId: string, patch: Partial<R>) {
        setRows(rowsRef.current.map(r => r.rowId === rowId ? {...r, ...patch} : r));
    }

    return {rows, apply, add, addWithPatch, remove, change, commit};
}

// ─── Per-row UI helpers ────────────────────────────────────────────────────

interface RowFieldsetProps {
    title: string;
    onRemove: () => void;
    /** Optional callback for the Done button; only rendered when supplied
     *  and the row is expanded. */
    onDone?: () => void;
    /** When true, the row renders compactly: title + summary + Edit/Remove
     *  buttons, no fieldset body. */
    collapsed?: boolean;
    /** Required when `collapsed` is true; rendered in place of children. */
    summary?: React.ReactNode;
    /** Required when `collapsed` is true; called by the Edit button. */
    onEdit?: () => void;
    children: React.ReactNode;
}

const RowFieldset = ({title, onRemove, onDone, collapsed, summary, onEdit, children}: RowFieldsetProps) => {
    if (collapsed) {
        return (
            <div className='border rounded p-2 mb-2 d-flex align-items-center'>
                <div className='flex-grow-1' style={{minWidth: 0}}>
                    <span className='text-muted small mr-2'>{title}</span>
                    <span style={{overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap'}}>
                        {summary ?? <span className='text-muted'>(empty)</span>}
                    </span>
                </div>
                <div style={{flexShrink: 0}}>
                    {onEdit && (
                        <button type='button' className='btn btn-sm btn-outline-secondary mr-2' onClick={onEdit}>
                            Edit
                        </button>
                    )}
                    <button type='button' className='btn btn-sm btn-outline-danger' onClick={onRemove}>
                        Remove
                    </button>
                </div>
            </div>
        );
    }
    return (
        <fieldset className='border rounded p-3 mb-3'>
            <legend className='h6 px-2' style={{width: 'auto'}}>{title}</legend>
            {children}
            <div className='text-right mt-2'>
                {onDone && (
                    <button
                        type='button'
                        className='btn btn-sm btn-outline-primary mr-2'
                        onClick={onDone}
                    >
                        Done
                    </button>
                )}
                <button
                    type='button'
                    className='btn btn-sm btn-outline-danger'
                    onClick={onRemove}
                >
                    Remove
                </button>
            </div>
        </fieldset>
    );
};

/**
 * Per-section expansion state. New rows (id == null) default to expanded;
 * rows already on the server default to collapsed. Explicit user choices
 * (Done / Edit) override the default and persist for the component's
 * lifetime.
 */
function useRowExpansion<R extends {rowId: string, id: number | null}>() {
    const [expandedSet, setExpandedSet] = useState<Set<string>>(new Set());
    const [collapsedSet, setCollapsedSet] = useState<Set<string>>(new Set());

    function isExpanded(row: R): boolean {
        if (expandedSet.has(row.rowId)) {
            return true;
        }
        if (collapsedSet.has(row.rowId)) {
            return false;
        }
        return row.id == null; // default: new → expanded, persisted → collapsed
    }

    function collapse(rowId: string) {
        setExpandedSet(prev => { const next = new Set(prev); next.delete(rowId); return next; });
        setCollapsedSet(prev => new Set(prev).add(rowId));
    }

    function expand(rowId: string) {
        setCollapsedSet(prev => { const next = new Set(prev); next.delete(rowId); return next; });
        setExpandedSet(prev => new Set(prev).add(rowId));
    }

    return {isExpanded, collapse, expand};
}

interface TypePickerModalProps {
    open: boolean;
    title: string;
    description?: string;
    options: Array<{value: string; label: string}>;
    onPick: (value: string) => void;
    onCancel: () => void;
}

/**
 * Self-contained "pick the type first" modal for adding a new lesion or
 * genotyping assay. The PDF spec calls for the curator to pick a type
 * before the type-specific field set appears; the modal makes that step
 * explicit. No jQuery dependency — just a controlled overlay with
 * Escape-to-cancel and a backdrop click that doesn't dismiss (so a
 * mis-click doesn't lose the half-typed picker context elsewhere on
 * the page).
 */
const TypePickerModal = ({open, title, description, options, onPick, onCancel}: TypePickerModalProps) => {
    useEffect(() => {
        if (!open) {
            return;
        }
        const onKey = (e: KeyboardEvent) => {
            if (e.key === 'Escape') {
                onCancel();
            }
        };
        window.addEventListener('keydown', onKey);
        return () => window.removeEventListener('keydown', onKey);
    }, [open, onCancel]);

    if (!open) {
        return null;
    }
    return (
        <div
            role='dialog'
            aria-modal='true'
            aria-label={title}
            style={{
                position: 'fixed',
                inset: 0,
                background: 'rgba(0,0,0,0.4)',
                zIndex: 1060,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
            }}
        >
            <div className='card shadow' style={{minWidth: 320, maxWidth: 480}}>
                <div className='card-body'>
                    <h5 className='card-title mb-2'>{title}</h5>
                    {description && <p className='text-muted small mb-3'>{description}</p>}
                    <div className='list-group'>
                        {options.map(opt => (
                            <button
                                key={opt.value}
                                type='button'
                                className='list-group-item list-group-item-action text-left'
                                onClick={() => onPick(opt.value)}
                            >
                                {opt.label}
                            </button>
                        ))}
                    </div>
                    <div className='text-right mt-3'>
                        <button type='button' className='btn btn-outline-secondary' onClick={onCancel}>
                            Cancel
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

interface TextRowFieldProps {
    id: string;
    label: string;
    value: string;
    type?: 'text' | 'number';
    placeholder?: string;
    /** Right-side unit label (e.g. "bp"). When set, the input gets a tighter
     *  max-width so the unit reads naturally next to a short numeric value. */
    suffix?: string;
    /** Optional HTML5 pattern attribute (regex). Drives the browser's
     *  built-in validity styling — invalid input gets the :invalid
     *  pseudo-class red border. The server is still authoritative. */
    pattern?: string;
    /** Hint rendered as muted help text under the input. */
    helpText?: string;
    /** When set, an "i" link rendered next to the label opens this URL. */
    infoHref?: string;
    onChange: (next: string) => void;
    onCommit: () => void;
}

const InfoLink = ({href, label}: {href: string; label: string}) => (
    <a
        href={href}
        target='_blank'
        rel='noopener noreferrer'
        className='text-info small ml-1'
        title={`More about ${label}`}
        style={{textDecoration: 'none'}}
    >
        (i)
    </a>
);

const TextRowField = ({id, label, value, type = 'text', placeholder, suffix, pattern, helpText, infoHref, onChange, onCommit}: TextRowFieldProps) => (
    <div className='form-group row'>
        <label htmlFor={id} className='col-sm-3 col-form-label'>
            {label}
            {infoHref && <InfoLink href={infoHref} label={label}/>}
        </label>
        <div className='col-sm-9'>
            <div className='d-flex align-items-center' style={{gap: 8}}>
                <input
                    type={type}
                    id={id}
                    className='form-control'
                    placeholder={placeholder}
                    value={value}
                    onChange={e => onChange(e.target.value)}
                    onBlur={onCommit}
                    step={type === 'number' ? 'any' : undefined}
                    pattern={pattern}
                    style={suffix ? {maxWidth: 180} : undefined}
                />
                {suffix && <span className='text-muted small'>{suffix}</span>}
            </div>
            {helpText && <small className='form-text text-muted'>{helpText}</small>}
        </div>
    </div>
);

const TextAreaRowField = ({id, label, value, onChange, onCommit}: TextRowFieldProps) => (
    <div className='form-group row mb-0'>
        <label htmlFor={id} className='col-sm-3 col-form-label'>{label}</label>
        <div className='col-sm-9'>
            <textarea
                id={id}
                className='form-control'
                rows={2}
                value={value}
                onChange={e => onChange(e.target.value)}
                onBlur={onCommit}
            />
        </div>
    </div>
);

interface BoolRowFieldProps {
    groupName: string;
    label: string;
    value: '' | 'true' | 'false';
    onChange: (next: 'true' | 'false') => void;
    onCommit: () => void;
}

const BoolRowField = ({groupName, label, value, onChange, onCommit}: BoolRowFieldProps) => {
    const labelId = `${groupName}-label`;
    const options: Array<['true' | 'false', string]> = [['true', 'Yes'], ['false', 'No']];
    return (
        <div className='form-group row'>
            <span id={labelId} className='col-sm-3 col-form-label'>{label}</span>
            <div className='col-sm-9' role='radiogroup' aria-labelledby={labelId}>
                {options.map(([v, lbl]) => {
                    const id = `${groupName}-${v}`;
                    return (
                        <div className='form-check form-check-inline' key={v}>
                            <input
                                type='radio'
                                id={id}
                                className='form-check-input'
                                name={groupName}
                                value={v}
                                checked={value === v}
                                onChange={() => { onChange(v); onCommit(); }}
                            />
                            <label className='form-check-label' htmlFor={id}>{lbl}</label>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

interface CheckboxRowFieldProps {
    id: string;
    label: string;
    value: boolean;
    onChange: (next: boolean) => void;
    onCommit: () => void;
}

/** Binary checkbox row. Use for fields that are genuinely true/false
 *  (no "unanswered" state) — e.g. "enzyme cleaves WT?" / "enzyme
 *  cleaves MUT?", both of which can be independently on. For tri-state
 *  Yes/No/unanswered, use {@link BoolRowField}. */
const CheckboxRowField = ({id, label, value, onChange, onCommit}: CheckboxRowFieldProps) => (
    <div className='form-group row'>
        <span className='col-sm-3 col-form-label'>{label}</span>
        <div className='col-sm-9'>
            <div className='form-check'>
                <input
                    type='checkbox'
                    id={id}
                    className='form-check-input'
                    checked={value}
                    onChange={e => { onChange(e.target.checked); onCommit(); }}
                />
                <label className='form-check-label' htmlFor={id}>Yes</label>
            </div>
        </div>
    </div>
);

interface AutocompleteRowFieldProps {
    id: string;
    label: string;
    value: string;
    fetchUrl: string;
    placeholder?: string;
    onChange: (next: string) => void;
    onCommit: () => void;
}

const AutocompleteRowField = ({id, label, value, fetchUrl, placeholder, onChange, onCommit}: AutocompleteRowFieldProps) => (
    <div className='form-group row'>
        <label htmlFor={id} className='col-sm-3 col-form-label'>{label}</label>
        <div className='col-sm-9'>
            <Autocomplete
                id={id}
                value={value}
                placeholder={placeholder}
                fetchUrl={fetchUrl}
                onChange={onChange}
                onCommit={() => onCommit()}
            />
        </div>
    </div>
);

interface SelectRowFieldProps {
    id: string;
    label: string;
    value: string;
    options: Array<{value: string; label: string}>;
    onChange: (next: string) => void;
    onCommit: () => void;
}

const SelectRowField = ({id, label, value, options, onChange, onCommit}: SelectRowFieldProps) => (
    <div className='form-group row'>
        <label htmlFor={id} className='col-sm-3 col-form-label'>{label}</label>
        <div className='col-sm-9'>
            <select
                id={id}
                className='form-control'
                value={value}
                onChange={e => { onChange(e.target.value); onCommit(); }}
            >
                <option value=''>(select)</option>
                {options.map(opt => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
            </select>
        </div>
    </div>
);

/** Mirrors LineSubmissionService.MAX_CHILD_ROWS_PER_MUTATION. */
const MAX_CHILD_ROWS = 10;

interface AddButtonProps {
    label: string;
    onAdd: () => void;
    /** When true, the button is disabled with an explanatory tooltip. */
    disabled?: boolean;
    /** Tooltip shown when disabled. */
    disabledReason?: string;
}

const AddButton = ({label, onAdd, disabled, disabledReason}: AddButtonProps) => (
    <button
        type='button'
        className='btn btn-sm btn-outline-secondary'
        onClick={onAdd}
        disabled={disabled}
        title={disabled ? disabledReason : undefined}
    >
        + Add {label}
    </button>
);

function capProps(rows: unknown[], kind: string): {disabled: boolean; disabledReason?: string} {
    if (rows.length >= MAX_CHILD_ROWS) {
        return {disabled: true, disabledReason: `Maximum ${MAX_CHILD_ROWS} ${kind} rows per mutation.`};
    }
    return {disabled: false};
}

// ─── Per-collection section components ────────────────────────────────────

interface SectionListProps<R> {
    rows: R[];
    onAdd: () => void;
    onRemove: (rowId: string) => void;
    onChange: (rowId: string, patch: Partial<R>) => void;
    onCommit: () => void;
}

function summarizeGene(row: GeneRow): string {
    const id = row.mutatedGeneAbbreviation || row.mutatedGeneZdbId || '(no gene selected)';
    const lg = row.linkageGroup ? ` · LG ${row.linkageGroup}` : '';
    return `${id}${lg}`;
}

const GenesSection = ({rows, onAdd, onRemove, onChange, onCommit}: SectionListProps<GeneRow>) => {
    const expansion = useRowExpansion<typeof rows[number]>();
    if (rows.length === 0) {
        return <div><p className='text-muted'>No genes recorded for this mutation.</p><AddButton label='gene' onAdd={onAdd} {...capProps(rows, 'gene')}/></div>;
    }
    return (
        <div>
            {rows.map((row, idx) => (
                <RowFieldset
                    key={row.rowId}
                    title={`Gene ${idx + 1}`}
                    collapsed={!expansion.isExpanded(row)}
                    summary={summarizeGene(row)}
                    onEdit={() => expansion.expand(row.rowId)}
                    onDone={() => expansion.collapse(row.rowId)}
                    onRemove={() => onRemove(row.rowId)}
                >
                    <div className='form-group row'>
                        <label htmlFor={`gene-zdb-${row.rowId}`} className='col-sm-3 col-form-label'>Mutated Gene ZDB ID</label>
                        <div className='col-sm-9'>
                            <input
                                type='text'
                                id={`gene-zdb-${row.rowId}`}
                                className='form-control'
                                placeholder='ZDB-GENE-…'
                                value={row.mutatedGeneZdbId}
                                onChange={e => onChange(row.rowId, {mutatedGeneZdbId: e.target.value})}
                                onBlur={onCommit}
                            />
                            {row.mutatedGeneAbbreviation && (
                                <small className='form-text text-muted'>
                                    Resolved: <em>{row.mutatedGeneAbbreviation}</em>
                                </small>
                            )}
                        </div>
                    </div>
                    <AutocompleteRowField
                        id={`gene-lg-${row.rowId}`}
                        label='Linkage Group'
                        value={row.linkageGroup}
                        fetchUrl='/action/zirc/chromosomes/search'
                        placeholder='1, 2, …, MT'
                        onChange={v => onChange(row.rowId, {linkageGroup: v})}
                        onCommit={onCommit}
                    />
                    <TextRowField id={`gene-gdna-${row.rowId}`}  label='GenBank gDNA'  value={row.genbankGenomicDna} onChange={v => onChange(row.rowId, {genbankGenomicDna: v})} onCommit={onCommit}/>
                    <TextAreaRowField id={`gene-cdna-${row.rowId}`} label='GenBank cDNA' value={row.genbankCdna} onChange={v => onChange(row.rowId, {genbankCdna: v})} onCommit={onCommit}/>
                    <BoolRowField
                        groupName={`gene-done-${row.rowId}`}
                        label='Section complete?'
                        value={row.sectionComplete}
                        onChange={v => onChange(row.rowId, {sectionComplete: v})}
                        onCommit={onCommit}
                    />
                </RowFieldset>
            ))}
            <AddButton label='gene' onAdd={onAdd} {...capProps(rows, 'gene')}/>
        </div>
    );
};

function summarizeLesion(row: LesionRow): string {
    const typeLabel = LESION_TYPE_OPTIONS.find(o => o.value === row.lesionType)?.label
        ?? row.lesionType;
    const head = typeLabel || '(no type)';
    const size = row.lesionSizeBp ? `${row.lesionSizeBp} bp` : '';
    const detail = row.nucleotideChange
        || row.deletedSequence
        || row.insertedSequence
        || row.transgeneSequence
        || row.locationInline;
    const parts = [head, size, detail].filter(Boolean);
    return parts.join(' · ');
}

// Cleared on a lesion type change so stale type-specific fields don't leak
// across types. The two genuinely cross-type fields (mutatedAminoAcids,
// additionalInfo) survive. hasLargeVariant resets too — the new type may
// not even surface the toggle.
const LESION_TYPE_SPECIFIC_PATCH: Partial<LesionRow> = {
    lesionSizeBp: '',
    nucleotideChange: '',
    deletedSequence: '',
    insertedSequence: '',
    transgeneSequence: '',
    locationInline: '',
    fivePrimeFlank: '',
    threePrimeFlank: '',
    hasLargeVariant: '',
};

interface LesionsSectionProps {
    rows: LesionRow[];
    onAddWithType: (type: string) => void;
    onRemove: (rowId: string) => void;
    onChange: (rowId: string, patch: Partial<LesionRow>) => void;
    onCommit: () => void;
}

const LesionsSection = ({rows, onAddWithType, onRemove, onChange, onCommit}: LesionsSectionProps) => {
    const expansion = useRowExpansion<LesionRow>();
    const [pickerOpen, setPickerOpen] = useState(false);

    function handlePick(type: string) {
        setPickerOpen(false);
        onAddWithType(type);
    }

    function handleTypeChange(row: LesionRow, newType: string) {
        if (newType === row.lesionType) {
            return;
        }
        // Clear type-specific fields so stale data from the previous type
        // doesn't end up persisted under the new type's interpretation.
        onChange(row.rowId, {...LESION_TYPE_SPECIFIC_PATCH, lesionType: newType});
        onCommit();
    }

    return (
        <div>
            {rows.length === 0
                ? <p className='text-muted'>No lesions recorded for this mutation.</p>
                : rows.map((row, idx) => {
                    const fields = visibleLesionFields(row.lesionType, row.hasLargeVariant === 'true');
                    return (
                        <RowFieldset
                            key={row.rowId}
                            title={`Lesion ${idx + 1}`}
                            collapsed={!expansion.isExpanded(row)}
                            summary={summarizeLesion(row)}
                            onEdit={() => expansion.expand(row.rowId)}
                            onDone={() => expansion.collapse(row.rowId)}
                            onRemove={() => onRemove(row.rowId)}
                        >
                            <SelectRowField
                                id={`les-type-${row.rowId}`}
                                label='Type'
                                value={row.lesionType}
                                options={LESION_TYPE_OPTIONS as Array<{value: string; label: string}>}
                                onChange={v => handleTypeChange(row, v)}
                                onCommit={() => { /* type change already committed by handleTypeChange */ }}
                            />
                            {fields.map(fieldKey => {
                                const def = LESION_FIELD_DEFS[fieldKey];
                                const id = `les-${fieldKey}-${row.rowId}`;
                                if (def.type === 'bool') {
                                    return (
                                        <BoolRowField
                                            key={fieldKey}
                                            groupName={id}
                                            label={def.label}
                                            value={row.hasLargeVariant}
                                            onChange={v => onChange(row.rowId, {hasLargeVariant: v})}
                                            onCommit={onCommit}
                                        />
                                    );
                                }
                                if (def.type === 'textarea') {
                                    return (
                                        <TextAreaRowField
                                            key={fieldKey}
                                            id={id}
                                            label={def.label}
                                            value={(row[fieldKey] as string) ?? ''}
                                            placeholder={def.placeholder}
                                            onChange={v => onChange(row.rowId, {[fieldKey]: v} as Partial<LesionRow>)}
                                            onCommit={onCommit}
                                        />
                                    );
                                }
                                return (
                                    <TextRowField
                                        key={fieldKey}
                                        id={id}
                                        label={def.label}
                                        value={(row[fieldKey] as string) ?? ''}
                                        type={def.type === 'int' ? 'number' : 'text'}
                                        placeholder={def.placeholder}
                                        suffix={def.suffix}
                                        helpText={def.helpText}
                                        infoHref={def.infoHref}
                                        onChange={v => onChange(row.rowId, {[fieldKey]: v} as Partial<LesionRow>)}
                                        onCommit={onCommit}
                                    />
                                );
                            })}
                        </RowFieldset>
                    );
                })}
            <AddButton label='lesion' onAdd={() => setPickerOpen(true)} {...capProps(rows, 'lesion')}/>
            <TypePickerModal
                open={pickerOpen}
                title='Add a lesion'
                description='Pick the lesion type — the form will then show only the fields that apply.'
                options={LESION_TYPE_OPTIONS as Array<{value: string; label: string}>}
                onPick={handlePick}
                onCancel={() => setPickerOpen(false)}
            />
        </div>
    );
};

function summarizeAssay(row: GenotypingAssayRow): string {
    const typeLabel = ASSAY_TYPE_OPTIONS.find(o => o.value === row.assayType)?.label
        ?? row.assayType;
    const head = typeLabel || '(no type)';
    const detail = row.forwardPrimer || row.wtSpecificPrimer || row.sslpMarkerName;
    return detail ? `${head} · ${detail}` : head;
}

// Cleared on an assay type change so stale type-specific data doesn't
// linger across types. assayType itself is set by the caller; additionalInfo
// is left alone (it applies to every type).
const ASSAY_TYPE_SPECIFIC_PATCH: Partial<GenotypingAssayRow> = {
    forwardPrimer: '',
    reversePrimer: '',
    expectedWtPcr: '',
    expectedMutPcr: '',
    restrictionEnzymeName: '',
    restrictionEnzymeCatalog: '',
    enzymeCleavesWt: false,
    enzymeCleavesMut: false,
    expectedWtDigest: '',
    expectedMutDigest: '',
    sequencingPrimer: '',
    dcapsMismatchPrimer: '',
    wtSpecificPrimer: '',
    mutSpecificPrimer: '',
    commonPrimer: '',
    kaspGenomicSequence: '',
    sslpMarkerName: '',
    sslpDistance: '',
    sslpGenomicLocation: '',
    sslpInducedBackground: '',
    sslpOutcrossedBackground: '',
    sslpInducedPcr: '',
    sslpOutcrossedPcr: '',
    chromatogramFilesAvailable: '',
    gelImagesAvailable: '',
    resultImagesAvailable: '',
    meltCurveFilesAvailable: '',
};

interface GenotypingAssaysSectionProps {
    rows: GenotypingAssayRow[];
    onAddWithType: (type: string) => void;
    onRemove: (rowId: string) => void;
    onChange: (rowId: string, patch: Partial<GenotypingAssayRow>) => void;
    onCommit: () => void;
}

const GenotypingAssaysSection = ({rows, onAddWithType, onRemove, onChange, onCommit}: GenotypingAssaysSectionProps) => {
    const expansion = useRowExpansion<GenotypingAssayRow>();
    const [pickerOpen, setPickerOpen] = useState(false);

    function handlePick(type: string) {
        setPickerOpen(false);
        onAddWithType(type);
    }

    function handleTypeChange(row: GenotypingAssayRow, newType: string) {
        if (newType === row.assayType) {
            return;
        }
        onChange(row.rowId, {...ASSAY_TYPE_SPECIFIC_PATCH, assayType: newType});
        onCommit();
    }

    return (
        <div>
            {rows.length === 0
                ? <p className='text-muted'>No genotyping assays recorded for this mutation.</p>
                : rows.map((row, idx) => {
                    const fields = visibleAssayFields(row.assayType);
                    return (
                        <RowFieldset
                            key={row.rowId}
                            title={`Genotyping Assay ${idx + 1}`}
                            collapsed={!expansion.isExpanded(row)}
                            summary={summarizeAssay(row)}
                            onEdit={() => expansion.expand(row.rowId)}
                            onDone={() => expansion.collapse(row.rowId)}
                            onRemove={() => onRemove(row.rowId)}
                        >
                            <SelectRowField
                                id={`asy-type-${row.rowId}`}
                                label='Type'
                                value={row.assayType}
                                options={ASSAY_TYPE_OPTIONS as Array<{value: string; label: string}>}
                                onChange={v => handleTypeChange(row, v)}
                                onCommit={() => { /* committed by handleTypeChange */ }}
                            />
                            {fields.map(fieldKey => {
                                const def = ASSAY_FIELD_DEFS[fieldKey];
                                const id = `asy-${fieldKey}-${row.rowId}`;
                                if (def.type === 'checkbox') {
                                    return (
                                        <CheckboxRowField
                                            key={fieldKey}
                                            id={id}
                                            label={def.label}
                                            value={row[fieldKey] as boolean}
                                            onChange={v => onChange(row.rowId, {[fieldKey]: v} as Partial<GenotypingAssayRow>)}
                                            onCommit={onCommit}
                                        />
                                    );
                                }
                                if (def.type === 'bool') {
                                    return (
                                        <BoolRowField
                                            key={fieldKey}
                                            groupName={id}
                                            label={def.label}
                                            value={row[fieldKey] as '' | 'true' | 'false'}
                                            onChange={v => onChange(row.rowId, {[fieldKey]: v} as Partial<GenotypingAssayRow>)}
                                            onCommit={onCommit}
                                        />
                                    );
                                }
                                if (def.type === 'textarea') {
                                    return (
                                        <TextAreaRowField
                                            key={fieldKey}
                                            id={id}
                                            label={def.label}
                                            value={(row[fieldKey] as string) ?? ''}
                                            placeholder={def.placeholder}
                                            onChange={v => onChange(row.rowId, {[fieldKey]: v} as Partial<GenotypingAssayRow>)}
                                            onCommit={onCommit}
                                        />
                                    );
                                }
                                return (
                                    <TextRowField
                                        key={fieldKey}
                                        id={id}
                                        label={def.label}
                                        value={(row[fieldKey] as string) ?? ''}
                                        placeholder={def.placeholder}
                                        suffix={def.suffix}
                                        pattern={def.pattern}
                                        helpText={def.helpText}
                                        onChange={v => onChange(row.rowId, {[fieldKey]: v} as Partial<GenotypingAssayRow>)}
                                        onCommit={onCommit}
                                    />
                                );
                            })}
                        </RowFieldset>
                    );
                })}
            <AddButton label='genotyping assay' onAdd={() => setPickerOpen(true)} {...capProps(rows, 'genotyping assay')}/>
            <TypePickerModal
                open={pickerOpen}
                title='Add a genotyping assay'
                description='Pick the assay type — the form will then show only the fields that apply.'
                options={ASSAY_TYPE_OPTIONS as Array<{value: string; label: string}>}
                onPick={handlePick}
                onCancel={() => setPickerOpen(false)}
            />
        </div>
    );
};

function summarizePhenotype(row: PhenotypeRow): string {
    const desc = row.description || '(no description)';
    const timing = row.hpfEnd && row.hpfStart
        ? `${row.hpfStart}–${row.hpfEnd} hpf`
        : row.hpfStart ? `${row.hpfStart} hpf`
            : row.stage || '';
    return timing ? `${desc} · ${timing}` : desc;
}

// Convert a stored-as-hpf string into the display unit (lossless when
// dpf divides evenly; rounded to 2 decimals otherwise).
function hpfStringInUnit(hpfStr: string, unit: 'hpf' | 'dpf'): string {
    const s = hpfStr.trim();
    if (!s) {
        return '';
    }
    const h = Number(s);
    if (!Number.isFinite(h)) {
        return s;
    }
    if (unit === 'hpf') {
        return String(h);
    }
    const d = h / 24;
    return Number.isInteger(d) ? String(d) : d.toFixed(2);
}

function inputToHpfString(input: string, unit: 'hpf' | 'dpf'): string {
    const s = input.trim();
    if (!s) {
        return '';
    }
    const n = Number(s);
    if (!Number.isFinite(n)) {
        return s; // keep raw, will fall out as null in parseIntOrNull
    }
    return String(Math.round(unit === 'dpf' ? n * 24 : n));
}

interface PhenotypeTimingRowProps {
    rowId: string;
    hpfStart: string;
    hpfEnd: string;
    unit: 'hpf' | 'dpf';
    stage: string;
    onChange: (patch: Partial<PhenotypeRow>) => void;
    onCommit: () => void;
}

const PhenotypeTimingRow = ({rowId, hpfStart, hpfEnd, unit, stage, onChange, onCommit}: PhenotypeTimingRowProps) => {
    const unitGroupName = `phn-unit-${rowId}`;
    const startId = `phn-hpf-start-${rowId}`;
    const endId   = `phn-hpf-end-${rowId}`;
    return (
        <div className='form-group row'>
            <span className='col-sm-3 col-form-label'>Timing</span>
            <div className='col-sm-9'>
                <div className='d-flex align-items-center flex-wrap' style={{gap: 12}}>
                    <div role='radiogroup' aria-label='Unit'>
                        {(['hpf', 'dpf'] as const).map(u => {
                            const id = `${unitGroupName}-${u}`;
                            return (
                                <div className='form-check form-check-inline' key={u}>
                                    <input
                                        type='radio'
                                        id={id}
                                        className='form-check-input'
                                        name={unitGroupName}
                                        value={u}
                                        checked={unit === u}
                                        onChange={() => onChange({hpfUnit: u})}
                                    />
                                    <label className='form-check-label' htmlFor={id}>{u}</label>
                                </div>
                            );
                        })}
                    </div>
                    <label htmlFor={startId} className='mb-0 small text-muted'>Start</label>
                    <input
                        id={startId}
                        type='number'
                        step='any'
                        className='form-control'
                        style={{maxWidth: 120}}
                        value={hpfStringInUnit(hpfStart, unit)}
                        onChange={e => onChange({hpfStart: inputToHpfString(e.target.value, unit)})}
                        onBlur={onCommit}
                    />
                    <label htmlFor={endId} className='mb-0 small text-muted'>End</label>
                    <input
                        id={endId}
                        type='number'
                        step='any'
                        className='form-control'
                        style={{maxWidth: 120}}
                        placeholder='(optional)'
                        value={hpfStringInUnit(hpfEnd, unit)}
                        onChange={e => onChange({hpfEnd: inputToHpfString(e.target.value, unit)})}
                        onBlur={onCommit}
                    />
                </div>
                {stage && (
                    <small className='form-text text-muted'>
                        Stage at 28.5°C: <em>{stage}</em>
                    </small>
                )}
            </div>
        </div>
    );
};

const PhenotypesSection = ({rows, onAdd, onRemove, onChange, onCommit}: SectionListProps<PhenotypeRow>) => {
    const expansion = useRowExpansion<typeof rows[number]>();
    if (rows.length === 0) {
        return <div><p className='text-muted'>No phenotypes recorded for this mutation.</p><AddButton label='phenotype' onAdd={onAdd} {...capProps(rows, 'phenotype')}/></div>;
    }
    return (
        <div>
            {rows.map((row, idx) => (
                <RowFieldset
                    key={row.rowId}
                    title={`Phenotype ${idx + 1}`}
                    collapsed={!expansion.isExpanded(row)}
                    summary={summarizePhenotype(row)}
                    onEdit={() => expansion.expand(row.rowId)}
                    onDone={() => expansion.collapse(row.rowId)}
                    onRemove={() => onRemove(row.rowId)}
                >
                    <TextRowField id={`phn-desc-${row.rowId}`} label='Description' value={row.description} onChange={v => onChange(row.rowId, {description: v})} onCommit={onCommit}/>
                    <PhenotypeTimingRow
                        rowId={row.rowId}
                        hpfStart={row.hpfStart}
                        hpfEnd={row.hpfEnd}
                        unit={row.hpfUnit}
                        stage={row.stage}
                        onChange={patch => onChange(row.rowId, patch)}
                        onCommit={onCommit}
                    />
                    <BoolRowField
                        groupName={`phn-zfin-imgperm-${row.rowId}`}
                        label='ZFIN image permission'
                        value={row.zfinImagePermission}
                        onChange={v => onChange(row.rowId, {zfinImagePermission: v})}
                        onCommit={onCommit}
                    />
                    <BoolRowField
                        groupName={`phn-zirc-imgperm-${row.rowId}`}
                        label='ZIRC image permission'
                        value={row.zircImagePermission}
                        onChange={v => onChange(row.rowId, {zircImagePermission: v})}
                        onCommit={onCommit}
                    />
                    <TextRowField id={`phn-pct-${row.rowId}`}     label='Non-Mendelian %'      value={row.nonMendelianPercentage} onChange={v => onChange(row.rowId, {nonMendelianPercentage: v})} onCommit={onCommit} type='number' suffix='%'/>
                    <TextAreaRowField id={`phn-pctcomment-${row.rowId}`} label='Non-Mendelian comment' value={row.nonMendelianComment} onChange={v => onChange(row.rowId, {nonMendelianComment: v})} onCommit={onCommit}/>
                    <TextRowField id={`phn-seg-${row.rowId}`}     label='Segregation (CSV)'    value={row.segregationCsv}         onChange={v => onChange(row.rowId, {segregationCsv: v})}         onCommit={onCommit} placeholder='mendelian_recessive, …'/>
                    <TextAreaRowField id={`phn-type-${row.rowId}`} label='Type (CSV)'           value={row.typeCsv}                onChange={v => onChange(row.rowId, {typeCsv: v})}                onCommit={onCommit}/>
                </RowFieldset>
            ))}
            <AddButton label='phenotype' onAdd={onAdd} {...capProps(rows, 'phenotype')}/>
        </div>
    );
};

const PublicationsSection = ({rows, onAdd, onRemove, onChange, onCommit}: SectionListProps<PublicationRow>) => {
    if (rows.length === 0) {
        return <div><p className='text-muted'>No publications recorded for this mutation.</p><AddButton label='publication' onAdd={onAdd} {...capProps(rows, 'publication')}/></div>;
    }
    return (
        <div>
            {rows.map((row, idx) => {
                const inputId = `pub-${row.rowId}`;
                return (
                    <div className='form-group row align-items-center' key={row.rowId}>
                        <label htmlFor={inputId} className='col-sm-3 col-form-label'>
                            Publication {idx + 1}
                        </label>
                        <div className='col-sm-9 d-flex align-items-center' style={{gap: 8}}>
                            <input
                                type='text'
                                id={inputId}
                                className='form-control'
                                placeholder='PMID, DOI, or citation'
                                value={row.publication}
                                onChange={e => onChange(row.rowId, {publication: e.target.value})}
                                onBlur={onCommit}
                            />
                            <button
                                type='button'
                                className='btn btn-sm btn-outline-danger'
                                onClick={() => onRemove(row.rowId)}
                            >
                                Remove
                            </button>
                        </div>
                    </div>
                );
            })}
            <AddButton label='publication' onAdd={onAdd} {...capProps(rows, 'publication')}/>
        </div>
    );
};

// ─── Main container ────────────────────────────────────────────────────────

const MutationEdit = ({mutationId}: MutationEditProps) => {
    const [values, setValues] = useState<Record<string, string> | null>(null);
    const [committed, setCommitted] = useState<Record<string, string>>({});
    const [loadError, setLoadError] = useState<string>('');
    const [saveEvent, setSaveEvent] = useState<SaveEvent | null>(null);
    const seqRef = useRef<number>(0);
    const saveQueueRef = useRef<Promise<unknown>>(Promise.resolve());

    function emit(event: Omit<SaveEvent, 'seq'>) {
        seqRef.current += 1;
        setSaveEvent({...event, seq: seqRef.current});
    }

    function enqueueSave<T>(fn: () => Promise<T>): Promise<T> {
        const next = saveQueueRef.current.then(fn, fn);
        saveQueueRef.current = next.catch(() => undefined);
        return next;
    }

    const childDeps = {mutationId, enqueueSave, emit};
    const genes        = useChildCollection(GENE_ADAPTER,        childDeps);
    const lesions      = useChildCollection(LESION_ADAPTER,      childDeps);
    const assays       = useChildCollection(ASSAY_ADAPTER,       childDeps);
    const phenotypes   = useChildCollection(PHENOTYPE_ADAPTER,   childDeps);
    const publications = useChildCollection(PUBLICATION_ADAPTER, childDeps);

    useEffect(() => {
        if (!mutationId) {
            return;
        }
        let cancelled = false;
        fetch(`/action/zirc/mutation/${mutationId}.json`)
            .then(r => {
                if (!r.ok) {
                    throw new Error(`HTTP ${r.status}`);
                }
                return r.json() as Promise<MutationDTO>;
            })
            .then(data => {
                if (cancelled) {
                    return;
                }
                applyDTO(data);
            })
            .catch(e => {
                if (!cancelled) {
                    setLoadError(e instanceof Error ? e.message : 'Load failed');
                }
            });
        return () => { cancelled = true; };
    }, [mutationId]);

    function applyDTO(data: MutationDTO) {
        const initial: Record<string, string> = {};
        ALL_MUTATION_FIELDS.forEach(d => {
            initial[d.field] = valueToInputString(data[d.field]);
        });
        setValues(initial);
        setCommitted(initial);
        genes.apply(data);
        lesions.apply(data);
        assays.apply(data);
        phenotypes.apply(data);
        publications.apply(data);
    }

    function setFieldValue(field: ScalarField, next: string) {
        setValues(prev => prev ? {...prev, [field]: next} : prev);
    }

    async function commitScalarField(field: FieldDef<ScalarField>, next: string) {
        if (next === committed[field.field]) {
            return;
        }
        emit({status: 'saving', label: field.label});
        // Optimistically mark committed so a same-value blur doesn't refire.
        setCommitted(prev => ({...prev, [field.field]: next}));
        try {
            await enqueueSave(async () => {
                const body = new URLSearchParams();
                body.append('mutationId', mutationId);
                body.append('field', field.field);
                body.append('value', next);
                const resp = await fetch('/action/zirc/mutation/save-field', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: body.toString(),
                });
                if (!resp.ok) {
                    throw new Error(`HTTP ${resp.status}`);
                }
                const data = await resp.json() as MutationDTO;
                const normalized = valueToInputString(data[field.field]);
                setValues(prev => prev ? {...prev, [field.field]: normalized} : prev);
                setCommitted(prev => ({...prev, [field.field]: normalized}));
            });
            emit({status: 'saved', label: field.label});
        } catch (e) {
            emit({status: 'error', label: field.label, message: e instanceof Error ? e.message : 'Save failed'});
        }
    }

    if (loadError) {
        return <div className='alert alert-danger'>Failed to load mutation: {loadError}</div>;
    }
    if (!values) {
        return <div className='text-muted'>Loading…</div>;
    }

    const renderRow = (def: FieldDef<ScalarField>) => (
        <FieldRow
            key={def.rowKey ?? def.field}
            def={def}
            value={values[def.field]}
            onChange={next => setFieldValue(def.field, next)}
            onCommit={next => commitScalarField(def, next)}
        />
    );

    // Filter visible rows up-front so React keys stay unique among rendered
    // children (two FieldDefs may share a field — see allele designation).
    const visibleFields = (defs: FieldDef<ScalarField>[]) =>
        defs.filter(d => !d.visible || d.visible(values));

    return <>
        <Section id='general' title='General'>
            <FieldsTable>{visibleFields(GENERAL_FIELDS).map(renderRow)}</FieldsTable>
        </Section>
        <Section id='genes' title='Genes'>
            <GenesSection rows={genes.rows} onAdd={genes.add} onRemove={genes.remove} onChange={genes.change} onCommit={genes.commit}/>
        </Section>
        <Section id='lesions' title='Lesions'>
            <LesionsSection
                rows={lesions.rows}
                onAddWithType={type => lesions.addWithPatch({lesionType: type})}
                onRemove={lesions.remove}
                onChange={lesions.change}
                onCommit={lesions.commit}
            />
        </Section>
        <Section id='genotyping-assays' title='Genotyping Assays'>
            <GenotypingAssaysSection
                rows={assays.rows}
                onAddWithType={type => assays.addWithPatch({assayType: type})}
                onRemove={assays.remove}
                onChange={assays.change}
                onCommit={assays.commit}
            />
        </Section>
        <Section id='phenotypes' title='Phenotypes'>
            <PhenotypesSection rows={phenotypes.rows} onAdd={phenotypes.add} onRemove={phenotypes.remove} onChange={phenotypes.change} onCommit={phenotypes.commit}/>
        </Section>
        <Section id='publications' title='Publications'>
            <PublicationsSection rows={publications.rows} onAdd={publications.add} onRemove={publications.remove} onChange={publications.change} onCommit={publications.commit}/>
        </Section>
        <Section id='lethality' title='Lethality'>
            <FieldsTable>{visibleFields(LETHALITY_FIELDS).map(renderRow)}</FieldsTable>
        </Section>
        <SaveToast event={saveEvent}/>
    </>;
};

export default MutationEdit;
