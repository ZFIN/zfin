import React, {useEffect, useRef, useState} from 'react';
import SaveToast, {SaveEvent} from '../components/zirc/SaveToast';
import {FieldDef, FieldRow, FieldsTable, Section, valueToInputString} from '../components/zirc/FormPrimitives';

interface LinkedFeatureWire {
    mutationAId: number | null;
    mutationBId: number | null;
    /** Server-supplied display echo; absent on outbound. */
    mutationALabel?: string | null;
    mutationBLabel?: string | null;
    distanceKnown: boolean | null;
    distanceCentimorgans: number | null;
    distanceMegabases: number | null;
    additionalInfo: string | null;
}

interface MutationSummary {
    id: number;
    sortOrder: number | null;
    alleleDesignation: string | null;
    mutagenesisProtocol: string | null;
    mutationType: string | null;
    mutationDiscoverer: string | null;
}

interface LineSubmissionDTO {
    zdbID: string;
    name: string | null;
    abbreviation: string | null;
    previousNames: string | null;
    featuresLinked: boolean | null;
    maternalBackground: string | null;
    paternalBackground: string | null;
    backgroundChangeable: boolean | null;
    backgroundChangeConcerns: string | null;
    unreportedFeaturesDetails: string | null;
    additionalInfo: string | null;
    singleAllelic: boolean | null;
    husbandryInfo: string | null;
    reasons?: string[] | null;
    reasonsOther?: string | null;
    linkedFeatures?: LinkedFeatureWire[] | null;
    mutations?: MutationSummary[] | null;
    isDraft?: boolean | null;
}

type ScalarField = Exclude<keyof LineSubmissionDTO,
    'zdbID' | 'isDraft' | 'reasons' | 'reasonsOther'
    | 'linkedFeatures' | 'mutations'>;

interface LineSubmissionEditProps {
    /** Empty string for the /new flow — the first save creates the row. */
    submissionId: string;
}

const OVERVIEW_FIELDS: FieldDef<ScalarField>[] = [
    {field: 'name',           label: 'Name',           type: 'text'},
    {field: 'previousNames',  label: 'Previous Names', type: 'text'},
    {field: 'featuresLinked', label: 'Features Linked', type: 'bool'},
];

const BACKGROUND_FIELDS: FieldDef<ScalarField>[] = [
    {field: 'singleAllelic',            label: 'Single-allelic submission', type: 'bool'},
    {field: 'backgroundChangeable',     label: 'Background Changeable', type: 'bool'},
    {field: 'backgroundChangeConcerns', label: 'Concerns',              type: 'textarea'},
];

// Maternal / paternal background render as dropdown-with-Other instead of
// plain text fields, but the data still lives in the same free-text column
// so we keep them in ALL_FIELDS for the seeding pass. The render order is
// hand-stitched in the Background section JSX.
const BACKGROUND_DROPDOWN_FIELDS: FieldDef<ScalarField>[] = [
    {field: 'maternalBackground', label: 'Maternal', type: 'text'},
    {field: 'paternalBackground', label: 'Paternal', type: 'text'},
];

// Canonical-but-revisable list. Picking "Other" reveals a free-text input
// that writes to the same column. The ZIRC team is expected to refine this
// enum; once stable, B10 will lock it down with a check constraint.
const BACKGROUND_OPTIONS: Array<{value: string; label: string}> = [
    {value: 'AB',      label: 'AB'},
    {value: 'TU',      label: 'TU'},
    {value: 'WIK',     label: 'WIK'},
    {value: 'AB/TU',   label: 'AB/TU'},
    {value: 'unknown', label: 'unknown'},
];

const ADDITIONAL_FIELDS: FieldDef<ScalarField>[] = [
    {field: 'unreportedFeaturesDetails', label: 'Unreported Features Details', type: 'textarea'},
    {field: 'husbandryInfo',             label: 'Husbandry Info',              type: 'textarea',
        placeholder: 'Husbandry-specific information, e.g. special feeding regime'},
    {field: 'additionalInfo',            label: 'Additional Info',             type: 'textarea'},
];

const ALL_FIELDS: FieldDef<ScalarField>[] = [
    ...OVERVIEW_FIELDS,
    ...BACKGROUND_FIELDS,
    ...BACKGROUND_DROPDOWN_FIELDS,
    ...ADDITIONAL_FIELDS,
];

const REASON_OPTIONS: Array<{value: string; label: string}> = [
    {value: 'frequently_requested',     label: 'Currently frequently requested'},
    {value: 'expect_high_demand',       label: 'Expect high demand'},
    {value: 'interesting_gene',         label: 'Interesting gene'},
    {value: 'community_resource',       label: 'Community resource/tool'},
    {value: 'mutant_gene_cloned',       label: 'Mutant gene cloned'},
    {value: 'danger_of_losing',         label: 'Danger of losing line'},
    {value: 'lack_of_space_or_funding', label: 'Lack of space or funding to maintain line'},
];

