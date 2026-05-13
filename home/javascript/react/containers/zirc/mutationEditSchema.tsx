// Schema for the ZIRC mutation editor. Consumed by MutationEdit.tsx via
// <FormRenderer schema={MUTATION_EDIT_SCHEMA} …/>.
//
// The schema mirrors MutationDTO's wire shape. The largest section by
// far is genotyping assays -- 8 assay types, each with their own field
// subset (driven by typeMatrices.ts) and their own file-upload custom
// nodes. Lesions are similar but smaller. Phenotypes embed a custom
// hpf/dpf timing widget (kept as a CustomNode escape hatch -- the
// derived-stage UX is genuinely unusual). Publications use a CustomNode
// too because the wire is a flat string[] rather than an array of
// objects, which doesn't fit ArrayNode cleanly yet.

import React from 'react';
import {array, custom, field, section} from '../../components/form-renderer/builders';
import type {FormNode} from '../../components/form-renderer/types';
import PhenotypeTimingRow from '../../components/zirc/PhenotypeTimingRow';
import ZircAssayFileUpload, {type UploadedAssayFile} from '../../components/zirc/ZircAssayFileUpload';
import {
    ASSAY_FIELD_DEFS,
    ASSAY_TYPE_OPTIONS,
    LESION_FIELD_DEFS,
    LESION_TYPE_OPTIONS,
    type AssayFieldKey,
    type LesionFieldKey,
    visibleAssayFields,
    visibleLesionFields,
} from '../../components/zirc/typeMatrices';
import type {
    GeneDTO,
    GenotypingAssayDTO,
    LesionDTO,
    MutationDTO,
    PhenotypeDTO,
} from '../MutationEdit';

// ─── Option lists ─────────────────────────────────────────────────────────

const MUTAGENESIS_STAGE_OPTIONS = [
    {value: 'adult_females', label: 'Adult females'},
    {value: 'adult_males',   label: 'Adult males'},
    {value: 'embryos',       label: 'Embryos'},
    {value: 'sperm',         label: 'Sperm'},
];

const MUTAGENESIS_PROTOCOL_OPTIONS = [
    {value: 'crispr',               label: 'CRISPR'},
    {value: 'ems',                  label: 'EMS'},
    {value: 'enu',                  label: 'ENU'},
    {value: 'g_rays',               label: 'g-rays'},
    {value: 'spontaneous',          label: 'Spontaneous'},
    {value: 'talen',                label: 'TALEN'},
    {value: 'tmp',                  label: 'TMP'},
    {value: 'zinc_finger_nuclease', label: 'Zinc finger nuclease'},
    {value: 'other',                label: 'Other'},
];

// ─── Helpers ──────────────────────────────────────────────────────────────

function lesionFieldVisible(fieldKey: LesionFieldKey) {
    return (_dto: unknown, row: unknown): boolean => {
        const r = row as Partial<LesionDTO> | undefined;
        const type = r?.lesionType ?? '';
        return visibleLesionFields(type, r?.hasLargeVariant === true).includes(fieldKey);
    };
}

function assayFieldVisible(fieldKey: AssayFieldKey) {
    return (_dto: unknown, row: unknown): boolean => {
        const r = row as Partial<GenotypingAssayDTO> | undefined;
        return visibleAssayFields(r?.assayType ?? '').includes(fieldKey);
    };
}

/** Build a FieldNode for one lesion field by looking up its def in typeMatrices.
 *  Optional `opts` lets the schema scope the field to a subset of lesion types
 *  and override the label — needed for Indel rows where `lesionSizeBp` reads
 *  as "Deletion size" rather than the generic "Lesion size". */
