// Single source of truth mapping each lesion / genotyping-assay type to the
// fields the curator should see. The shape mirrors the xlsx spreadsheets
// shipped by the ZIRC team (see more-product-specs/Field summary…).
//
// The matrices are intentionally data, not code branches — adding a new type
// or moving a field between types is a single-line edit here, no JSX changes.

export type LesionTypeKey =
    | 'point_mutation'
    | 'deletion'
    | 'insertion'
    | 'indel'
    | 'transgene'
    | 'other'
    | 'unknown';

export interface LesionTypeOption {
    value: LesionTypeKey;
    label: string;
}

// Type list per the PDF "don't include translocation, inversion, duplication"
// guidance plus the xlsx's Other / Unknown escape hatches.
export const LESION_TYPE_OPTIONS: LesionTypeOption[] = [
    {value: 'point_mutation', label: 'Point mutation'},
    {value: 'deletion',       label: 'Deletion'},
    {value: 'insertion',      label: 'Insertion'},
    {value: 'indel',          label: 'Indel (delins)'},
    {value: 'transgene',      label: 'Transgene'},
    {value: 'other',          label: 'Other'},
    {value: 'unknown',        label: 'Unknown'},
];

export type LesionFieldKey =
    | 'lesionSizeBp'
    | 'nucleotideChange'
    | 'deletedSequence'
    | 'insertedSequence'
    | 'transgeneSequence'
    | 'locationInline'
    | 'fivePrimeFlank'
    | 'threePrimeFlank'
    | 'hasLargeVariant'
    | 'mutatedAminoAcids'
    | 'mutatedAminoAcidsHgvs'
    | 'additionalInfo';

// Display + input metadata per field. `suffix` is for the bp-style right-side
// unit (B5.6 cosmetic). `placeholder` mirrors the PDF's "please list at least
// 5 / 20 nucleotides" guidance for location fields.
export interface LesionFieldDef {
    key: LesionFieldKey;
    label: string;
    type: 'text' | 'textarea' | 'int' | 'bool';
    placeholder?: string;
    suffix?: string;
    /** Optional helper line under the input. */
    helpText?: string;
    /** Optional "i" link target. When set, an info link rendered next to
     *  the field opens this URL. */
    infoHref?: string;
}

export const LESION_FIELD_DEFS: Record<LesionFieldKey, LesionFieldDef> = {
    lesionSizeBp:      {key: 'lesionSizeBp',      label: 'Lesion size',                 type: 'int',     suffix: 'bp'},
    nucleotideChange:  {key: 'nucleotideChange',  label: 'Nucleotide change',           type: 'text',
        placeholder: 'WT → mutant, e.g. A → T'},
    deletedSequence:   {key: 'deletedSequence',   label: 'Deleted sequence',            type: 'textarea'},
    insertedSequence:  {key: 'insertedSequence',  label: 'Inserted sequence',           type: 'textarea'},
    transgeneSequence: {key: 'transgeneSequence', label: 'Transgene sequence',          type: 'textarea'},
    locationInline:    {key: 'locationInline',    label: 'Location (inline)',           type: 'textarea',
        helpText: 'Annotated inline; list at least 5 nt before and after.'},
    fivePrimeFlank:    {key: 'fivePrimeFlank',    label: '5′ flank',                    type: 'textarea',
        helpText: 'At least 20 nt directly preceding the lesion / transgene.',
        infoHref: 'https://wiki.zfin.org/display/general/Transgene+Insertion+Sequence+Conventions'},
    threePrimeFlank:   {key: 'threePrimeFlank',   label: '3′ flank',                    type: 'textarea',
        helpText: 'At least 20 nt directly following the lesion / transgene.',
        infoHref: 'https://wiki.zfin.org/display/general/Transgene+Insertion+Sequence+Conventions'},
    hasLargeVariant:   {key: 'hasLargeVariant',   label: 'Large variant?',              type: 'bool'},
    mutatedAminoAcids:     {key: 'mutatedAminoAcids',     label: 'Mutated amino acids',  type: 'text',
        helpText: 'Free-text description (frameshift, etc.).'},
    mutatedAminoAcidsHgvs: {key: 'mutatedAminoAcidsHgvs', label: 'Mutated amino acids (HGVS.P)', type: 'text',
        helpText: 'Formal HGVS.P nomenclature, e.g. p.Arg54Ter.',
        infoHref: 'https://hgvs-nomenclature.org/stable/recommendations/protein/'},
    additionalInfo:        {key: 'additionalInfo',        label: 'Additional info',      type: 'textarea'},
};

