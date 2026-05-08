import React, {useEffect, useState} from 'react';

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
}

type ScalarField = Exclude<keyof LineSubmissionDTO, 'zdbID'>;
type FieldType = 'text' | 'textarea' | 'bool';
type SaveStatus = 'idle' | 'saving' | 'saved' | 'error';

interface LineSubmissionEditProps {
    submissionId: string;
}

interface FieldDef {
    field: ScalarField;
    label: string;
    type: FieldType;
}

const OVERVIEW_FIELDS: FieldDef[] = [
    {field: 'name',           label: 'Name',           type: 'text'},
    {field: 'abbreviation',   label: 'Abbreviation',   type: 'text'},
    {field: 'previousNames',  label: 'Previous Names', type: 'text'},
    {field: 'featuresLinked', label: 'Features Linked', type: 'bool'},
];

const BACKGROUND_FIELDS: FieldDef[] = [
    {field: 'maternalBackground',       label: 'Maternal',              type: 'text'},
    {field: 'paternalBackground',       label: 'Paternal',              type: 'text'},
    {field: 'backgroundChangeable',     label: 'Background Changeable', type: 'bool'},
    {field: 'backgroundChangeConcerns', label: 'Concerns',              type: 'textarea'},
];

const ADDITIONAL_FIELDS: FieldDef[] = [
    {field: 'unreportedFeaturesDetails', label: 'Unreported Features Details', type: 'textarea'},
    {field: 'additionalInfo',            label: 'Additional Info',             type: 'textarea'},
];

function valueToInputString(v: string | boolean | null | undefined): string {
    if (v === null || v === undefined) {
        return '';
    }
    if (typeof v === 'boolean') {
        return v ? 'true' : 'false';
    }
    return v;
}

interface SaveState {
    status: SaveStatus;
    error: string;
}

interface FieldRowProps {
    def: FieldDef;
    value: string;
    saveState: SaveState;
    onChange: (next: string) => void;
    onCommit: (next: string) => void;
}

const FieldRow = ({def, value, saveState, onChange, onCommit}: FieldRowProps) => {
    function renderInput() {
        if (def.type === 'textarea') {
            return (
                <textarea
                    className='form-control'
                    rows={3}
                    value={value}
                    onChange={e => onChange(e.target.value)}
                    onBlur={() => onCommit(value)}
                />
            );
        }
        if (def.type === 'bool') {
            const groupName = `field-${def.field}`;
            const options: Array<[string, string]> = [['true', 'Yes'], ['false', 'No'], ['', '—']];
            return (
                <div>
                    {options.map(([v, lbl]) => (
                        <div className='form-check form-check-inline' key={v}>
                            <input
                                type='radio'
                                className='form-check-input'
                                name={groupName}
                                value={v}
                                checked={value === v}
                                onChange={() => { onChange(v); onCommit(v); }}
                            />
                            <label className='form-check-label'>{lbl}</label>
                        </div>
                    ))}
                </div>
            );
        }
        return (
            <input
                type='text'
                className='form-control'
                value={value}
                onChange={e => onChange(e.target.value)}
                onBlur={() => onCommit(value)}
            />
        );
    }

    return (
        <tr>
            <th className='w-25'>{def.label}</th>
            <td>
                {renderInput()}
                <div className='small mt-1' style={{minHeight: '1.25em'}}>
                    {saveState.status === 'saving' && <span className='text-muted'>Saving…</span>}
                    {saveState.status === 'saved'  && <span className='text-success'>Saved</span>}
                    {saveState.status === 'error'  && <span className='text-danger'>Error: {saveState.error}</span>}
                </div>
            </td>
        </tr>
    );
};

interface SectionProps {
    id: string;
    title: string;
    children: React.ReactNode;
}

const Section = ({id, title, children}: SectionProps) => (
    <section className='section' id={id}>
        <div className='heading'>{title}</div>
        <table className='table table-borderless'>
            <tbody>{children}</tbody>
        </table>
    </section>
);

const LineSubmissionEdit = ({submissionId}: LineSubmissionEditProps) => {
    const [values, setValues] = useState<Record<string, string> | null>(null);
    const [committed, setCommitted] = useState<Record<string, string>>({});
    const [saveStates, setSaveStates] = useState<Record<string, SaveState>>({});
    const [loadError, setLoadError] = useState<string>('');

    useEffect(() => {
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
                const initial: Record<string, string> = {};
                ([...OVERVIEW_FIELDS, ...BACKGROUND_FIELDS, ...ADDITIONAL_FIELDS]).forEach(d => {
                    initial[d.field] = valueToInputString(data[d.field]);
                });
                setValues({...initial, zdbID: data.zdbID});
                setCommitted(initial);
            })
            .catch(e => {
                if (!cancelled) {
                    setLoadError(e instanceof Error ? e.message : 'Load failed');
                }
            });
        return () => { cancelled = true; };
    }, [submissionId]);

    function setFieldValue(field: ScalarField, next: string) {
        setValues(prev => prev ? {...prev, [field]: next} : prev);
    }

    function setSaveState(field: ScalarField, state: SaveState) {
        setSaveStates(prev => ({...prev, [field]: state}));
    }

    async function commit(field: ScalarField, next: string) {
        if (next === committed[field]) {
            return;
        }
        setSaveState(field, {status: 'saving', error: ''});
        try {
            const body = new URLSearchParams();
            body.append('field', field);
            body.append('value', next);
            const resp = await fetch(`/action/zirc/line-submission/${submissionId}/update-field`, {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: body.toString(),
            });
            if (!resp.ok) {
                throw new Error(`HTTP ${resp.status}`);
            }
            const data = await resp.json();
            const serverValue = data.value == null ? '' : String(data.value);
            setCommitted(prev => ({...prev, [field]: serverValue}));
            setFieldValue(field, serverValue);
            setSaveState(field, {status: 'saved', error: ''});
        } catch (e) {
            setSaveState(field, {status: 'error', error: e instanceof Error ? e.message : 'Save failed'});
        }
    }

    if (loadError) {
        return <div className='alert alert-danger'>Failed to load submission: {loadError}</div>;
    }
    if (!values) {
        return <div className='text-muted'>Loading…</div>;
    }

    const renderRow = (def: FieldDef) => (
        <FieldRow
            key={def.field}
            def={def}
            value={values[def.field]}
            saveState={saveStates[def.field] || {status: 'idle', error: ''}}
            onChange={next => setFieldValue(def.field, next)}
            onCommit={next => commit(def.field, next)}
        />
    );

    return <>
        <Section id='overview' title='Overview'>
            <tr>
                <th className='w-25'>ID</th>
                <td><code>{values.zdbID}</code></td>
            </tr>
            {OVERVIEW_FIELDS.map(renderRow)}
        </Section>
        <Section id='background' title='Background'>
            {BACKGROUND_FIELDS.map(renderRow)}
        </Section>
        <Section id='additional-info' title='Additional Info'>
            {ADDITIONAL_FIELDS.map(renderRow)}
        </Section>
    </>;
};

export default LineSubmissionEdit;
