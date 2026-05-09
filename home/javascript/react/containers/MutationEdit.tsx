import React, {useEffect, useRef, useState} from 'react';
import SaveToast, {SaveEvent} from '../components/zirc/SaveToast';
import {FieldDef, FieldRow, FieldsTable, Section, valueToInputString} from '../components/zirc/FormPrimitives';

interface GeneWire {
    id: number | null;
    sortOrder: number | null;
    mutatedGeneZdbId: string | null;
    mutatedGeneAbbreviation: string | null;
    linkageGroup: string | null;
    genbankGenomicDna: string | null;
    genbankCdna: string | null;
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
}

type ScalarField = Exclude<keyof MutationDTO,
    'id' | 'lineSubmissionId' | 'sortOrder' | 'genes'>;

interface MutationEditProps {
    mutationId: string;
}

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

interface GeneRow {
    /** Local-only React key. Stable so input focus survives re-renders. */
    rowId: string;
    /** Persistent server id; null for rows the user just added. */
    id: number | null;
    mutatedGeneZdbId: string;
    /** Read-only display echo from the server's last response. */
    mutatedGeneAbbreviation: string;
    linkageGroup: string;
    genbankGenomicDna: string;
    genbankCdna: string;
}

let nextGeneRowId = 1;
function freshGeneRowId(): string {
    return `gene-${nextGeneRowId++}`;
}

function emptyGeneRow(): GeneRow {
    return {
        rowId: freshGeneRowId(),
        id: null,
        mutatedGeneZdbId: '',
        mutatedGeneAbbreviation: '',
        linkageGroup: '',
        genbankGenomicDna: '',
        genbankCdna: '',
    };
}

function wireToGeneRow(w: GeneWire): GeneRow {
    return {
        rowId: freshGeneRowId(),
        id: w.id,
        mutatedGeneZdbId: w.mutatedGeneZdbId ?? '',
        mutatedGeneAbbreviation: w.mutatedGeneAbbreviation ?? '',
        linkageGroup: w.linkageGroup ?? '',
        genbankGenomicDna: w.genbankGenomicDna ?? '',
        genbankCdna: w.genbankCdna ?? '',
    };
}

function geneRowToWire(r: GeneRow): GeneWire {
    return {
        id: r.id,
        sortOrder: null, // server assigns based on list order
        mutatedGeneZdbId: r.mutatedGeneZdbId.trim() || null,
        mutatedGeneAbbreviation: null, // server-supplied; client doesn't echo back
        linkageGroup: r.linkageGroup.trim() || null,
        genbankGenomicDna: r.genbankGenomicDna.trim() || null,
        genbankCdna: r.genbankCdna.trim() || null,
    };
}

interface GenesSectionProps {
    rows: GeneRow[];
    onAdd: () => void;
    onRemove: (rowId: string) => void;
    onChange: (rowId: string, patch: Partial<GeneRow>) => void;
    onCommitRow: () => void;
}

