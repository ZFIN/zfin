import React, {useEffect, useRef, useState} from 'react';
import SaveToast, {SaveEvent} from '../components/zirc/SaveToast';
import {FieldDef, FieldRow, FieldsTable, Section, valueToInputString} from '../components/zirc/FormPrimitives';

// ─── Wire types ────────────────────────────────────────────────────────────

interface GeneWire {
    id: number | null;
    sortOrder: number | null;
    mutatedGeneZdbId: string | null;
    mutatedGeneAbbreviation: string | null;
    linkageGroup: string | null;
    genbankGenomicDna: string | null;
    genbankCdna: string | null;
}

interface LesionWire {
    id: number | null;
    sortOrder: number | null;
    lesionType: string | null;
    indexDeletionPos: number | null;
    indexInsertionSize: number | null;
    deletedBasePairs: string | null;
    insertedBasePairs: string | null;
    wtGenomicSequence: string | null;
    mutatedAminoAcids: string | null;
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
    restrictionEnzyme: string | null;
    enzymeCleaves: string | null;
    expectedWtDigest: string | null;
    expectedMutDigest: string | null;
    additionalInfo: string | null;
}

interface PhenotypeWire {
    id: number | null;
    sortOrder: number | null;
    description: string | null;
    hoursPostFertilization: number | null;
    stage: string | null;
    zfinImagePermission: boolean | null;
    nonMendelianPercentage: number | null;
    segregation: string[] | null;
    type: string[] | null;
}

interface MutationDTO {
    id: number;
    lineSubmissionId: string;
    sortOrder: number | null;
    alleleDesignation: string | null;
    mutagenesisProtocol: string | null;
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
}

type ScalarField = Exclude<keyof MutationDTO,
    'id' | 'lineSubmissionId' | 'sortOrder'
    | 'genes' | 'lesions' | 'genotypingAssays' | 'phenotypes'>;

interface MutationEditProps {
    mutationId: string;
}

// ─── Scalar field defs ─────────────────────────────────────────────────────

const GENERAL_FIELDS: FieldDef<ScalarField>[] = [
    {field: 'alleleDesignation',        label: 'Allele Designation',     type: 'text',     idPrefix: 'mut'},
    {field: 'mutagenesisProtocol',      label: 'Mutagenesis Protocol',   type: 'text',     idPrefix: 'mut'},
    {field: 'molecularlyCharacterized', label: 'Molecularly Characterized', type: 'bool',  idPrefix: 'mut'},
    {field: 'mutationType',             label: 'Mutation Type',          type: 'text',     idPrefix: 'mut'},
    {field: 'zfinRecordEstablished',    label: 'ZFIN Record Established', type: 'bool',    idPrefix: 'mut'},
    {field: 'cellGenomicFeature',       label: 'Cell Genomic Feature',   type: 'text',     idPrefix: 'mut'},
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
    }),
    wireToRow: w => ({
        rowId: freshRowId('gene'),
        id: w.id,
        mutatedGeneZdbId: w.mutatedGeneZdbId ?? '',
        mutatedGeneAbbreviation: w.mutatedGeneAbbreviation ?? '',
        linkageGroup: w.linkageGroup ?? '',
        genbankGenomicDna: w.genbankGenomicDna ?? '',
        genbankCdna: w.genbankCdna ?? '',
    }),
    rowToWire: r => ({
        id: r.id,
        sortOrder: null,
        mutatedGeneZdbId: trimOrNull(r.mutatedGeneZdbId),
        mutatedGeneAbbreviation: null, // server-supplied; client doesn't echo back
        linkageGroup: trimOrNull(r.linkageGroup),
        genbankGenomicDna: trimOrNull(r.genbankGenomicDna),
        genbankCdna: trimOrNull(r.genbankCdna),
    }),
};