function lesionField(
    key: LesionFieldKey,
    opts?: {labelOverride?: string; onlyForTypes?: string[]; exceptForTypes?: string[]},
): FormNode {
    const def = LESION_FIELD_DEFS[key];
    const type =
        def.type === 'int'   ? 'int'
            : def.type === 'bool'  ? 'bool'
                : def.type === 'textarea' ? 'textarea'
                    : 'text';
    const baseVisible = lesionFieldVisible(key);
    const visible =
        opts?.onlyForTypes || opts?.exceptForTypes
            ? (dto: unknown, row: unknown) => {
                const r = row as Partial<LesionDTO> | undefined;
                const t = r?.lesionType ?? '';
                if (opts.onlyForTypes && !opts.onlyForTypes.includes(t)) {
                    return false;
                }
                if (opts.exceptForTypes && opts.exceptForTypes.includes(t)) {
                    return false;
                }
                return baseVisible(dto, row);
            }
            : baseVisible;
    return field({
        path: key,
        label: opts?.labelOverride ?? def.label,
        type,
        placeholder: def.placeholder,
        helpText: def.helpText,
        infoHref: def.infoHref,
        suffix: def.suffix,
        visible,
    });
}

/** Build the field/custom node for one assay field, including file-upload custom nodes. */
function assayField(key: AssayFieldKey): FormNode {
    const def = ASSAY_FIELD_DEFS[key];
    if (def.type === 'files') {
        const fileKind = def.fileKind ?? key;
        return custom({
            id: `assay-${key}`,
            visible: assayFieldVisible(key),
            render: ({row, actions}) => {
                const r = row as Partial<GenotypingAssayDTO> | undefined;
                const assayId = r?.id ?? null;
                if (assayId == null) {
                    return (
                        <p className='text-muted small mb-0'>
                            Save the assay before uploading files.
                        </p>
                    );
                }
                const files = ((r?.files ?? []) as UploadedAssayFile[])
                    .filter(f => f.kind === fileKind);
                const refresh = actions.applyMutationDTO as
                    | ((dto: unknown) => void)
                    | undefined;
                return (
                    <div className='mb-3'>
                        <h6>{def.label}</h6>
                        <ZircAssayFileUpload
                            assayId={assayId}
                            fileKind={fileKind}
                            files={files}
                            onMutationRefresh={dto => refresh?.(dto)}
                        />
                    </div>
                );
            },
        });
    }
    if (def.type === 'checkbox') {
        return field({
            path: key,
            label: def.label,
            type: 'checkbox',
            helpText: def.helpText,
            visible: assayFieldVisible(key),
        });
    }
    const type =
        def.type === 'autocomplete' ? 'autocomplete'
            : def.type === 'textarea' ? 'textarea'
                : 'text';
    return field({
        path: key,
        label: def.label,
        type,
        placeholder: def.placeholder,
        helpText: def.helpText,
        suffix: def.suffix,
        autocompleteUrl: def.autocompleteUrl,
        visible: assayFieldVisible(key),
    });
}

function summarizeAssay(row: unknown): string {
    const r = row as Partial<GenotypingAssayDTO>;
    const typeLabel = ASSAY_TYPE_OPTIONS.find(o => o.value === r.assayType)?.label
        ?? r.assayType
        ?? '(unset)';
    const primer = r.forwardPrimer ?? r.sequencingPrimer ?? r.wtSpecificPrimer ?? '';
    return primer ? `${typeLabel} · ${primer.slice(0, 20)}…` : typeLabel;
}

function summarizeLesion(row: unknown): string {
    const r = row as Partial<LesionDTO>;
    const typeLabel = LESION_TYPE_OPTIONS.find(o => o.value === r.lesionType)?.label
        ?? r.lesionType
        ?? '(unset)';
    const size = r.lesionSizeBp ? `${r.lesionSizeBp} bp` : '';
    return [typeLabel, size].filter(Boolean).join(' · ');
}

function summarizeGene(row: unknown): string {
    const r = row as Partial<GeneDTO>;
    return r.mutatedGeneAbbreviation || r.mutatedGeneZdbId || '(unset)';
}