interface ReasonsSectionProps {
    reasons: string[];
    reasonsOther: string;
    otherChecked: boolean;
    onToggleCanonical: (value: string, checked: boolean) => void;
    onToggleOther: (checked: boolean) => void;
    onChangeOther: (text: string) => void;
    onCommitOther: () => void;
}

const ReasonsSection = ({
    reasons,
    reasonsOther,
    otherChecked,
    onToggleCanonical,
    onToggleOther,
    onChangeOther,
    onCommitOther,
}: ReasonsSectionProps) => {
    const otherTextId = 'ls-reasons-other-text';
    return (
        <fieldset className='border-0 p-0 m-0'>
            <legend className='sr-only'>Reasons why ZIRC should accept this line</legend>
            {REASON_OPTIONS.map(opt => {
                const id = `ls-reason-${opt.value}`;
                const checked = reasons.includes(opt.value);
                return (
                    <div className='form-check' key={opt.value}>
                        <input
                            type='checkbox'
                            id={id}
                            className='form-check-input'
                            checked={checked}
                            onChange={e => onToggleCanonical(opt.value, e.target.checked)}
                        />
                        <label className='form-check-label' htmlFor={id}>{opt.label}</label>
                    </div>
                );
            })}
            <div className='form-check'>
                <input
                    type='checkbox'
                    id='ls-reason-other'
                    className='form-check-input'
                    checked={otherChecked}
                    onChange={e => onToggleOther(e.target.checked)}
                />
                <label className='form-check-label' htmlFor='ls-reason-other'>Other</label>
            </div>
            {otherChecked && (
                <div className='mt-2 ml-4' style={{maxWidth: 600}}>
                    <label htmlFor={otherTextId} className='sr-only'>Other reason details</label>
                    <input
                        type='text'
                        id={otherTextId}
                        className='form-control'
                        placeholder='Describe the other reason'
                        value={reasonsOther}
                        onChange={e => onChangeOther(e.target.value)}
                        onBlur={onCommitOther}
                        autoFocus
                    />
                </div>
            )}
        </fieldset>
    );
};

interface DropdownWithOtherRowProps {
    field: ScalarField;
    label: string;
    value: string;
    options: Array<{value: string; label: string}>;
    onChange: (next: string) => void;
    onCommit: (next: string) => void;
}

/**
 * Single-select dropdown of canonical values plus an "Other" option that
 * reveals a free-text input. The wire value is a single string column —
 * picking a canonical writes that value, picking Other writes whatever
 * the user types. Used for maternal / paternal background where the
 * canonical enum isn't yet locked down. Lives in a <tr> for FieldsTable.
 */
const DropdownWithOtherRow = ({field, label, value, options, onChange, onCommit}: DropdownWithOtherRowProps) => {
    const inputId = `ls-field-${field}`;
    const otherInputId = `${inputId}-other`;
    const labelId = `ls-field-label-${field}`;
    const isCanonical = !!value && options.some(o => o.value === value);
    // otherMode is derived (non-blank, non-canonical value), with an
    // additional manual override for "user just picked Other but hasn't
    // typed yet" — without it the text input would disappear the moment
    // the user clears it.
    const [otherPicked, setOtherPicked] = useState<boolean>(false);
    const otherMode = otherPicked || (!isCanonical && !!value);
    const selectValue = otherMode ? '__other' : (isCanonical ? value : '');

    function handleSelectChange(picked: string) {
        if (picked === '__other') {
            setOtherPicked(true);
            // If the user was on a canonical value, clear it so the Other
            // text input starts empty rather than echoing the canonical.
            if (isCanonical) {
                onChange('');
                onCommit('');
            }
            return;
        }
        setOtherPicked(false);
        onChange(picked);
        onCommit(picked);
    }

    return (
        <tr>
            <th className='w-25' scope='row' id={labelId}>
                <label htmlFor={inputId} className='mb-0'>{label}</label>
            </th>
            <td>
                <div className='d-flex' style={{gap: 8}}>
                    <select
                        id={inputId}
                        className='form-control'
                        value={selectValue}
                        onChange={e => handleSelectChange(e.target.value)}
                        style={{maxWidth: 200}}
                    >
                        <option value=''>(select)</option>
                        {options.map(opt => (
                            <option key={opt.value} value={opt.value}>{opt.label}</option>
                        ))}
                        <option value='__other'>Other</option>
                    </select>
                    {otherMode && (
                        <input
                            type='text'
                            id={otherInputId}
                            className='form-control'
                            placeholder='Other background'
                            value={value}
                            onChange={e => onChange(e.target.value)}
                            onBlur={() => onCommit(value)}
                            aria-label={`${label} (other)`}
                            autoFocus={otherPicked}
                        />
                    )}
                </div>
            </td>
        </tr>
    );
};

interface LinkedFeatureRow {
    /** Local-only React key. Stable across re-renders so input focus isn't lost. */
    rowId: string;
    /** Empty string when the dropdown hasn't been picked yet. */
    mutationAId: string;
    mutationBId: string;
    /** '' = unanswered, 'true' / 'false' = picked. Matches the bool radio convention. */
    distanceKnown: '' | 'true' | 'false';
    /** One distance input + a unit selector (per the form spec). On the wire
     *  we still store it in the matching of the two schema columns. */
    distanceValue: string;
    distanceUnit: '' | 'cM' | 'Mb';
    additionalInfo: string;
}

