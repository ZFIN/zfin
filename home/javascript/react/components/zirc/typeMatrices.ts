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
        helpText: 'At least 20 nt directly preceding the lesion.'},
    threePrimeFlank:   {key: 'threePrimeFlank',   label: '3′ flank',                    type: 'textarea',
        helpText: 'At least 20 nt directly following the lesion.'},
    hasLargeVariant:   {key: 'hasLargeVariant',   label: 'Large variant?',              type: 'bool'},
    mutatedAminoAcids: {key: 'mutatedAminoAcids', label: 'Mutated amino acids',         type: 'text'},
    additionalInfo:    {key: 'additionalInfo',    label: 'Additional info',             type: 'textarea'},
};

// Base field order per type. The Large-variant toggle (hasLargeVariant) is
// listed for Deletion / Insertion / Indel; visibleLesionFields() splices in
// the structured 5'/3' flank rows when it's checked.
const BASE_LESION_FIELDS_BY_TYPE: Record<LesionTypeKey, LesionFieldKey[]> = {
    point_mutation: ['lesionSizeBp', 'nucleotideChange', 'locationInline', 'mutatedAminoAcids', 'additionalInfo'],
    deletion:       ['lesionSizeBp', 'deletedSequence', 'locationInline', 'hasLargeVariant', 'mutatedAminoAcids', 'additionalInfo'],
    insertion:      ['lesionSizeBp', 'insertedSequence', 'locationInline', 'hasLargeVariant', 'mutatedAminoAcids', 'additionalInfo'],
    indel:          ['lesionSizeBp', 'nucleotideChange', 'locationInline', 'hasLargeVariant', 'mutatedAminoAcids', 'additionalInfo'],
    transgene:      ['lesionSizeBp', 'transgeneSequence', 'fivePrimeFlank', 'threePrimeFlank', 'mutatedAminoAcids', 'additionalInfo'],
    other:          ['mutatedAminoAcids', 'additionalInfo'],
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