// Base field order per type. The Large-variant toggle (hasLargeVariant) is
// listed for Deletion / Insertion / Indel; visibleLesionFields() splices in
// the structured 5'/3' flank rows when it's checked.
const BASE_LESION_FIELDS_BY_TYPE: Record<LesionTypeKey, LesionFieldKey[]> = {
    point_mutation: ['lesionSizeBp', 'nucleotideChange', 'locationInline', 'mutatedAminoAcids', 'mutatedAminoAcidsHgvs', 'additionalInfo'],
    deletion:       ['lesionSizeBp', 'deletedSequence', 'locationInline', 'hasLargeVariant', 'mutatedAminoAcids', 'mutatedAminoAcidsHgvs', 'additionalInfo'],
    insertion:      ['lesionSizeBp', 'insertedSequence', 'locationInline', 'hasLargeVariant', 'mutatedAminoAcids', 'mutatedAminoAcidsHgvs', 'additionalInfo'],
    indel:          ['lesionSizeBp', 'nucleotideChange', 'locationInline', 'hasLargeVariant', 'mutatedAminoAcids', 'mutatedAminoAcidsHgvs', 'additionalInfo'],
    transgene:      ['lesionSizeBp', 'transgeneSequence', 'fivePrimeFlank', 'threePrimeFlank', 'mutatedAminoAcids', 'mutatedAminoAcidsHgvs', 'additionalInfo'],
    other:          ['mutatedAminoAcids', 'mutatedAminoAcidsHgvs', 'additionalInfo'],
    unknown:        ['additionalInfo'],
};

/** Returns the list of visible field keys for the given lesion type + state. */
export function visibleLesionFields(type: string, hasLargeVariant: boolean): LesionFieldKey[] {
    const key = type as LesionTypeKey;
    const base = BASE_LESION_FIELDS_BY_TYPE[key];
    if (!base) {
        return [];
    }
    if (hasLargeVariant && (key === 'deletion' || key === 'insertion' || key === 'indel')) {
        const idx = base.indexOf('hasLargeVariant');
        return [
            ...base.slice(0, idx + 1),
            'fivePrimeFlank',
            'threePrimeFlank',
            ...base.slice(idx + 1),
        ];
    }
    return base;
}

// ─── Genotyping assays ────────────────────────────────────────────────────

export type AssayTypeKey =
    | 'pcr_gel'
    | 'pcr_sequencing'
    | 'rflp'
    | 'dcaps'
    | 'asa'
    | 'kasp'
    | 'hrma'
    | 'sslp';

export interface AssayTypeOption {
    value: AssayTypeKey;
    label: string;
}

export const ASSAY_TYPE_OPTIONS: AssayTypeOption[] = [
    {value: 'pcr_gel',        label: 'PCR + gel electrophoresis'},
    {value: 'pcr_sequencing', label: 'PCR + sequencing'},
    {value: 'rflp',           label: 'RFLP'},
    {value: 'dcaps',          label: 'dCAPS'},
    {value: 'asa',            label: 'ASA'},
    {value: 'kasp',           label: 'KASP'},
    {value: 'hrma',           label: 'HRMA'},
    {value: 'sslp',           label: 'SSLP'},
];

export type AssayFieldKey =
    | 'forwardPrimer'
    | 'reversePrimer'
    | 'expectedWtPcr'
    | 'expectedMutPcr'
    | 'sequencingPrimer'
    | 'chromatogramFiles'
    | 'dcapsMismatchPrimer'
    | 'restrictionEnzymeName'
    | 'restrictionEnzymeCatalog'
    | 'enzymeCleavesWt'
    | 'enzymeCleavesMut'
    | 'expectedWtDigest'
    | 'expectedMutDigest'
    | 'wtSpecificPrimer'
    | 'mutSpecificPrimer'
    | 'commonPrimer'
    | 'kaspGenomicSequence'
    | 'gelImageFiles'
    | 'resultImageFiles'
    | 'meltCurveFiles'
    | 'sslpMarkerName'
    | 'sslpDistance'
    | 'sslpGenomicLocation'
    | 'sslpInducedBackground'
    | 'sslpOutcrossedBackground'
    | 'sslpInducedPcr'
    | 'sslpOutcrossedPcr'
    | 'additionalInfo';