let nextRowId = 1;
function freshRowId(): string {
    return `lf-${nextRowId++}`;
}

function emptyLinkedFeatureRow(): LinkedFeatureRow {
    return {
        rowId: freshRowId(),
        mutationAId: '',
        mutationBId: '',
        distanceKnown: '',
        distanceValue: '',
        distanceUnit: '',
        additionalInfo: '',
    };
}

function wireToRow(w: LinkedFeatureWire): LinkedFeatureRow {
    // Schema stores cM and Mb as separate columns; UI exposes one input
    // + unit selector. Read whichever column is populated; if (somehow)
    // both, prefer cM and the user can re-pick the unit.
    let distanceValue = '';
    let distanceUnit: '' | 'cM' | 'Mb' = '';
    if (w.distanceCentimorgans != null) {
        distanceValue = String(w.distanceCentimorgans);
        distanceUnit = 'cM';
    } else if (w.distanceMegabases != null) {
        distanceValue = String(w.distanceMegabases);
        distanceUnit = 'Mb';
    }
    return {
        rowId: freshRowId(),
        mutationAId: w.mutationAId == null ? '' : String(w.mutationAId),
        mutationBId: w.mutationBId == null ? '' : String(w.mutationBId),
        distanceKnown: w.distanceKnown === true ? 'true'
            : w.distanceKnown === false ? 'false'
                : '',
        distanceValue,
        distanceUnit,
        additionalInfo: w.additionalInfo ?? '',
    };
}

function rowToWire(r: LinkedFeatureRow): LinkedFeatureWire {
    function parseInt10OrNull(s: string): number | null {
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
        const n = Number(s);
        return Number.isFinite(n) ? n : null;
    }
    const value = parseFloatOrNull(r.distanceValue);
    return {
        mutationAId: parseInt10OrNull(r.mutationAId),
        mutationBId: parseInt10OrNull(r.mutationBId),
        distanceKnown: r.distanceKnown === 'true' ? true
            : r.distanceKnown === 'false' ? false
                : null,
        // Map the single value into the matching schema column based on
        // unit; clear the other column so we don't leave stale data when
        // the unit changes.
        distanceCentimorgans: r.distanceUnit === 'cM' ? value : null,
        distanceMegabases:    r.distanceUnit === 'Mb' ? value : null,
        additionalInfo: r.additionalInfo.trim() || null,
    };
}

function mutationLabel(m: MutationSummary): string {
    if (m.alleleDesignation && m.alleleDesignation.trim()) {
        return m.alleleDesignation;
    }
    return `#${m.sortOrder ?? '?'}`;
}

interface MutationsSectionProps {
    submissionId: string;
    mutations: MutationSummary[];
    onRemove: (id: number, label: string) => void;
}

/** Mirrors LineSubmissionService.MAX_MUTATIONS_PER_SUBMISSION. */
const MAX_MUTATIONS = 5;