function summarizePhenotype(row: unknown): string {
    const r = row as Partial<PhenotypeDTO>;
    const desc = (r.description ?? '').slice(0, 60);
    return desc ? `${desc}${(r.description?.length ?? 0) > 60 ? '…' : ''}` : '(no description)';
}

// ─── Empty DTO factory ────────────────────────────────────────────────────

export function emptyMutationDTO(lineSubmissionId: string): MutationDTO {
    return {
        id: 0,
        lineSubmissionId,
        sortOrder: null,
        alleleDesignation: null,
        alleleInZfin: null,
        mutagenesisStage: null,
        mutagenesisProtocol: null,
        mutagenesisProtocolOther: null,
        molecularlyCharacterized: null,
        mutationType: null,
        homozygousLethal: null,
        lethalityStageTypical: null,
        lethalitySpecificTimepoint: null,
        lethalityWindowStart: null,
        lethalityWindowEnd: null,
        lethalityAdditionalInfo: null,
        zfinRecordEstablished: null,
        cellGenomicFeature: null,
        mutationDiscoverer: null,
        mutationInstitution: null,
        genes: [],
        lesions: [],
        genotypingAssays: [],
        phenotypes: [],
        publications: [],
    };
}

// ─── Schema ───────────────────────────────────────────────────────────────

