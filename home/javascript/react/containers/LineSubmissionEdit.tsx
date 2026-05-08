import React, {useEffect, useState} from 'react';
import SaveToast, {SaveEvent} from '../components/zirc/SaveToast';

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
    isDraft?: boolean | null;
}

type ScalarField = Exclude<keyof LineSubmissionDTO, 'zdbID' | 'isDraft'>;
type FieldType = 'text' | 'textarea' | 'bool';

interface LineSubmissionEditProps {
    /** Empty string for the /new flow — the first save creates the row. */
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

const ALL_FIELDS: FieldDef[] = [...OVERVIEW_FIELDS, ...BACKGROUND_FIELDS, ...ADDITIONAL_FIELDS];

export function valueToInputString(v: string | boolean | null | undefined): string {
    if (v === null || v === undefined) {
        return '';
    }
    if (typeof v === 'boolean') {
        return v ? 'true' : 'false';
    }
    return v;
}

interface FieldRowProps {
    def: FieldDef;
    value: string;
    onChange: (next: string) => void;
    onCommit: (next: string) => void;
}

const FieldRow = ({def, value, onChange, onCommit}: FieldRowProps) => {
    const inputId = `ls-field-${def.field}`;
    const labelId = `ls-label-${def.field}`;

    function renderInput() {
        if (def.type === 'textarea') {
            return (
                <textarea
                    id={inputId}
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
            // Two-state radio: Yes / No. Initial null shows nothing checked, but
            // once the user picks a value there's no way to unset it (matches
            // the YAML form spec — null is a "not yet answered" state, not a
            // long-lived choice).
            const options: Array<[string, string]> = [['true', 'Yes'], ['false', 'No']];
            return (
                <div role='radiogroup' aria-labelledby={labelId}>
                    {options.map(([v, lbl]) => {
                        const radioId = `${inputId}-${v}`;
                        return (
                            <div className='form-check form-check-inline' key={v}>
                                <input
                                    type='radio'
                                    id={radioId}
                                    className='form-check-input'
                                    name={groupName}
                                    value={v}
                                    checked={value === v}
                                    onChange={() => { onChange(v); onCommit(v); }}
                                />
                                <label className='form-check-label' htmlFor={radioId}>{lbl}</label>
                            </div>
                        );
                    })}
                </div>
            );
        }
        return (
            <input
                id={inputId}
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
            <th className='w-25' scope='row' id={labelId}>
                {def.type === 'bool'
                    ? def.label
                    : <label htmlFor={inputId} className='mb-0'>{def.label}</label>}
            </th>
            <td>{renderInput()}</td>
        </tr>
    );
};

interface SectionProps {
    id: string;
    title: string;
    children: React.ReactNode;
}

const Section = ({id, title, children}: SectionProps) => (
    <section className='section' id={id} aria-labelledby={`${id}-heading`}>
        <h2 id={`${id}-heading`} className='heading'>{title}</h2>
        <table className='table table-borderless'>
            <tbody>{children}</tbody>
        </table>
    </section>
);

const LineSubmissionEdit = ({submissionId}: LineSubmissionEditProps) => {
    const [zdbID, setZdbID] = useState<string>(submissionId);
    const [values, setValues] = useState<Record<string, string> | null>(submissionId ? null : emptyValues());
    const [committed, setCommitted] = useState<Record<string, string>>(submissionId ? {} : emptyValues());
    const [loadError, setLoadError] = useState<string>('');
    const [saveEvent, setSaveEvent] = useState<SaveEvent | null>(null);
    const [saveSeq, setSaveSeq] = useState<number>(0);

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
    }

    function setFieldValue(field: ScalarField, next: string) {
        setValues(prev => prev ? {...prev, [field]: next} : prev);
    }

    function emit(event: Omit<SaveEvent, 'seq'>) {
        const seq = saveSeq + 1;
        setSaveSeq(seq);
        setSaveEvent({...event, seq});
    }

    async function commit(field: FieldDef, next: string) {
        if (next === committed[field.field]) {
            return;
        }
        emit({status: 'saving', label: field.label});
        try {
            const body = new URLSearchParams();
            if (zdbID) {
                body.append('zdbID', zdbID);
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
            const wasNew = !zdbID;
            applyDTO(data);
            if (wasNew && data.zdbID) {
                window.history.replaceState(null, '', `/action/zirc/line-submission/${data.zdbID}/edit`);
            }
            emit({status: 'saved', label: field.label});
        } catch (e) {
            emit({status: 'error', label: field.label, message: e instanceof Error ? e.message : 'Save failed'});
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
            onChange={next => setFieldValue(def.field, next)}
            onCommit={next => commit(def, next)}
        />
    );

    return <>
        <Section id='overview' title='Overview'>
            <tr>
                <th className='w-25'>ID</th>
                <td>
                    {zdbID
                        ? <code>{zdbID}</code>
                        : <span className='text-muted small'>(assigned on first save)</span>}
                </td>
            </tr>
            {OVERVIEW_FIELDS.map(renderRow)}
        </Section>
        <Section id='background' title='Background'>
            {BACKGROUND_FIELDS.map(renderRow)}
        </Section>
        <Section id='additional-info' title='Additional Info'>
            {ADDITIONAL_FIELDS.map(renderRow)}
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