export interface AssayFieldDef {
    key: AssayFieldKey;
    label: string;
    /** 'checkbox' is a binary always-on/always-off (for the WT / MUT
     *  cleaves toggles which can be independently set).
     *  'autocomplete' fetches matching suggestions from autocompleteUrl.
     *  'files' renders a file upload + list widget bound to fileKind. */
    type: 'text' | 'textarea' | 'checkbox' | 'autocomplete' | 'files';
    placeholder?: string;
    suffix?: string;
    /** Optional HTML5 pattern attribute for client-side validation hint. */
    pattern?: string;
    /** Hint rendered as muted help text under the input. */
    helpText?: string;
    /** Required for 'autocomplete' fields. The component fetches matches
     *  from `${autocompleteUrl}?term=…` and expects a JSON array of
     *  `{label, value}`. */
    autocompleteUrl?: string;
    /** Required for 'files' fields. The DB-level af_kind for files
     *  rendered by this widget. */
    fileKind?: string;
}

export const ASSAY_FIELD_DEFS: Record<AssayFieldKey, AssayFieldDef> = {
    forwardPrimer:              {key: 'forwardPrimer',              label: 'Forward primer',                  type: 'text',     pattern: '^[ACTGNactgn]+$', helpText: 'ACTGN only.'},
    reversePrimer:              {key: 'reversePrimer',              label: 'Reverse primer',                  type: 'text',     pattern: '^[ACTGNactgn]+$', helpText: 'ACTGN only.'},
    expectedWtPcr:              {key: 'expectedWtPcr',              label: 'Expected wild-type PCR product',  type: 'text',     suffix: 'bp'},
    expectedMutPcr:             {key: 'expectedMutPcr',             label: 'Expected mutant PCR product',     type: 'text',     suffix: 'bp'},
    sequencingPrimer:           {key: 'sequencingPrimer',           label: 'Sequencing primer',               type: 'text',     pattern: '^[ACTGNactgn]+$', helpText: 'ACTGN only.'},
    chromatogramFiles:          {key: 'chromatogramFiles',          label: 'Chromatograms',                   type: 'files', fileKind: 'chromatogram'},
    dcapsMismatchPrimer:        {key: 'dcapsMismatchPrimer',        label: 'Primer with introduced mismatch', type: 'text',     pattern: '^[ACTGNactgn]+$', helpText: 'ACTGN only.'},
    restrictionEnzymeName:      {key: 'restrictionEnzymeName',      label: 'Restriction enzyme name',         type: 'text'},
    restrictionEnzymeCatalog:   {key: 'restrictionEnzymeCatalog',   label: 'Restriction enzyme catalog #',    type: 'text'},
    enzymeCleavesWt:            {key: 'enzymeCleavesWt',            label: 'Enzyme cleaves WT template',      type: 'checkbox'},
    enzymeCleavesMut:           {key: 'enzymeCleavesMut',           label: 'Enzyme cleaves MUT template',     type: 'checkbox'},
    expectedWtDigest:           {key: 'expectedWtDigest',           label: 'Expected WT product after digest', type: 'text', suffix: 'bp'},
    expectedMutDigest:          {key: 'expectedMutDigest',          label: 'Expected MUT product after digest', type: 'text', suffix: 'bp'},
    wtSpecificPrimer:           {key: 'wtSpecificPrimer',           label: 'WT-specific primer',              type: 'text',     pattern: '^[ACTGNactgn]+$', helpText: 'ACTGN only.'},
    mutSpecificPrimer:          {key: 'mutSpecificPrimer',          label: 'Mutant-specific primer',          type: 'text',     pattern: '^[ACTGNactgn]+$', helpText: 'ACTGN only.'},
    commonPrimer:               {key: 'commonPrimer',               label: 'Common primer',                   type: 'text',     pattern: '^[ACTGNactgn]+$', helpText: 'ACTGN only.'},
    kaspGenomicSequence:        {key: 'kaspGenomicSequence',        label: 'Genomic DNA sequence (KASP design)', type: 'textarea'},
    gelImageFiles:              {key: 'gelImageFiles',              label: 'Annotated gel images',            type: 'files', fileKind: 'gel_image'},
    resultImageFiles:           {key: 'resultImageFiles',           label: 'Annotated result images',         type: 'files', fileKind: 'result_image'},
    meltCurveFiles:             {key: 'meltCurveFiles',             label: 'Annotated melt curve files',      type: 'files', fileKind: 'melt_curve'},
    sslpMarkerName:             {key: 'sslpMarkerName',             label: 'SSLP marker name',                type: 'autocomplete',
        autocompleteUrl: '/action/zirc/markers/search?typeGroup=SSLP',
        placeholder: 'Search ZFIN SSLP markers…'},
    sslpDistance:               {key: 'sslpDistance',               label: 'Distance marker → mutation',      type: 'text'},
    sslpGenomicLocation:        {key: 'sslpGenomicLocation',        label: 'Genomic location of marker',      type: 'text'},
    sslpInducedBackground:      {key: 'sslpInducedBackground',      label: 'Background mutation was induced on', type: 'text'},
    sslpOutcrossedBackground:   {key: 'sslpOutcrossedBackground',   label: 'Recommended outcrossing background', type: 'text'},
    sslpInducedPcr:             {key: 'sslpInducedPcr',             label: 'PCR product on induced background', type: 'text',  suffix: 'bp'},
    sslpOutcrossedPcr:          {key: 'sslpOutcrossedPcr',          label: 'PCR product on outcrossing background', type: 'text', suffix: 'bp'},
    additionalInfo:             {key: 'additionalInfo',             label: 'Additional info',                 type: 'textarea'},
};