export const MUTATION_EDIT_SCHEMA: FormNode[] = [

    section('general', 'General', [
        field({
            path: 'alleleInZfin',
            label: 'Allele in ZFIN',
            type: 'bool',
            helpText: 'When "Yes", the field below searches existing ZFIN markers.',
        }),
        field({
            path: 'alleleDesignation',
            label: 'Allele Designation',
            type: 'autocomplete',
            autocompleteUrl: '/action/zirc/features/search',
            placeholder: 'Search ZFIN features…',
            infoHref: 'https://wiki.zfin.org/display/general/ZFIN+Nomenclature+Conventions',
            visible: dto => (dto as MutationDTO).alleleInZfin === true,
        }),
        field({
            path: 'alleleDesignation',
            label: 'Allele Designation',
            type: 'text',
            infoHref: 'https://wiki.zfin.org/display/general/ZFIN+Nomenclature+Conventions',
            visible: dto => (dto as MutationDTO).alleleInZfin !== true,
        }),
        field({
            path: 'mutagenesisStage',
            label: 'Mutagenesis Stage',
            type: 'select',
            options: MUTAGENESIS_STAGE_OPTIONS,
        }),
        field({
            path: 'mutagenesisProtocol',
            label: 'Mutagenesis Protocol',
            type: 'select-with-other',
            otherPath: 'mutagenesisProtocolOther',
            otherValue: 'other',
            options: MUTAGENESIS_PROTOCOL_OPTIONS,
        }),
        field({path: 'molecularlyCharacterized', label: 'Molecularly Characterized', type: 'bool'}),
        field({path: 'mutationType',             label: 'Mutation Type',             type: 'text'}),
        field({path: 'zfinRecordEstablished',    label: 'ZFIN Record Established',   type: 'bool'}),
        field({
            path: 'cellGenomicFeature',
            label: 'ZDB Genomic Feature #',
            type: 'text',
            visible: dto => (dto as MutationDTO).zfinRecordEstablished === true,
        }),
        field({path: 'mutationDiscoverer',  label: 'Discoverer',  type: 'text'}),
        field({path: 'mutationInstitution', label: 'Institution', type: 'text'}),
    ]),

    section('genes', 'Genes', [
        array({
            id: 'genes',
            path: 'genes',
            title: 'Genes',
            emptyMessage: 'No genes recorded for this mutation.',
            itemLabel: idx => `Gene ${idx + 1}`,
            newRow: () => ({
                id: null,
                sortOrder: null,
                mutatedGeneZdbId: null,
                mutatedGeneAbbreviation: null,
                linkageGroup: null,
                genbankGenomicDna: null,
                genbankCdna: null,
            }),
            collapseWhen: row => {
                const r = row as Partial<GeneDTO>;
                return !!r.mutatedGeneZdbId;
            },
            summarize: summarizeGene,
            maxItems: 10,
            childTemplate: [
                field({
                    path: 'mutatedGeneAbbreviation',
                    label: 'Mutated gene',
                    type: 'autocomplete',
                    autocompleteUrl: '/action/zirc/markers/search?typeGroup=GENEDOM',
                    placeholder: 'Search ZFIN genes…',
                }),
                // Server-resolved from the autocomplete pick; not user-editable.
                field({
                    path: 'mutatedGeneZdbId',
                    label: 'ZDB ID',
                    type: 'readonly',
                    placeholder: '(set when you pick a gene above)',
                }),
                field({path: 'linkageGroup',      label: 'Linkage group', type: 'autocomplete',
                    autocompleteUrl: '/action/zirc/chromosomes/search'}),
                field({path: 'genbankGenomicDna', label: 'GenBank genomic DNA', type: 'text'}),
                field({path: 'genbankCdna',       label: 'GenBank cDNA',        type: 'text'}),
            ],
        }),
    ]),

    section('lesions', 'Lesions', [
        array({
            id: 'lesions',
            path: 'lesions',
            title: 'Lesions',
            emptyMessage: 'No lesions recorded.',
            itemLabel: idx => `Lesion ${idx + 1}`,
            maxItems: 10,
            newRow: () => ({
                id: null,
                sortOrder: null,
                lesionType: null,
                lesionSizeBp: null,
                insertionSizeBp: null,
                nucleotideChange: null,
                deletedSequence: null,
                insertedSequence: null,
                transgeneSequence: null,
                locationInline: null,
                fivePrimeFlank: null,
                threePrimeFlank: null,
                hasLargeVariant: null,
                mutatedAminoAcids: null,
                mutatedAminoAcidsHgvs: null,
                additionalInfo: null,
            }),
            addRequiresTypePick: {
                title: 'Pick a lesion type',
                description: 'The type determines which fields appear.',
                options: LESION_TYPE_OPTIONS,
                targetPath: 'lesionType',
            },
            collapseWhen: row => !!(row as Partial<LesionDTO>).lesionType,
            summarize: summarizeLesion,
            childTemplate: [
                // Lesion type is set by the type-picker modal but also editable
                // afterwards via this dropdown.
                field({
                    path: 'lesionType',
                    label: 'Lesion type',
                    type: 'select',
                    options: LESION_TYPE_OPTIONS,
                }),
                // Every lesion field listed in typeMatrices, gated by
                // visibleLesionFields(lesionType, hasLargeVariant). Indel
                // gets two split-out size fields ("Deletion size" reuses
                // lesionSizeBp with a relabel; "Insertion size" is its own
                // column), so the default lesionSizeBp render is suppressed
                // for indel.
                lesionField('lesionSizeBp', {exceptForTypes: ['indel']}),
                lesionField('lesionSizeBp', {onlyForTypes: ['indel'], labelOverride: 'Deletion size'}),
                lesionField('insertionSizeBp'),
                lesionField('nucleotideChange'),
                lesionField('deletedSequence'),
                lesionField('insertedSequence'),
                lesionField('transgeneSequence'),
                lesionField('locationInline'),
                lesionField('hasLargeVariant'),
                lesionField('fivePrimeFlank'),
                lesionField('threePrimeFlank'),
                lesionField('mutatedAminoAcids'),
                lesionField('mutatedAminoAcidsHgvs'),
                lesionField('additionalInfo'),
            ],
        }),
    ]),

    section('genotyping-assays', 'Genotyping Assays', [
        array({
            id: 'genotypingAssays',
            path: 'genotypingAssays',
            title: 'Genotyping Assays',
            emptyMessage: 'No genotyping assays recorded.',
            itemLabel: idx => `Assay ${idx + 1}`,
            maxItems: 10,
            newRow: () => ({
                id: null,
                sortOrder: null,
                assayType: null,
                forwardPrimer: null,
                reversePrimer: null,
                expectedWtPcr: null,
                expectedMutPcr: null,
                restrictionEnzymeName: null,
                restrictionEnzymeCatalog: null,
                enzymeCleaves: [],
                expectedWtDigest: null,
                expectedMutDigest: null,
                additionalInfo: null,
                sequencingPrimer: null,
                dcapsMismatchPrimer: null,
                wtSpecificPrimer: null,
                mutSpecificPrimer: null,
                commonPrimer: null,
                kaspGenomicSequence: null,
                sslpMarkerName: null,
                sslpDistance: null,
                sslpGenomicLocation: null,
                sslpInducedBackground: null,
                sslpOutcrossedBackground: null,
                sslpInducedPcr: null,
                sslpOutcrossedPcr: null,
                files: [],
            }),
            addRequiresTypePick: {
                title: 'Pick an assay type',
                description: 'The type determines which fields and file uploads appear.',
                options: ASSAY_TYPE_OPTIONS,
                targetPath: 'assayType',
            },
            collapseWhen: row => !!(row as Partial<GenotypingAssayDTO>).assayType,
            summarize: summarizeAssay,
            childTemplate: [
                field({
                    path: 'assayType',
                    label: 'Assay type',
                    type: 'select',
                    options: ASSAY_TYPE_OPTIONS,
                }),
                // All possible assay fields, gated by visibleAssayFields(assayType).
                ...((Object.keys(ASSAY_FIELD_DEFS) as AssayFieldKey[]).map(assayField)),
            ],
        }),
    ]),

    section('phenotypes', 'Phenotypes', [
        array({
            id: 'phenotypes',
            path: 'phenotypes',
            title: 'Phenotypes',
            emptyMessage: 'No phenotypes recorded.',
            itemLabel: idx => `Phenotype ${idx + 1}`,
            maxItems: 10,
            newRow: () => ({
                id: null,
                sortOrder: null,
                description: null,
                hpfStart: null,
                hpfEnd: null,
                stage: null,
                zfinImagePermission: null,
                zircImagePermission: null,
                nonMendelianPercentage: null,
                nonMendelianComment: null,
                segregation: [],
                type: [],
            }),
            collapseWhen: row => !!(row as Partial<PhenotypeDTO>).description,
            summarize: summarizePhenotype,
            childTemplate: [
                field({path: 'description', label: 'Description', type: 'textarea'}),
                // Timing widget is genuinely unusual (hpf/dpf unit toggle
                // with server-derived stage display). CustomNode escape
                // hatch into PhenotypeTimingRow.
                custom({
                    id: 'phen-timing',
                    render: ({row, onCommit}) => {
                        const r = row as Partial<PhenotypeDTO>;
                        return (
                            <PhenotypeTimingRow
                                hpfStart={r.hpfStart ?? null}
                                hpfEnd={r.hpfEnd ?? null}
                                stage={r.stage ?? null}
                                onCommit={onCommit}
                            />
                        );
                    },
                }),
                field({path: 'zfinImagePermission', label: 'ZFIN image permission?', type: 'bool'}),
                field({path: 'zircImagePermission', label: 'ZIRC image permission?', type: 'bool'}),
                field({
                    path: 'nonMendelianPercentage',
                    label: 'Non-Mendelian %',
                    type: 'int',
                    suffix: '%',
                }),
                field({path: 'nonMendelianComment', label: 'Non-Mendelian comment', type: 'textarea'}),
                // Segregation and type are string[] -- multi-checkbox-with-other
                // would fit, but the canonical lists aren't locked down yet.
                // For now, render as simple text fields with CSV semantics that
                // the container's save callback splits/joins as needed.
                custom({
                    id: 'phen-segregation',
                    render: ({row, onCommit}) => {
                        const r = row as Partial<PhenotypeDTO>;
                        return (
                            <div className='form-group row'>
                                <label className='col-sm-3 col-form-label'>Segregation (CSV)</label>
                                <div className='col-sm-9'>
                                    <input
                                        type='text'
                                        className='form-control'
                                        defaultValue={(r.segregation ?? []).join(', ')}
                                        onBlur={e => {
                                            const arr = e.target.value
                                                .split(',')
                                                .map(s => s.trim())
                                                .filter(Boolean);
                                            onCommit({segregation: arr});
                                        }}
                                    />
                                </div>
                            </div>
                        );
                    },
                }),
                custom({
                    id: 'phen-type',
                    render: ({row, onCommit}) => {
                        const r = row as Partial<PhenotypeDTO>;
                        return (
                            <div className='form-group row'>
                                <label className='col-sm-3 col-form-label'>Phenotype type (CSV)</label>
                                <div className='col-sm-9'>
                                    <input
                                        type='text'
                                        className='form-control'
                                        defaultValue={(r.type ?? []).join(', ')}
                                        onBlur={e => {
                                            const arr = e.target.value
                                                .split(',')
                                                .map(s => s.trim())
                                                .filter(Boolean);
                                            onCommit({type: arr});
                                        }}
                                    />
                                </div>
                            </div>
                        );
                    },
                }),
            ],
        }),
    ]),

    section('lethality', 'Lethality', [
        field({path: 'homozygousLethal',           label: 'Homozygous Lethal',     type: 'bool'}),
        field({path: 'lethalityStageTypical',      label: 'Stage (Typical)',       type: 'text'}),
        field({path: 'lethalitySpecificTimepoint', label: 'Specific Timepoint',    type: 'text'}),
        field({path: 'lethalityWindowStart',       label: 'Window Start',          type: 'text'}),
        field({path: 'lethalityWindowEnd',         label: 'Window End',            type: 'text'}),
        field({path: 'lethalityAdditionalInfo',    label: 'Additional Info',       type: 'textarea'}),
    ]),

    section('publications', 'Publications', [
        // Publications wire as a flat string[] which doesn't fit ArrayNode's
        // object-row model. CustomNode handles the list of strings; saves
        // route through onCommit({publications: ...}) → /patch.
        custom({
            id: 'publications-list',
            render: ({dto, onChange, onCommit}) => {
                const m = dto as MutationDTO;
                const pubs = m.publications ?? [];
                const replace = (next: string[]) => onChange({publications: next});
                const save = (next: string[]) =>
                    onCommit({publications: next.map(s => s.trim()).filter(Boolean)});
                return (
                    <div>
                        {pubs.length === 0 && (
                            <p className='text-muted'>No publications recorded.</p>
                        )}
                        {pubs.map((p, i) => (
                            <div className='d-flex mb-2' style={{gap: 8}} key={i}>
                                <input
                                    type='text'
                                    className='form-control'
                                    placeholder='Citation, PMID, DOI, or ZDB Pub ID'
                                    value={p}
                                    // Local-only update so freshly-typed text
                                    // shows immediately without round-tripping.
                                    onChange={e => {
                                        const next = [...pubs];
                                        next[i] = e.target.value;
                                        replace(next);
                                    }}
                                    // Commit on blur, trimming + dropping blanks.
                                    onBlur={() => save(pubs)}
                                />
                                <button
                                    type='button'
                                    className='btn btn-sm btn-outline-danger'
                                    onClick={() => save(pubs.filter((_, j) => j !== i))}
                                >
                                    Remove
                                </button>
                            </div>
                        ))}
                        <button
                            type='button'
                            className='btn btn-sm btn-outline-secondary'
                            // Local-only add — server would strip a blank row.
                            // The actual save fires when the curator types into
                            // the new row and blurs.
                            onClick={() => replace([...pubs, ''])}
                            disabled={pubs.length >= 20}
                        >
                            + Add publication
                        </button>
                    </div>
                );
            },
        }),
    ]),
];
