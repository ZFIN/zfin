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
import { viewConfigFrom, leafOf } from '../useViewConfig';
import { StatusBadge } from '../../components/StatusBadge';

type AutoSizeOptions = {
    /** Sibling field whose sequence length feeds this value. */
    sourceField?: string;
    /** Fixed value, when the size is definitional (point mutation = 1 bp). */
    constantValue?: number;
    suffix?: string;
};

/**
 * Read-only size field whose value is always derived, never typed — either a
 * fixed constant (a point mutation is 1 bp) or the nucleotide count of a
 * sibling sequence field (deletion / insertion sizes follow their sequence).
 *
 * The authoritative value is recomputed server-side on every save
 * (ZircSubmissionService#recalcLesionSizes); this widget mirrors that same
 * derivation live for display and writes nothing back to the form, so there
 * is no autosave round-trip and no controlled-input race.
 */
function sequenceLength(seq: unknown): number | null {
    if (typeof seq !== 'string') {return null;}
    const letters = seq.match(/[A-Za-z]/g);
    return letters ? letters.length : null;
}

function AutoSizeRenderer({ path, label, uischema, visible, config }: ControlProps) {
    const ctx = useJsonForms();
    if (visible === false) {return null;}

    const opts = ((uischema as { options?: AutoSizeOptions } | undefined)?.options) ?? {};
    const { sourceField, constantValue, suffix } = opts;
    const fieldName = leafOf(path);
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const view = viewConfigFrom(config);

    const rootData = (ctx.core?.data ?? {}) as Record<string, unknown>;
    const computed = constantValue != null
        ? constantValue
        : (sourceField ? sequenceLength(rootData[sourceField]) : null);
    const display = computed == null ? '' : String(computed);

    return (
        <tr>
            <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                {view.readonly && <StatusBadge status={view.fieldStatus[fieldName]}/>}
                {view.readonly
                    ? label
                    : <label htmlFor={inputId} className='mb-0'>{label}</label>}
            </th>
            <td>
                <div style={{ maxWidth: '40em' }}>
                    <div className='input-group'>
                        <input
                            id={inputId}
                            type='text'
                            className='form-control'
                            value={display}
                            readOnly
                            disabled
                            aria-label={label ?? fieldName}
                        />
                        {suffix && (
                            <div className='input-group-append'>
                                <span className='input-group-text'>{suffix}</span>
                            </div>
                        )}
                    </div>
                </div>
            </td>
        </tr>
    );
}

export const autoSizeRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'autoSize'))),
    renderer: withJsonFormsControlProps(AutoSizeRenderer),
};
