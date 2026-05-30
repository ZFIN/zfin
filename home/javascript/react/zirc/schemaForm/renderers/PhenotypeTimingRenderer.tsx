import * as React from 'react';
import {
    and,
    ControlProps,
    isControl,
    JsonFormsRendererRegistryEntry,
    optionIs,
    rankWith,
} from '@jsonforms/core';
import { useJsonForms, withJsonFormsControlProps } from '@jsonforms/react';
import { viewConfigFrom } from '../useViewConfig';

/**
 * Phenotype timing widget for the per-phenotype edit form (M8.1). The
 * Control is bound to {@code hpfStart} via {@code options.widget =
 * 'phenotypeTiming'}; the renderer reads {@code hpfEnd} and
 * {@code stage} from the parent form data via {@code useJsonForms}
 * and writes back to them through {@code handleChange}.
 *
 * <p>The hpf/dpf unit toggle is purely UI state. The wire format is
 * always integer hpf — selecting 'dpf' multiplies by 24 on input and
 * divides by 24 on display. {@code stage} is server-derived in the
 * legacy code path; here it's a read-only echo (TODO: re-derive
 * once the STAGE lookup is wired in).
 */
function PhenotypeTimingRenderer({ data, handleChange, path, config }: ControlProps) {
    const ctx = useJsonForms();
    const root = (ctx.core?.data ?? {}) as {
        hpfStart?: number | null;
        hpfEnd?: number | null;
        stage?: string | null;
    };
    const hpfStart = (data ?? null) as number | null;
    const hpfEnd = root.hpfEnd ?? null;
    const stage = root.stage ?? null;
    const view = viewConfigFrom(config);

    if (view.readonly) {
        const startDisplay = hpfStart == null ? '—' : `${hpfStart} hpf`;
        const endDisplay = hpfEnd == null ? '' : ` – ${hpfEnd} hpf`;
        return (
            <div className='form-group row mb-2'>
                <span className='col-sm-3 col-form-label'>Timing</span>
                <div className='col-sm-9'>
                    <span>{startDisplay}{endDisplay}</span>
                    {stage && (
                        <small className='form-text text-muted'>
                            Stage at 28.5°C: <em>{stage}</em>
                        </small>
                    )}
                </div>
            </div>
        );
    }

    const endPath = path.replace(/hpfStart$/, 'hpfEnd');
    const [unit, setUnit] = React.useState<'hpf' | 'dpf'>('hpf');
    const [startDraft, setStartDraft] = React.useState<string>(hpfToDisplay(hpfStart, unit));
    const [endDraft, setEndDraft] = React.useState<string>(hpfToDisplay(hpfEnd, unit));

    React.useEffect(() => {
        setStartDraft(hpfToDisplay(hpfStart, unit));
        setEndDraft(hpfToDisplay(hpfEnd, unit));
    }, [hpfStart, hpfEnd, unit]);

    const commitStart = () => {
        const next = displayToHpf(startDraft, unit);
        if (next !== hpfStart) {handleChange(path, next);}
    };
    const commitEnd = () => {
        const next = displayToHpf(endDraft, unit);
        if (next !== hpfEnd) {handleChange(endPath, next);}
    };

    const unitGroupName = `phn-unit-${path}`;
    return (
        <div className='form-group row'>
            <span className='col-sm-3 col-form-label'>Timing</span>
            <div className='col-sm-9'>
                <div className='d-flex align-items-center flex-wrap' style={{ gap: 12 }}>
                    <div role='radiogroup' aria-label='Unit'>
                        {(['hpf', 'dpf'] as const).map((u) => {
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
                        style={{ maxWidth: 120 }}
                        value={startDraft}
                        onChange={(e) => setStartDraft(e.target.value)}
                        onBlur={commitStart}
                    />
                    <label className='mb-0 small text-muted'>End</label>
                    <input
                        type='number'
                        step='any'
                        className='form-control'
                        style={{ maxWidth: 120 }}
                        placeholder='(optional)'
                        value={endDraft}
                        onChange={(e) => setEndDraft(e.target.value)}
                        onBlur={commitEnd}
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
}

function hpfToDisplay(hpf: number | null, unit: 'hpf' | 'dpf'): string {
    if (hpf == null) {return '';}
    if (unit === 'hpf') {return String(hpf);}
    const d = hpf / 24;
    return Number.isInteger(d) ? String(d) : d.toFixed(2);
}

function displayToHpf(display: string, unit: 'hpf' | 'dpf'): number | null {
    if (!display.trim()) {return null;}
    const n = Number(display);
    if (!Number.isFinite(n)) {return null;}
    return unit === 'hpf' ? Math.round(n) : Math.round(n * 24);
}

export const phenotypeTimingRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'phenotypeTiming'))),
    renderer: withJsonFormsControlProps(PhenotypeTimingRenderer),
};