const ASSAY_FIELDS_BY_TYPE: Record<AssayTypeKey, AssayFieldKey[]> = {
    pcr_gel: [
        'forwardPrimer', 'reversePrimer', 'expectedWtPcr', 'expectedMutPcr',
        'gelImageFiles', 'additionalInfo',
    ],
    pcr_sequencing: [
        'forwardPrimer', 'reversePrimer', 'expectedWtPcr', 'expectedMutPcr',
        'sequencingPrimer', 'chromatogramFiles', 'additionalInfo',
    ],
    rflp: [
        'forwardPrimer', 'reversePrimer', 'expectedWtPcr', 'expectedMutPcr',
        'restrictionEnzymeName', 'restrictionEnzymeCatalog', 'enzymeCleavesWt', 'enzymeCleavesMut', 'expectedWtDigest', 'expectedMutDigest',
        'gelImageFiles', 'additionalInfo',
    ],
    dcaps: [
        'forwardPrimer', 'reversePrimer', 'expectedWtPcr', 'expectedMutPcr',
        'dcapsMismatchPrimer', 'restrictionEnzymeName', 'restrictionEnzymeCatalog', 'enzymeCleavesWt', 'enzymeCleavesMut',
        'expectedWtDigest', 'expectedMutDigest',
        'gelImageFiles', 'additionalInfo',
    ],
    asa: [
        'expectedWtPcr', 'expectedMutPcr',
        'wtSpecificPrimer', 'mutSpecificPrimer', 'commonPrimer',
        'resultImageFiles', 'additionalInfo',
    ],
    kasp: [
        'expectedWtPcr', 'expectedMutPcr',
        'wtSpecificPrimer', 'mutSpecificPrimer', 'commonPrimer',
        'kaspGenomicSequence',
        'resultImageFiles', 'additionalInfo',
    ],
    hrma: [
        'forwardPrimer', 'reversePrimer', 'expectedWtPcr', 'expectedMutPcr',
        'meltCurveFiles', 'additionalInfo',
    ],
    sslp: [
        'forwardPrimer', 'reversePrimer',
        'sslpMarkerName', 'sslpDistance', 'sslpGenomicLocation',
        'sslpInducedBackground', 'sslpOutcrossedBackground',
        'sslpInducedPcr', 'sslpOutcrossedPcr',
        'gelImageFiles', 'additionalInfo',
    ],
};

export function visibleAssayFields(type: string): AssayFieldKey[] {
    return ASSAY_FIELDS_BY_TYPE[type as AssayTypeKey] ?? [];
}