interface LesionRow {
    rowId: string;
    id: number | null;
    lesionType: string;
    indexDeletionPos: string;
    indexInsertionSize: string;
    deletedBasePairs: string;
    insertedBasePairs: string;
    wtGenomicSequence: string;
    mutatedAminoAcids: string;
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
        indexDeletionPos: '',
        indexInsertionSize: '',
        deletedBasePairs: '',
        insertedBasePairs: '',
        wtGenomicSequence: '',
        mutatedAminoAcids: '',
        additionalInfo: '',
    }),
    wireToRow: w => ({
        rowId: freshRowId('lesion'),
        id: w.id,
        lesionType: w.lesionType ?? '',
        indexDeletionPos: w.indexDeletionPos == null ? '' : String(w.indexDeletionPos),
        indexInsertionSize: w.indexInsertionSize == null ? '' : String(w.indexInsertionSize),
        deletedBasePairs: w.deletedBasePairs ?? '',
        insertedBasePairs: w.insertedBasePairs ?? '',
        wtGenomicSequence: w.wtGenomicSequence ?? '',
        mutatedAminoAcids: w.mutatedAminoAcids ?? '',
        additionalInfo: w.additionalInfo ?? '',
    }),
    rowToWire: r => ({
        id: r.id,
        sortOrder: null,
        lesionType: trimOrNull(r.lesionType),
        indexDeletionPos: parseIntOrNull(r.indexDeletionPos),
        indexInsertionSize: parseIntOrNull(r.indexInsertionSize),
        deletedBasePairs: trimOrNull(r.deletedBasePairs),
        insertedBasePairs: trimOrNull(r.insertedBasePairs),
        wtGenomicSequence: trimOrNull(r.wtGenomicSequence),
        mutatedAminoAcids: trimOrNull(r.mutatedAminoAcids),
        additionalInfo: trimOrNull(r.additionalInfo),
    }),
};

interface GenotypingAssayRow {
    rowId: string;
    id: number | null;
    assayType: string;
    forwardPrimer: string;
    reversePrimer: string;
    expectedWtPcr: string;
    expectedMutPcr: string;
    restrictionEnzyme: string;
    enzymeCleaves: string;
    expectedWtDigest: string;
    expectedMutDigest: string;
    additionalInfo: string;
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
        restrictionEnzyme: '',
        enzymeCleaves: '',
        expectedWtDigest: '',
        expectedMutDigest: '',
        additionalInfo: '',
    }),
    wireToRow: w => ({
        rowId: freshRowId('assay'),
        id: w.id,
        assayType: w.assayType ?? '',
        forwardPrimer: w.forwardPrimer ?? '',
        reversePrimer: w.reversePrimer ?? '',
        expectedWtPcr: w.expectedWtPcr ?? '',
        expectedMutPcr: w.expectedMutPcr ?? '',
        restrictionEnzyme: w.restrictionEnzyme ?? '',
        enzymeCleaves: w.enzymeCleaves ?? '',
        expectedWtDigest: w.expectedWtDigest ?? '',
        expectedMutDigest: w.expectedMutDigest ?? '',
        additionalInfo: w.additionalInfo ?? '',
    }),
    rowToWire: r => ({
        id: r.id,
        sortOrder: null,
        assayType: trimOrNull(r.assayType),
        forwardPrimer: trimOrNull(r.forwardPrimer),
        reversePrimer: trimOrNull(r.reversePrimer),
        expectedWtPcr: trimOrNull(r.expectedWtPcr),
        expectedMutPcr: trimOrNull(r.expectedMutPcr),
        restrictionEnzyme: trimOrNull(r.restrictionEnzyme),
        enzymeCleaves: trimOrNull(r.enzymeCleaves),
        expectedWtDigest: trimOrNull(r.expectedWtDigest),
        expectedMutDigest: trimOrNull(r.expectedMutDigest),
        additionalInfo: trimOrNull(r.additionalInfo),
    }),
};

interface PhenotypeRow {
    rowId: string;
    id: number | null;
    description: string;
    hoursPostFertilization: string;
    stage: string;
    /** '' / 'true' / 'false' — same as bool radios elsewhere. */
    zfinImagePermission: '' | 'true' | 'false';
    nonMendelianPercentage: string;
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
        hoursPostFertilization: '',
        stage: '',
        zfinImagePermission: '',
        nonMendelianPercentage: '',
        segregationCsv: '',
        typeCsv: '',
    }),
    wireToRow: w => ({
        rowId: freshRowId('phen'),
        id: w.id,
        description: w.description ?? '',
        hoursPostFertilization: w.hoursPostFertilization == null ? '' : String(w.hoursPostFertilization),
        stage: w.stage ?? '',
        zfinImagePermission: w.zfinImagePermission === true ? 'true'
            : w.zfinImagePermission === false ? 'false'
                : '',
        nonMendelianPercentage: w.nonMendelianPercentage == null ? '' : String(w.nonMendelianPercentage),
        segregationCsv: arrayToCsv(w.segregation),
        typeCsv: arrayToCsv(w.type),
    }),
    rowToWire: r => ({
        id: r.id,
        sortOrder: null,
        description: trimOrNull(r.description),
        hoursPostFertilization: parseIntOrNull(r.hoursPostFertilization),
        stage: trimOrNull(r.stage),
        zfinImagePermission: r.zfinImagePermission === 'true' ? true
            : r.zfinImagePermission === 'false' ? false
                : null,
        nonMendelianPercentage: parseFloatOrNull(r.nonMendelianPercentage),
        segregation: csvToArray(r.segregationCsv),
        type: csvToArray(r.typeCsv),
    }),
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
}