const GenesSection = ({rows, onAdd, onRemove, onChange, onCommitRow}: GenesSectionProps) => {
    if (rows.length === 0) {
        return (
            <div>
                <p className='text-muted'>No genes recorded for this mutation.</p>
                <button type='button' className='btn btn-sm btn-outline-secondary' onClick={onAdd}>
                    + Add gene
                </button>
            </div>
        );
    }
    return (
        <div>
            {rows.map((row, idx) => {
                const zdbId = `gene-zdb-${row.rowId}`;
                const lgId = `gene-lg-${row.rowId}`;
                const gdnaId = `gene-gdna-${row.rowId}`;
                const cdnaId = `gene-cdna-${row.rowId}`;
                return (
                    <fieldset key={row.rowId} className='border rounded p-3 mb-3'>
                        <legend className='h6 px-2' style={{width: 'auto'}}>
                            Gene {idx + 1}
                        </legend>
                        <div className='form-group row'>
                            <label htmlFor={zdbId} className='col-sm-3 col-form-label'>Mutated Gene ZDB ID</label>
                            <div className='col-sm-9'>
                                <input
                                    type='text'
                                    id={zdbId}
                                    className='form-control'
                                    placeholder='ZDB-GENE-…'
                                    value={row.mutatedGeneZdbId}
                                    onChange={e => onChange(row.rowId, {mutatedGeneZdbId: e.target.value})}
                                    onBlur={() => onCommitRow()}
                                />
                                {row.mutatedGeneAbbreviation && (
                                    <small className='form-text text-muted'>
                                        Resolved: <em>{row.mutatedGeneAbbreviation}</em>
                                    </small>
                                )}
                            </div>
                        </div>
                        <div className='form-group row'>
                            <label htmlFor={lgId} className='col-sm-3 col-form-label'>Linkage Group</label>
                            <div className='col-sm-9'>
                                <input
                                    type='text'
                                    id={lgId}
                                    className='form-control'
                                    value={row.linkageGroup}
                                    onChange={e => onChange(row.rowId, {linkageGroup: e.target.value})}
                                    onBlur={() => onCommitRow()}
                                />
                            </div>
                        </div>
                        <div className='form-group row'>
                            <label htmlFor={gdnaId} className='col-sm-3 col-form-label'>GenBank gDNA</label>
                            <div className='col-sm-9'>
                                <input
                                    type='text'
                                    id={gdnaId}
                                    className='form-control'
                                    value={row.genbankGenomicDna}
                                    onChange={e => onChange(row.rowId, {genbankGenomicDna: e.target.value})}
                                    onBlur={() => onCommitRow()}
                                />
                            </div>
                        </div>
                        <div className='form-group row mb-0'>
                            <label htmlFor={cdnaId} className='col-sm-3 col-form-label'>GenBank cDNA</label>
                            <div className='col-sm-9'>
                                <input
                                    type='text'
                                    id={cdnaId}
                                    className='form-control'
                                    value={row.genbankCdna}
                                    onChange={e => onChange(row.rowId, {genbankCdna: e.target.value})}
                                    onBlur={() => onCommitRow()}
                                />
                            </div>
                        </div>
                        <div className='text-right mt-2'>
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
            <button type='button' className='btn btn-sm btn-outline-secondary' onClick={onAdd}>
                + Add gene
            </button>
        </div>
    );
};

const MutationEdit = ({mutationId}: MutationEditProps) => {
    const [values, setValues] = useState<Record<string, string> | null>(null);
    const [committed, setCommitted] = useState<Record<string, string>>({});
    const [genes, setGenesState] = useState<GeneRow[]>([]);
    const [committedGenesJSON, setCommittedGenesJSON] = useState<string>('[]');
    const [loadError, setLoadError] = useState<string>('');
    const [saveEvent, setSaveEvent] = useState<SaveEvent | null>(null);
    const seqRef = useRef<number>(0);
    const saveQueueRef = useRef<Promise<unknown>>(Promise.resolve());
    const genesRef = useRef<GeneRow[]>([]);

    function setGenes(next: GeneRow[]) {
        genesRef.current = next;
        setGenesState(next);
    }

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
        applyGenesFromDTO(data.genes ?? []);
    }

    function applyGenesFromDTO(wire: GeneWire[]) {
        setGenes(wire.map(wireToGeneRow));
        // Snapshot just the wire-format payload (without local rowIds) so
        // commitGenes can dedupe no-ops by exact JSON equality.
        setCommittedGenesJSON(JSON.stringify(wire.map(w => ({
            id: w.id,
            sortOrder: null,
            mutatedGeneZdbId: w.mutatedGeneZdbId ?? null,
            mutatedGeneAbbreviation: null,
            linkageGroup: w.linkageGroup ?? null,
            genbankGenomicDna: w.genbankGenomicDna ?? null,
            genbankCdna: w.genbankCdna ?? null,
        }))));
    }

    function setFieldValue(field: ScalarField, next: string) {
        setValues(prev => prev ? {...prev, [field]: next} : prev);
    }

    function emit(event: Omit<SaveEvent, 'seq'>) {
        seqRef.current += 1;
        setSaveEvent({...event, seq: seqRef.current});
    }

    function enqueueSave<T>(fn: () => Promise<T>): Promise<T> {
        const next = saveQueueRef.current.then(fn, fn);
        saveQueueRef.current = next.catch(() => undefined);
        return next;
    }

    async function commit(field: FieldDef<ScalarField>, next: string) {
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
                // Update only the saved field from the server's normalized
                // value — see LineSubmissionEdit for the rationale.
                const normalized = valueToInputString(data[field.field]);
                setValues(prev => prev ? {...prev, [field.field]: normalized} : prev);
                setCommitted(prev => ({...prev, [field.field]: normalized}));
            });
            emit({status: 'saved', label: field.label});
        } catch (e) {
            emit({status: 'error', label: field.label, message: e instanceof Error ? e.message : 'Save failed'});
        }
    }

    /**
     * Replace-all save for genes. Reads from genesRef so synchronous
     * change-then-commit handlers (none today, but consistent with
     * LinkedFeatures) see the latest array.
     */
    async function commitGenes() {
        const wire = genesRef.current.map(geneRowToWire);
        const wireJSON = JSON.stringify(wire);
        if (wireJSON === committedGenesJSON) {
            return;
        }
        emit({status: 'saving', label: 'Genes'});
        try {
            await enqueueSave(async () => {
                const resp = await fetch(`/action/zirc/mutation/${mutationId}/save-genes`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: wireJSON,
                });
                if (!resp.ok) {
                    // Try to surface the server's error message (e.g. "Marker not found: …")
                    let detail = `HTTP ${resp.status}`;
                    try {
                        const err = await resp.json();
                        if (err && err.detail) {
                            detail = err.detail;
                        }
                    } catch { /* ignore */ }
                    throw new Error(detail);
                }
                const data = await resp.json() as MutationDTO;
                applyGenesFromDTO(data.genes ?? []);
            });
            emit({status: 'saved', label: 'Genes'});
        } catch (e) {
            emit({status: 'error', label: 'Genes',
                message: e instanceof Error ? e.message : 'Save failed'});
        }
    }

    function handleAddGene() {
        // Don't commit on add — empty rows would either fail validation or
        // be saved as empty; let the user fill in fields and blur first.
        setGenes([...genesRef.current, emptyGeneRow()]);
    }

    function handleRemoveGene(rowId: string) {
        setGenes(genesRef.current.filter(r => r.rowId !== rowId));
        commitGenes();
    }

    function handleChangeGene(rowId: string, patch: Partial<GeneRow>) {
        setGenes(genesRef.current.map(r => r.rowId === rowId ? {...r, ...patch} : r));
    }

    function handleCommitGeneRow() {
        commitGenes();
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
            onCommit={next => commit(def, next)}
        />
    );

    return <>
        <Section id='general' title='General'>
            <FieldsTable>{GENERAL_FIELDS.map(renderRow)}</FieldsTable>
        </Section>
        <Section id='genes' title='Genes'>
            <GenesSection
                rows={genes}
                onAdd={handleAddGene}
                onRemove={handleRemoveGene}
                onChange={handleChangeGene}
                onCommitRow={handleCommitGeneRow}
            />
        </Section>
        <Section id='lethality' title='Lethality'>
            <FieldsTable>{LETHALITY_FIELDS.map(renderRow)}</FieldsTable>
        </Section>
        <SaveToast event={saveEvent}/>
    </>;
};

export default MutationEdit;