const MutationsSection = ({submissionId, mutations, onRemove}: MutationsSectionProps) => {
    const addUrl = submissionId
        ? `/action/zirc/line-submission/${encodeURIComponent(submissionId)}/mutation/new`
        : null;
    const atCap = mutations.length >= MAX_MUTATIONS;
    const capReason = `Maximum ${MAX_MUTATIONS} mutations per submission.`;
    return (
        <div>
            {mutations.length === 0
                ? <p className='text-muted'>No mutations recorded for this submission.</p>
                : <table className='table table-striped'>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Allele Designation</th>
                            <th>Mutagenesis Protocol</th>
                            <th>Mutation Type</th>
                            <th>Discoverer</th>
                            <th className='text-right'>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {mutations.map(m => {
                            const label = m.alleleDesignation && m.alleleDesignation.trim()
                                ? m.alleleDesignation
                                : `#${m.sortOrder ?? '?'}`;
                            return (
                                <tr key={m.id}>
                                    <td>{m.sortOrder ?? '—'}</td>
                                    <td>{m.alleleDesignation ?? <span className='text-muted'>—</span>}</td>
                                    <td>{m.mutagenesisProtocol ?? <span className='text-muted'>—</span>}</td>
                                    <td>{m.mutationType ?? <span className='text-muted'>—</span>}</td>
                                    <td>{m.mutationDiscoverer ?? <span className='text-muted'>—</span>}</td>
                                    <td className='text-right'>
                                        <a
                                            className='btn btn-sm btn-outline-primary mr-2'
                                            href={`/action/zirc/mutation/${m.id}/edit`}
                                        >
                                            Edit
                                        </a>
                                        <button
                                            type='button'
                                            className='btn btn-sm btn-outline-danger'
                                            onClick={() => onRemove(m.id, label)}
                                        >
                                            Remove
                                        </button>
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            }
            {addUrl && !atCap
                ? <a href={addUrl} className='btn btn-sm btn-outline-secondary'>+ Add mutation</a>
                : (
                    <button
                        type='button'
                        className='btn btn-sm btn-outline-secondary'
                        disabled
                        title={atCap ? capReason : 'Save a field on this submission first'}
                    >
                        + Add mutation
                    </button>
                )}
        </div>
    );
};

interface LinkedFeaturesSectionProps {
    rows: LinkedFeatureRow[];
    /** Mutations on this submission, used to populate the per-row dropdowns. */
    mutations: MutationSummary[];
    onAdd: () => void;
    onRemove: (rowId: string) => void;
    onChange: (rowId: string, patch: Partial<LinkedFeatureRow>) => void;
    /** Called when a row's input loses focus or a radio is picked. The
     *  parent reads the latest state from a ref, so no rowId is needed. */
    onCommitRow: () => void;
}

function summarizeLinkedFeature(row: LinkedFeatureRow, mutations: MutationSummary[]): string {
    const lookup = (idStr: string) => mutations.find(m => String(m.id) === idStr);
    const a = lookup(row.mutationAId);
    const b = lookup(row.mutationBId);
    const aLabel = a ? mutationLabel(a) : '?';
    const bLabel = b ? mutationLabel(b) : '?';
    const pair = `${aLabel} ↔ ${bLabel}`;
    if (row.distanceKnown === 'true' && row.distanceValue && row.distanceUnit) {
        return `${pair} · ${row.distanceValue} ${row.distanceUnit}`;
    }
    return pair;
}

/**
 * Per-row collapse state for the linked-features section. Rows whose pair
 * isn't fully picked default to expanded; complete rows default to
 * collapsed. Explicit Done / Edit overrides take precedence.
 */
function useLinkedFeatureExpansion() {
    const [expandedSet, setExpandedSet] = useState<Set<string>>(new Set());
    const [collapsedSet, setCollapsedSet] = useState<Set<string>>(new Set());

    function isExpanded(row: LinkedFeatureRow): boolean {
        if (expandedSet.has(row.rowId)) {
            return true;
        }
        if (collapsedSet.has(row.rowId)) {
            return false;
        }
        return !row.mutationAId || !row.mutationBId;
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

const LinkedFeaturesSection = ({rows, mutations, onAdd, onRemove, onChange, onCommitRow}: LinkedFeaturesSectionProps) => {
    const expansion = useLinkedFeatureExpansion();
    const noMutations = mutations.length < 2;
    if (rows.length === 0) {
        return (
            <div>
                {noMutations
                    ? <p className='text-muted'>
                        Linked features pair two mutations on this submission.
                        Add at least two mutations above to get started.
                    </p>
                    : <p className='text-muted'>No linked features.</p>}
                <button
                    type='button'
                    className='btn btn-sm btn-outline-secondary'
                    onClick={onAdd}
                    disabled={noMutations}
                    title={noMutations ? 'Add at least two mutations first' : undefined}
                >
                    + Add linked feature
                </button>
            </div>
        );
    }
    return (
        <div>
            {rows.map((row, idx) => {
                const aId = `ls-lf-mutA-${row.rowId}`;
                const bId = `ls-lf-mutB-${row.rowId}`;
                const knownGroup = `ls-lf-known-${row.rowId}`;
                const knownLabelId = `ls-lf-known-label-${row.rowId}`;
                const distId = `ls-lf-dist-${row.rowId}`;
                const unitGroup = `ls-lf-unit-${row.rowId}`;
                const unitLabelId = `ls-lf-unit-label-${row.rowId}`;
                const infoId = `ls-lf-info-${row.rowId}`;
                const showDistance = row.distanceKnown === 'true';
                if (!expansion.isExpanded(row)) {
                    return (
                        <div key={row.rowId} className='border rounded p-2 mb-2 d-flex align-items-center'>
                            <div className='flex-grow-1' style={{minWidth: 0}}>
                                <span className='text-muted small mr-2'>Linked feature {idx + 1}</span>
                                <span style={{overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap'}}>
                                    {summarizeLinkedFeature(row, mutations)}
                                </span>
                            </div>
                            <div style={{flexShrink: 0}}>
                                <button
                                    type='button'
                                    className='btn btn-sm btn-outline-secondary mr-2'
                                    onClick={() => expansion.expand(row.rowId)}
                                >
                                    Edit
                                </button>
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
                }
                return (
                    <fieldset key={row.rowId} className='border rounded p-3 mb-3'>
                        <legend className='h6 px-2' style={{width: 'auto'}}>
                            Linked feature {idx + 1}
                        </legend>
                        <div className='form-group row'>
                            <label htmlFor={aId} className='col-sm-3 col-form-label'>Mutation A</label>
                            <div className='col-sm-9'>
                                <select
                                    id={aId}
                                    className='form-control'
                                    value={row.mutationAId}
                                    onChange={e => {
                                        onChange(row.rowId, {mutationAId: e.target.value});
                                        onCommitRow();
                                    }}
                                >
                                    <option value=''>(select mutation)</option>
                                    {mutations.map(m => (
                                        <option key={m.id} value={String(m.id)}>{mutationLabel(m)}</option>
                                    ))}
                                </select>
                            </div>
                        </div>
                        <div className='form-group row'>
                            <label htmlFor={bId} className='col-sm-3 col-form-label'>Mutation B</label>
                            <div className='col-sm-9'>
                                <select
                                    id={bId}
                                    className='form-control'
                                    value={row.mutationBId}
                                    onChange={e => {
                                        onChange(row.rowId, {mutationBId: e.target.value});
                                        onCommitRow();
                                    }}
                                >
                                    <option value=''>(select mutation)</option>
                                    {mutations
                                        .filter(m => String(m.id) !== row.mutationAId)
                                        .map(m => (
                                            <option key={m.id} value={String(m.id)}>{mutationLabel(m)}</option>
                                        ))}
                                </select>
                            </div>
                        </div>
                        <div className='form-group row'>
                            <span id={knownLabelId} className='col-sm-3 col-form-label'>Distance known?</span>
                            <div className='col-sm-9' role='radiogroup' aria-labelledby={knownLabelId}>
                                {[['true', 'Yes'], ['false', 'No']].map(([v, lbl]) => {
                                    const id = `${knownGroup}-${v}`;
                                    return (
                                        <div className='form-check form-check-inline' key={v}>
                                            <input
                                                type='radio'
                                                id={id}
                                                className='form-check-input'
                                                name={knownGroup}
                                                value={v}
                                                checked={row.distanceKnown === v}
                                                onChange={() => {
                                                    onChange(row.rowId, {distanceKnown: v as 'true' | 'false'});
                                                    onCommitRow();
                                                }}
                                            />
                                            <label className='form-check-label' htmlFor={id}>{lbl}</label>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                        {showDistance && (
                            <div className='form-group row'>
                                <label htmlFor={distId} className='col-sm-3 col-form-label'>Distance</label>
                                <div className='col-sm-9'>
                                    <div className='d-flex align-items-center' style={{gap: 12}}>
                                        <input
                                            type='number'
                                            id={distId}
                                            step='any'
                                            className='form-control'
                                            style={{maxWidth: 160}}
                                            value={row.distanceValue}
                                            onChange={e => onChange(row.rowId, {distanceValue: e.target.value})}
                                            onBlur={() => onCommitRow()}
                                        />
                                        <span id={unitLabelId} className='sr-only'>Distance unit</span>
                                        <div role='radiogroup' aria-labelledby={unitLabelId}>
                                            {[['cM', 'cM'], ['Mb', 'Mb']].map(([v, lbl]) => {
                                                const id = `${unitGroup}-${v}`;
                                                return (
                                                    <div className='form-check form-check-inline' key={v}>
                                                        <input
                                                            type='radio'
                                                            id={id}
                                                            className='form-check-input'
                                                            name={unitGroup}
                                                            value={v}
                                                            checked={row.distanceUnit === v}
                                                            onChange={() => {
                                                                onChange(row.rowId, {distanceUnit: v as 'cM' | 'Mb'});
                                                                onCommitRow();
                                                            }}
                                                        />
                                                        <label className='form-check-label' htmlFor={id}>{lbl}</label>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}
                        <div className='form-group row mb-0'>
                            <label htmlFor={infoId} className='col-sm-3 col-form-label'>Additional info</label>
                            <div className='col-sm-9'>
                                <textarea
                                    id={infoId}
                                    className='form-control'
                                    rows={2}
                                    value={row.additionalInfo}
                                    onChange={e => onChange(row.rowId, {additionalInfo: e.target.value})}
                                    onBlur={() => onCommitRow()}
                                />
                            </div>
                        </div>
                        <div className='text-right mt-2'>
                            <button
                                type='button'
                                className='btn btn-sm btn-outline-primary mr-2'
                                onClick={() => expansion.collapse(row.rowId)}
                            >
                                Done
                            </button>
                            <button
                                type='button'
                                className='btn btn-sm btn-outline-danger'
                                onClick={() => onRemove(row.rowId)}
                            >
                                Remove
                            </button>
                        </div>
                    </fieldset>
                );
            })}
            <button
                type='button'
                className='btn btn-sm btn-outline-secondary'
                onClick={onAdd}
                disabled={noMutations}
                title={noMutations ? 'Add at least two mutations first' : undefined}
            >
                + Add linked feature
            </button>
        </div>
    );
};

const LineSubmissionEdit = ({submissionId}: LineSubmissionEditProps) => {
    const [zdbID, setZdbID] = useState<string>(submissionId);
    const [values, setValues] = useState<Record<string, string> | null>(submissionId ? null : emptyValues());
    const [committed, setCommitted] = useState<Record<string, string>>(submissionId ? {} : emptyValues());
    const [reasons, setReasons] = useState<string[]>([]);
    const [reasonsOther, setReasonsOther] = useState<string>('');
    const [otherChecked, setOtherChecked] = useState<boolean>(false);
    const [committedReasonsOther, setCommittedReasonsOther] = useState<string>('');
    const [linkedFeatures, setLinkedFeaturesState] = useState<LinkedFeatureRow[]>([]);
    /** JSON of the wire-format linked features as they last appeared on the server. */
    const [committedLinkedFeaturesJSON, setCommittedLinkedFeaturesJSON] = useState<string>('[]');
    const [mutations, setMutations] = useState<MutationSummary[]>([]);
    /**
     * Mirror of {@link linkedFeatures}. Read from this ref inside save logic so
     * synchronous change-then-commit handlers (e.g. the radio's onChange that
     * fires both `change` and `commit` in one event) see the post-change array
     * even before React re-renders.
     */
    const linkedFeaturesRef = useRef<LinkedFeatureRow[]>([]);
    const [loadError, setLoadError] = useState<string>('');
    const [saveEvent, setSaveEvent] = useState<SaveEvent | null>(null);

    // Monotonic seq counter for SaveToast — useRef so concurrent calls inside a
    // single render don't both compute the same next value (closure capture
    // would otherwise see the same saveSeq snapshot).
    const seqRef = useRef<number>(0);

    // Serialize all save POSTs through one queue. Reasons:
    //  (1) When zdbID is empty, two concurrent save-field POSTs would each
    //      trigger create-on-first-save and we'd end up with two rows.
    //  (2) Out-of-order responses can revert newer edits if we naively
    //      applyDTO(response) — see code review on the previous commit.
    // The queue ensures at most one save is in flight at a time.
    const saveQueueRef = useRef<Promise<unknown>>(Promise.resolve());

    // Mirror of zdbID for the save closures. State updates from concurrent
    // saves race with closure capture; the ref always reflects the latest
    // server-assigned ID.
    const zdbIDRef = useRef<string>(submissionId);

    useEffect(() => {
        if (!submissionId) {
            return;
        }
        let cancelled = false;
        fetch(`/action/zirc/line-submission/${submissionId}.json`)
            .then(r => {
                if (!r.ok) {
                    throw new Error(`HTTP ${r.status}`);
                }
                return r.json() as Promise<LineSubmissionDTO>;
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
    }, [submissionId]);

    function applyDTO(data: LineSubmissionDTO) {
        const initial: Record<string, string> = {};
        ALL_FIELDS.forEach(d => {
            initial[d.field] = valueToInputString(data[d.field]);
        });
        setValues(initial);
        setCommitted(initial);
        setZdbID(data.zdbID);
        zdbIDRef.current = data.zdbID;
        const loadedReasons = Array.isArray(data.reasons) ? data.reasons : [];
        const loadedOther = data.reasonsOther ?? '';
        setReasons(loadedReasons);
        setReasonsOther(loadedOther);
        setOtherChecked(loadedOther.trim().length > 0);
        setCommittedReasonsOther(loadedOther);
        applyLinkedFeaturesFromDTO(data.linkedFeatures ?? []);
        setMutations(data.mutations ?? []);
    }

    function setLinkedFeatures(next: LinkedFeatureRow[]) {
        linkedFeaturesRef.current = next;
        setLinkedFeaturesState(next);
    }

    function applyLinkedFeaturesFromDTO(wire: LinkedFeatureWire[]) {
        setLinkedFeatures(wire.map(wireToRow));
        // Snapshot the wire-format JSON so subsequent commits can dedupe no-ops.
        // Include exactly the fields commitLinkedFeatures() sends so the dedup
        // compares apples to apples — labels are server-supplied echoes and
        // must not appear in either snapshot.
        setCommittedLinkedFeaturesJSON(JSON.stringify(wire.map(w => ({
            mutationAId: w.mutationAId ?? null,
            mutationBId: w.mutationBId ?? null,
            distanceKnown: w.distanceKnown ?? null,
            distanceCentimorgans: w.distanceCentimorgans ?? null,
            distanceMegabases: w.distanceMegabases ?? null,
            additionalInfo: w.additionalInfo ?? null,
        }))));
    }

    function setFieldValue(field: ScalarField, next: string) {
        setValues(prev => prev ? {...prev, [field]: next} : prev);
    }

    function emit(event: Omit<SaveEvent, 'seq'>) {
        seqRef.current += 1;
        setSaveEvent({...event, seq: seqRef.current});
    }

    /**
     * Run a save through the serialization queue. The queue swallows previous
     * failures so one bad save doesn't poison the chain.
     */
    function enqueueSave<T>(fn: () => Promise<T>): Promise<T> {
        const next = saveQueueRef.current.then(fn, fn);
        saveQueueRef.current = next.catch(() => undefined);
        return next;
    }

    function captureNewIdIfAny(data: LineSubmissionDTO, wasNew: boolean) {
        if (wasNew && data.zdbID) {
            setZdbID(data.zdbID);
            zdbIDRef.current = data.zdbID;
            window.history.replaceState(null, '', `/action/zirc/line-submission/${data.zdbID}/edit`);
        }
    }

    async function commit(field: FieldDef, next: string) {
        if (next === committed[field.field]) {
            return;
        }
        emit({status: 'saving', label: field.label});
        // Optimistically mark this field "committed" so a subsequent equal-value
        // blur doesn't fire a redundant save; if the actual save fails we'll
        // see the error toast and the field's display value stays as the user
        // typed it.
        setCommitted(prev => ({...prev, [field.field]: next}));
        try {
            await enqueueSave(async () => {
                const wasNew = !zdbIDRef.current;
                const body = new URLSearchParams();
                if (zdbIDRef.current) {
                    body.append('zdbID', zdbIDRef.current);
                }
                body.append('field', field.field);
                body.append('value', next);
                const resp = await fetch('/action/zirc/line-submission/save-field', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: body.toString(),
                });
                if (!resp.ok) {
                    throw new Error(`HTTP ${resp.status}`);
                }
                const data = await resp.json() as LineSubmissionDTO;
                // Update only the saved field from the server's normalized
                // value — applying the whole DTO would clobber concurrent
                // edits to other fields if responses arrived out of order.
                // (Serialization makes that less likely, but per-field
                // updates remove the foot-gun entirely.)
                const normalized = valueToInputString(
                    (data as unknown as Record<string, string | boolean | null | undefined>)[field.field]);
                setValues(prev => prev ? {...prev, [field.field]: normalized} : prev);
                setCommitted(prev => ({...prev, [field.field]: normalized}));
                captureNewIdIfAny(data, wasNew);
            });
            emit({status: 'saved', label: field.label});
        } catch (e) {
            emit({status: 'error', label: field.label, message: e instanceof Error ? e.message : 'Save failed'});
        }
    }

    async function commitReasons(nextReasons: string[], nextOther: string) {
        emit({status: 'saving', label: 'Acceptance Reasons'});
        try {
            await enqueueSave(async () => {
                const wasNew = !zdbIDRef.current;
                const body = new URLSearchParams();
                if (zdbIDRef.current) {
                    body.append('zdbID', zdbIDRef.current);
                }
                nextReasons.forEach(r => body.append('reasons', r));
                body.append('reasonsOther', nextOther);
                const resp = await fetch('/action/zirc/line-submission/save-reasons', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: body.toString(),
                });
                if (!resp.ok) {
                    throw new Error(`HTTP ${resp.status}`);
                }
                const data = await resp.json() as LineSubmissionDTO;
                // Reasons are saved as a section: replacing the whole section
                // state from the server is the right thing here (no concurrent
                // edits to "part of" reasons). We deliberately don't applyDTO
                // since that would clobber unrelated scalar edits.
                const normalizedReasons = Array.isArray(data.reasons) ? data.reasons : [];
                const normalizedOther = data.reasonsOther ?? '';
                setReasons(normalizedReasons);
                setReasonsOther(normalizedOther);
                setOtherChecked(normalizedOther.trim().length > 0);
                setCommittedReasonsOther(normalizedOther);
                captureNewIdIfAny(data, wasNew);
            });
            emit({status: 'saved', label: 'Acceptance Reasons'});
        } catch (e) {
            emit({status: 'error', label: 'Acceptance Reasons',
                message: e instanceof Error ? e.message : 'Save failed'});
        }
    }

    function handleToggleCanonical(value: string, checked: boolean) {
        const next = checked
            ? [...reasons, value]
            : reasons.filter(r => r !== value);
        setReasons(next);
        commitReasons(next, otherChecked ? reasonsOther : '');
    }

    function handleToggleOther(checked: boolean) {
        setOtherChecked(checked);
        if (!checked) {
            setReasonsOther('');
            setCommittedReasonsOther('');
            commitReasons(reasons, '');
        }
        // When checked, show the input but don't save until the user types + blurs.
    }

    function handleCommitOther() {
        if (reasonsOther === committedReasonsOther) {
            return;
        }
        setCommittedReasonsOther(reasonsOther);
        commitReasons(reasons, reasonsOther);
    }

    /**
     * Commit the linked-features list to the server. Always reads from
     * {@link linkedFeaturesRef} so synchronous change-then-commit flows (radio
     * onChange) see the post-change array even before React re-renders. Sends
     * the list as JSON; a single-shot replace is the natural shape for this
     * section.
     */
    async function commitLinkedFeatures() {
        // A row is sendable when both mutations are picked. The server
        // does a stricter validation (same-submission, no self-link) and
        // is the authoritative drop policy; this is just the client-side
        // backstop so we don't send obvious garbage.
        const wire = linkedFeaturesRef.current.map(rowToWire)
            .filter(w => w.mutationAId != null && w.mutationBId != null);
        const wireJSON = JSON.stringify(wire);
        if (wireJSON === committedLinkedFeaturesJSON) {
            return;
        }
        emit({status: 'saving', label: 'Linked Features'});
        try {
            await enqueueSave(async () => {
                const wasNew = !zdbIDRef.current;
                const url = '/action/zirc/line-submission/save-linked-features'
                    + (zdbIDRef.current ? `?zdbID=${encodeURIComponent(zdbIDRef.current)}` : '');
                const resp = await fetch(url, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: wireJSON,
                });
                if (!resp.ok) {
                    throw new Error(`HTTP ${resp.status}`);
                }
                const data = await resp.json() as LineSubmissionDTO;
                applyLinkedFeaturesFromDTO(data.linkedFeatures ?? []);
                captureNewIdIfAny(data, wasNew);
            });
            emit({status: 'saved', label: 'Linked Features'});
        } catch (e) {
            emit({status: 'error', label: 'Linked Features',
                message: e instanceof Error ? e.message : 'Save failed'});
        }
    }

    function handleAddLinkedFeature() {
        // Don't commit on add — empty rows are filtered out server-side anyway.
        setLinkedFeatures([...linkedFeaturesRef.current, emptyLinkedFeatureRow()]);
    }

    function handleRemoveLinkedFeature(rowId: string) {
        setLinkedFeatures(linkedFeaturesRef.current.filter(r => r.rowId !== rowId));
        commitLinkedFeatures();
    }

    function handleChangeLinkedFeature(rowId: string, patch: Partial<LinkedFeatureRow>) {
        setLinkedFeatures(linkedFeaturesRef.current.map(r => r.rowId === rowId ? {...r, ...patch} : r));
    }

    function handleCommitLinkedFeatureRow() {
        commitLinkedFeatures();
    }

    async function handleRemoveMutation(mutationId: number, label: string) {
        // eslint-disable-next-line no-alert
        if (!window.confirm(`Remove mutation "${label}"? This cannot be undone.`)) {
            return;
        }
        emit({status: 'saving', label: `Mutation ${label}`});
        try {
            await enqueueSave(async () => {
                const resp = await fetch(`/action/zirc/mutation/${mutationId}/delete`, {
                    method: 'POST',
                });
                if (!resp.ok) {
                    throw new Error(`HTTP ${resp.status}`);
                }
                const data = await resp.json() as LineSubmissionDTO;
                // The delete endpoint returns the parent submission DTO so we
                // can refresh the mutations list (and any other server-derived
                // state) without a separate GET.
                setMutations(data.mutations ?? []);
            });
            emit({status: 'saved', label: `Mutation ${label}`});
        } catch (e) {
            emit({status: 'error', label: `Mutation ${label}`,
                message: e instanceof Error ? e.message : 'Delete failed'});
        }
    }

    if (loadError) {
        return <div className='alert alert-danger'>Failed to load submission: {loadError}</div>;
    }
    if (!values) {
        return <div className='text-muted'>Loading…</div>;
    }

    const renderRow = (def: FieldDef<ScalarField>) => (
        <FieldRow
            key={def.field}
            def={def}
            value={values[def.field]}
            onChange={next => setFieldValue(def.field, next)}
            onCommit={next => commit(def, next)}
        />
    );

    return <>
        <Section id='overview' title='Overview'>
            <FieldsTable>
                <tr>
                    <th className='w-25'>ID</th>
                    <td>
                        {zdbID
                            ? <code>{zdbID}</code>
                            : <span className='text-muted small'>(assigned on first save)</span>}
                    </td>
                </tr>
                {OVERVIEW_FIELDS.map(renderRow)}
            </FieldsTable>
        </Section>
        <Section id='acceptance-reasons' title='Acceptance Reasons'>
            <ReasonsSection
                reasons={reasons}
                reasonsOther={reasonsOther}
                otherChecked={otherChecked}
                onToggleCanonical={handleToggleCanonical}
                onToggleOther={handleToggleOther}
                onChangeOther={setReasonsOther}
                onCommitOther={handleCommitOther}
            />
        </Section>
        <Section id='mutations' title='Mutations'>
            <MutationsSection
                submissionId={zdbID}
                mutations={mutations}
                onRemove={handleRemoveMutation}
            />
        </Section>
        <Section id='linked-features' title='Linked Features'>
            <LinkedFeaturesSection
                rows={linkedFeatures}
                mutations={mutations}
                onAdd={handleAddLinkedFeature}
                onRemove={handleRemoveLinkedFeature}
                onChange={handleChangeLinkedFeature}
                onCommitRow={handleCommitLinkedFeatureRow}
            />
        </Section>
        <Section id='background' title='Background'>
            <FieldsTable>
                {/* Order: single-allelic flag, maternal/paternal dropdowns, then the
                    background-changeable Yes/No and its concerns textarea. */}
                {renderRow(BACKGROUND_FIELDS[0])}
                {BACKGROUND_DROPDOWN_FIELDS.map(def => (
                    <DropdownWithOtherRow
                        key={def.field}
                        field={def.field}
                        label={def.label}
                        value={values[def.field]}
                        options={BACKGROUND_OPTIONS}
                        onChange={next => setFieldValue(def.field, next)}
                        onCommit={next => commit(def, next)}
                    />
                ))}
                {BACKGROUND_FIELDS.slice(1).map(renderRow)}
            </FieldsTable>
        </Section>
        <Section id='additional-info' title='Additional Info'>
            <FieldsTable>{ADDITIONAL_FIELDS.map(renderRow)}</FieldsTable>
        </Section>
        <SaveToast event={saveEvent}/>
    </>;
};

function emptyValues(): Record<string, string> {
    const out: Record<string, string> = {};
    ALL_FIELDS.forEach(d => { out[d.field] = ''; });
    return out;
}

export default LineSubmissionEdit;