interface ChildCollection<R extends {rowId: string}> {
    rows: R[];
    apply: (dto: MutationDTO) => void;
    add: () => void;
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
        const next = wire.map(adapter.wireToRow);
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

    function remove(rowId: string) {
        setRows(rowsRef.current.filter(r => r.rowId !== rowId));
        commit();
    }

    function change(rowId: string, patch: Partial<R>) {
        setRows(rowsRef.current.map(r => r.rowId === rowId ? {...r, ...patch} : r));
    }

    return {rows, apply, add, remove, change, commit};
}

// ─── Per-row UI helpers ────────────────────────────────────────────────────

interface RowFieldsetProps {
    title: string;
    onRemove: () => void;
    children: React.ReactNode;
}

const RowFieldset = ({title, onRemove, children}: RowFieldsetProps) => (
    <fieldset className='border rounded p-3 mb-3'>
        <legend className='h6 px-2' style={{width: 'auto'}}>{title}</legend>
        {children}
        <div className='text-right mt-2'>
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

interface TextRowFieldProps {
    id: string;
    label: string;
    value: string;
    type?: 'text' | 'number';
    placeholder?: string;
    onChange: (next: string) => void;
    onCommit: () => void;
}

const TextRowField = ({id, label, value, type = 'text', placeholder, onChange, onCommit}: TextRowFieldProps) => (
    <div className='form-group row'>
        <label htmlFor={id} className='col-sm-3 col-form-label'>{label}</label>
        <div className='col-sm-9'>
            <input
                type={type}
                id={id}
                className='form-control'
                placeholder={placeholder}
                value={value}
                onChange={e => onChange(e.target.value)}
                onBlur={onCommit}
                step={type === 'number' ? 'any' : undefined}
            />
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

interface AddButtonProps {
    label: string;
    onAdd: () => void;
}

const AddButton = ({label, onAdd}: AddButtonProps) => (
    <button type='button' className='btn btn-sm btn-outline-secondary' onClick={onAdd}>
        + Add {label}
    </button>
);

// ─── Per-collection section components ────────────────────────────────────

interface SectionListProps<R> {
    rows: R[];
    onAdd: () => void;
    onRemove: (rowId: string) => void;
    onChange: (rowId: string, patch: Partial<R>) => void;
    onCommit: () => void;
}

const GenesSection = ({rows, onAdd, onRemove, onChange, onCommit}: SectionListProps<GeneRow>) => {
    if (rows.length === 0) {
        return <div><p className='text-muted'>No genes recorded for this mutation.</p><AddButton label='gene' onAdd={onAdd}/></div>;
    }
    return (
        <div>
            {rows.map((row, idx) => (
                <RowFieldset key={row.rowId} title={`Gene ${idx + 1}`} onRemove={() => onRemove(row.rowId)}>
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
                    <TextRowField id={`gene-lg-${row.rowId}`}    label='Linkage Group' value={row.linkageGroup}      onChange={v => onChange(row.rowId, {linkageGroup: v})}      onCommit={onCommit}/>
                    <TextRowField id={`gene-gdna-${row.rowId}`}  label='GenBank gDNA'  value={row.genbankGenomicDna} onChange={v => onChange(row.rowId, {genbankGenomicDna: v})} onCommit={onCommit}/>
                    <TextAreaRowField id={`gene-cdna-${row.rowId}`} label='GenBank cDNA' value={row.genbankCdna} onChange={v => onChange(row.rowId, {genbankCdna: v})} onCommit={onCommit}/>
                </RowFieldset>
            ))}
            <AddButton label='gene' onAdd={onAdd}/>
        </div>
    );
};

const LesionsSection = ({rows, onAdd, onRemove, onChange, onCommit}: SectionListProps<LesionRow>) => {
    if (rows.length === 0) {
        return <div><p className='text-muted'>No lesions recorded for this mutation.</p><AddButton label='lesion' onAdd={onAdd}/></div>;
    }
    return (
        <div>
            {rows.map((row, idx) => (
                <RowFieldset key={row.rowId} title={`Lesion ${idx + 1}`} onRemove={() => onRemove(row.rowId)}>
                    <TextRowField id={`les-type-${row.rowId}`}   label='Type'                  value={row.lesionType}         onChange={v => onChange(row.rowId, {lesionType: v})}         onCommit={onCommit} placeholder='deletion, insertion, indel, …'/>
                    <TextRowField id={`les-delpos-${row.rowId}`} label='Index Deletion Pos'    value={row.indexDeletionPos}   onChange={v => onChange(row.rowId, {indexDeletionPos: v})}   onCommit={onCommit} type='number'/>
                    <TextRowField id={`les-inssz-${row.rowId}`}  label='Index Insertion Size'  value={row.indexInsertionSize} onChange={v => onChange(row.rowId, {indexInsertionSize: v})} onCommit={onCommit} type='number'/>
                    <TextRowField id={`les-delbp-${row.rowId}`}  label='Deleted Base Pairs'    value={row.deletedBasePairs}   onChange={v => onChange(row.rowId, {deletedBasePairs: v})}   onCommit={onCommit}/>
                    <TextRowField id={`les-insbp-${row.rowId}`}  label='Inserted Base Pairs'   value={row.insertedBasePairs}  onChange={v => onChange(row.rowId, {insertedBasePairs: v})}  onCommit={onCommit}/>
                    <TextRowField id={`les-wtgs-${row.rowId}`}   label='WT Genomic Sequence'   value={row.wtGenomicSequence}  onChange={v => onChange(row.rowId, {wtGenomicSequence: v})}  onCommit={onCommit}/>
                    <TextRowField id={`les-aa-${row.rowId}`}     label='Mutated Amino Acids'   value={row.mutatedAminoAcids}  onChange={v => onChange(row.rowId, {mutatedAminoAcids: v})}  onCommit={onCommit}/>
                    <TextAreaRowField id={`les-info-${row.rowId}`} label='Additional Info'      value={row.additionalInfo}     onChange={v => onChange(row.rowId, {additionalInfo: v})}     onCommit={onCommit}/>
                </RowFieldset>
            ))}
            <AddButton label='lesion' onAdd={onAdd}/>
        </div>
    );
};

const GenotypingAssaysSection = ({rows, onAdd, onRemove, onChange, onCommit}: SectionListProps<GenotypingAssayRow>) => {
    if (rows.length === 0) {
        return <div><p className='text-muted'>No genotyping assays recorded for this mutation.</p><AddButton label='genotyping assay' onAdd={onAdd}/></div>;
    }
    return (
        <div>
            {rows.map((row, idx) => (
                <RowFieldset key={row.rowId} title={`Genotyping Assay ${idx + 1}`} onRemove={() => onRemove(row.rowId)}>
                    <TextRowField id={`asy-type-${row.rowId}`}    label='Assay Type'         value={row.assayType}          onChange={v => onChange(row.rowId, {assayType: v})}          onCommit={onCommit} placeholder='pcr_gel, rflp, kasp, …'/>
                    <TextRowField id={`asy-fwd-${row.rowId}`}     label='Forward Primer'     value={row.forwardPrimer}      onChange={v => onChange(row.rowId, {forwardPrimer: v})}      onCommit={onCommit}/>
                    <TextRowField id={`asy-rev-${row.rowId}`}     label='Reverse Primer'     value={row.reversePrimer}      onChange={v => onChange(row.rowId, {reversePrimer: v})}      onCommit={onCommit}/>
                    <TextRowField id={`asy-wtpcr-${row.rowId}`}   label='Expected WT PCR'    value={row.expectedWtPcr}      onChange={v => onChange(row.rowId, {expectedWtPcr: v})}      onCommit={onCommit}/>
                    <TextRowField id={`asy-mutpcr-${row.rowId}`}  label='Expected Mut PCR'   value={row.expectedMutPcr}     onChange={v => onChange(row.rowId, {expectedMutPcr: v})}     onCommit={onCommit}/>
                    <TextRowField id={`asy-renz-${row.rowId}`}    label='Restriction Enzyme' value={row.restrictionEnzyme}  onChange={v => onChange(row.rowId, {restrictionEnzyme: v})}  onCommit={onCommit}/>
                    <TextRowField id={`asy-cleav-${row.rowId}`}   label='Enzyme Cleaves'     value={row.enzymeCleaves}      onChange={v => onChange(row.rowId, {enzymeCleaves: v})}      onCommit={onCommit}/>
                    <TextRowField id={`asy-wtdig-${row.rowId}`}   label='Expected WT Digest' value={row.expectedWtDigest}   onChange={v => onChange(row.rowId, {expectedWtDigest: v})}   onCommit={onCommit}/>
                    <TextRowField id={`asy-mutdig-${row.rowId}`}  label='Expected Mut Digest' value={row.expectedMutDigest} onChange={v => onChange(row.rowId, {expectedMutDigest: v})}  onCommit={onCommit}/>
                    <TextAreaRowField id={`asy-info-${row.rowId}`} label='Additional Info'    value={row.additionalInfo}     onChange={v => onChange(row.rowId, {additionalInfo: v})}     onCommit={onCommit}/>
                </RowFieldset>
            ))}
            <AddButton label='genotyping assay' onAdd={onAdd}/>
        </div>
    );
};

const PhenotypesSection = ({rows, onAdd, onRemove, onChange, onCommit}: SectionListProps<PhenotypeRow>) => {
    if (rows.length === 0) {
        return <div><p className='text-muted'>No phenotypes recorded for this mutation.</p><AddButton label='phenotype' onAdd={onAdd}/></div>;
    }
    return (
        <div>
            {rows.map((row, idx) => (
                <RowFieldset key={row.rowId} title={`Phenotype ${idx + 1}`} onRemove={() => onRemove(row.rowId)}>
                    <TextRowField id={`phn-desc-${row.rowId}`}     label='Description'              value={row.description}             onChange={v => onChange(row.rowId, {description: v})}             onCommit={onCommit}/>
                    <TextRowField id={`phn-hpf-${row.rowId}`}      label='Hours Post Fertilization' value={row.hoursPostFertilization}  onChange={v => onChange(row.rowId, {hoursPostFertilization: v})}  onCommit={onCommit} type='number'/>
                    <TextRowField id={`phn-stage-${row.rowId}`}    label='Stage'                    value={row.stage}                   onChange={v => onChange(row.rowId, {stage: v})}                   onCommit={onCommit}/>
                    <BoolRowField
                        groupName={`phn-imgperm-${row.rowId}`}
                        label='ZFIN Image Permission'
                        value={row.zfinImagePermission}
                        onChange={v => onChange(row.rowId, {zfinImagePermission: v})}
                        onCommit={onCommit}
                    />
                    <TextRowField id={`phn-pct-${row.rowId}`}      label='Non-Mendelian %'          value={row.nonMendelianPercentage}  onChange={v => onChange(row.rowId, {nonMendelianPercentage: v})}  onCommit={onCommit} type='number'/>
                    <TextRowField id={`phn-seg-${row.rowId}`}      label='Segregation (CSV)'        value={row.segregationCsv}          onChange={v => onChange(row.rowId, {segregationCsv: v})}          onCommit={onCommit} placeholder='mendelian_recessive, …'/>
                    <TextAreaRowField id={`phn-type-${row.rowId}`} label='Type (CSV)'               value={row.typeCsv}                 onChange={v => onChange(row.rowId, {typeCsv: v})}                 onCommit={onCommit}/>
                </RowFieldset>
            ))}
            <AddButton label='phenotype' onAdd={onAdd}/>
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
    const genes      = useChildCollection(GENE_ADAPTER,      childDeps);
    const lesions    = useChildCollection(LESION_ADAPTER,    childDeps);
    const assays     = useChildCollection(ASSAY_ADAPTER,     childDeps);
    const phenotypes = useChildCollection(PHENOTYPE_ADAPTER, childDeps);

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
            key={def.field}
            def={def}
            value={values[def.field]}
            onChange={next => setFieldValue(def.field, next)}
            onCommit={next => commitScalarField(def, next)}
        />
    );

    return <>
        <Section id='general' title='General'>
            <FieldsTable>{GENERAL_FIELDS.map(renderRow)}</FieldsTable>
        </Section>
        <Section id='genes' title='Genes'>
            <GenesSection rows={genes.rows} onAdd={genes.add} onRemove={genes.remove} onChange={genes.change} onCommit={genes.commit}/>
        </Section>
        <Section id='lesions' title='Lesions'>
            <LesionsSection rows={lesions.rows} onAdd={lesions.add} onRemove={lesions.remove} onChange={lesions.change} onCommit={lesions.commit}/>
        </Section>
        <Section id='genotyping-assays' title='Genotyping Assays'>
            <GenotypingAssaysSection rows={assays.rows} onAdd={assays.add} onRemove={assays.remove} onChange={assays.change} onCommit={assays.commit}/>
        </Section>
        <Section id='phenotypes' title='Phenotypes'>
            <PhenotypesSection rows={phenotypes.rows} onAdd={phenotypes.add} onRemove={phenotypes.remove} onChange={phenotypes.change} onCommit={phenotypes.commit}/>
        </Section>
        <Section id='lethality' title='Lethality'>
            <FieldsTable>{LETHALITY_FIELDS.map(renderRow)}</FieldsTable>
        </Section>
        <SaveToast event={saveEvent}/>
    </>;
};

export default MutationEdit;
