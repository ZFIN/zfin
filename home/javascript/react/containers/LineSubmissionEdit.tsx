import React, {useEffect, useRef, useState} from 'react';
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
    reasons?: string[] | null;
    reasonsOther?: string | null;
    isDraft?: boolean | null;
}

type ScalarField = Exclude<keyof LineSubmissionDTO,
    'zdbID' | 'isDraft' | 'reasons' | 'reasonsOther'>;
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

const REASON_OPTIONS: Array<{value: string; label: string}> = [
    {value: 'frequently_requested',     label: 'Currently frequently requested'},
    {value: 'expect_high_demand',       label: 'Expect high demand'},
    {value: 'interesting_gene',         label: 'Interesting gene'},
    {value: 'community_resource',       label: 'Community resource/tool'},
    {value: 'mutant_gene_cloned',       label: 'Mutant gene cloned'},
    {value: 'danger_of_losing',         label: 'Danger of losing line'},
    {value: 'lack_of_space_or_funding', label: 'Lack of space or funding to maintain line'},
];

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
        {children}
    </section>
);

const FieldsTable = ({children}: {children: React.ReactNode}) => (
    <table className='table table-borderless'>
        <tbody>{children}</tbody>
    </table>
);

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

const LineSubmissionEdit = ({submissionId}: LineSubmissionEditProps) => {
    const [zdbID, setZdbID] = useState<string>(submissionId);
    const [values, setValues] = useState<Record<string, string> | null>(submissionId ? null : emptyValues());
    const [committed, setCommitted] = useState<Record<string, string>>(submissionId ? {} : emptyValues());
    const [reasons, setReasons] = useState<string[]>([]);
    const [reasonsOther, setReasonsOther] = useState<string>('');
    const [otherChecked, setOtherChecked] = useState<boolean>(false);
    const [committedReasonsOther, setCommittedReasonsOther] = useState<string>('');
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
                const normalized = valueToInputString(data[field.field]);
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
        <Section id='background' title='Background'>
            <FieldsTable>{BACKGROUND_FIELDS.map(renderRow)}</FieldsTable>
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
