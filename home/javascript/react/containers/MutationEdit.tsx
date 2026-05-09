import React, {useEffect, useRef, useState} from 'react';
import SaveToast, {SaveEvent} from '../components/zirc/SaveToast';
import {FieldDef, FieldRow, FieldsTable, Section, valueToInputString} from '../components/zirc/FormPrimitives';

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
}

type ScalarField = Exclude<keyof MutationDTO, 'id' | 'lineSubmissionId' | 'sortOrder'>;

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

const MutationEdit = ({mutationId}: MutationEditProps) => {
    const [values, setValues] = useState<Record<string, string> | null>(null);
    const [committed, setCommitted] = useState<Record<string, string>>({});
    const [loadError, setLoadError] = useState<string>('');
    const [saveEvent, setSaveEvent] = useState<SaveEvent | null>(null);
    const seqRef = useRef<number>(0);
    const saveQueueRef = useRef<Promise<unknown>>(Promise.resolve());

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
        <Section id='lethality' title='Lethality'>
            <FieldsTable>{LETHALITY_FIELDS.map(renderRow)}</FieldsTable>
        </Section>
        <SaveToast event={saveEvent}/>
    </>;
};

export default MutationEdit;
