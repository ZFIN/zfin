import React, {useState} from 'react';

interface PhenotypeTimingRowProps {
    /** Storage is always hpf integer (or null). The UI toggle below is
     *  purely a display convenience — selecting 'dpf' multiplies by 24
     *  on the way in and divides by 24 on the way out. */
    hpfStart: number | null;
    hpfEnd: number | null;
    /** Server-derived display from hpfStart. Read-only echo. */
    stage: string | null;
    /** Commit a wire-format patch (one or both of hpfStart, hpfEnd as
     *  integers or null). The renderer translates the patch into an
     *  array update on the parent. */
    onCommit: (patch: {hpfStart?: number | null; hpfEnd?: number | null}) => void;
}

/**
 * Phenotype timing widget for the mutation editor's phenotypes section.
 * Renders Start + End number inputs with an hpf/dpf unit radio that's
 * purely UI state — the wire format is always hpf integer.
 *
 * Lifted out of the legacy MutationEdit container (pre-ZFIN-10265-schema-refactor)
 * and adapted for the schema-driven renderer's CustomNode interface.
 */
const PhenotypeTimingRow = ({hpfStart, hpfEnd, stage, onCommit}: PhenotypeTimingRowProps) => {
    const [unit, setUnit] = useState<'hpf' | 'dpf'>('hpf');
    const [startDraft, setStartDraft] = useState<string>(hpfToDisplay(hpfStart, unit));
    const [endDraft, setEndDraft] = useState<string>(hpfToDisplay(hpfEnd, unit));

    // Re-sync drafts when the wire value changes externally (server response,
    // load, or unit toggle).
    React.useEffect(() => {
        setStartDraft(hpfToDisplay(hpfStart, unit));
        setEndDraft(hpfToDisplay(hpfEnd, unit));
    }, [hpfStart, hpfEnd, unit]);

    const unitGroupName = 'phn-unit';
    return (
        <div className='form-group row'>
            <span className='col-sm-3 col-form-label'>Timing</span>
            <div className='col-sm-9'>
                <div className='d-flex align-items-center flex-wrap' style={{gap: 12}}>
                    <div role='radiogroup' aria-label='Unit'>
                        {(['hpf', 'dpf'] as const).map(u => {
                            const id = `${unitGroupName}-${u}`;
                            return (
                                <div className='form-check form-check-inline' key={u}>
                                    <input
                                        type='radio'
                                        id={id}
                                        className='form-check-input'
                                        name={unitGroupName}
                                        value={u}
                                        checked={unit === u}
                                        onChange={() => setUnit(u)}
                                    />
                                    <label className='form-check-label' htmlFor={id}>{u}</label>
                                </div>
                            );
                        })}
                    </div>
                    <label className='mb-0 small text-muted'>Start</label>
                    <input
                        type='number'
                        step='any'
                        className='form-control'
                        style={{maxWidth: 120}}
                        value={startDraft}
                        onChange={e => setStartDraft(e.target.value)}
                        onBlur={() => onCommit({hpfStart: displayToHpf(startDraft, unit)})}
                    />
                    <label className='mb-0 small text-muted'>End</label>
                    <input
                        type='number'
                        step='any'
                        className='form-control'
                        style={{maxWidth: 120}}
                        placeholder='(optional)'
                        value={endDraft}
                        onChange={e => setEndDraft(e.target.value)}
                        onBlur={() => onCommit({hpfEnd: displayToHpf(endDraft, unit)})}
                    />
                </div>
                {stage && (
                    <small className='form-text text-muted'>
                        Stage at 28.5°C: <em>{stage}</em>
                    </small>
                )}
            </div>
        </div>
    );
};

/**
 * Convert a stored hpf integer (or null) into the display string for the
 * chosen unit. Lossless when dpf divides evenly; rounded to 2 decimals
 * otherwise.
 */
function hpfToDisplay(hpf: number | null, unit: 'hpf' | 'dpf'): string {
    if (hpf == null) {
        return '';
    }
    if (unit === 'hpf') {
        return String(hpf);
    }
    const d = hpf / 24;
    return Number.isInteger(d) ? String(d) : d.toFixed(2);
}

/**
 * Convert a user-typed display string back to an hpf integer (or null
 * for blank/unparseable input). dpf values multiply by 24 and round.
 */
function displayToHpf(input: string, unit: 'hpf' | 'dpf'): number | null {
    const s = input.trim();
    if (!s) {
        return null;
    }
    const n = Number(s);
    if (!Number.isFinite(n)) {
        return null;
    }
    return Math.round(unit === 'dpf' ? n * 24 : n);
}

export default PhenotypeTimingRow;
