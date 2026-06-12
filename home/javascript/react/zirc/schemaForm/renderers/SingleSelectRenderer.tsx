import * as React from 'react';
import {
    and,
    ControlProps,
    isControl,
    JsonFormsRendererRegistryEntry,
    optionIs,
    rankWith,
} from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { viewConfigFrom, leafOf, commentsEnabled } from '../useViewConfig';
import { StatusBadge } from '../../components/StatusBadge';
import { FieldHistory } from '../../components/FieldHistory';
import { FieldComments } from '../../components/FieldComments';
import { ValueDisplay } from '../../components/ValueDisplay';

const OTHER_SENTINEL = '__other';

/**
 * Single-select dropdown over a {@code string[]} scope: the curator picks at
 * most one value, stored as a 0- or 1-element array so the underlying
 * {@code text[]} column is unchanged. Choosing the blank "(select)" option
 * clears the array.
 *
 * Unless {@code options.noOther} is set, the dropdown also offers an "Other"
 * choice that reveals a free-text input; the typed value is stored as the
 * single (non-standard) array element. "Other selected" is local UI state so
 * picking Other on an empty field doesn't immediately hide the input again
 * (mirrors {@link SelectWithOtherRenderer}, array-backed).
 *
 * Used by Phenotype's segregation (closed list — {@code noOther}) and
 * phenotype type (Other allowed) — ZFIN-10348 / ZFIN-10349. The Non-Mendelian
 * %/comment reveal keys off the array {@code contains "Non-Mendelian"} rule,
 * which a 1-element array still satisfies.
 *
 * Recognized {@code options}:
 * <ul>
 *   <li>{@code standardValues} — canonical values listed in the dropdown.</li>
 *   <li>{@code standardLabels} — parallel display labels (falls back to value).</li>
 *   <li>{@code noOther} — suppress the "Other" option + free-text input.</li>
 * </ul>
 *
 * Rendered by any uiSchema Control with {@code options.widget = "singleSelect"}.
 */
type SingleSelectOptions = {
    standardValues?: string[];
    standardLabels?: string[];
    noOther?: boolean;
};

function SingleSelectRenderer({ data, handleChange, path, label, uischema, visible, config }: ControlProps) {
    if (visible === false) {return null;}

    const fieldName = leafOf(path);
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const options = ((uischema as { options?: SingleSelectOptions } | undefined)?.options) ?? {};
    const standardValues = options.standardValues ?? [];
    const standardLabels = options.standardLabels ?? null;
    const noOther = options.noOther === true;
    const labelFor = (v: string, i: number) =>
        standardLabels && standardLabels[i] != null ? standardLabels[i] : v;

    // Array-backed: the value is the first (and only) element, if any.
    const items: string[] = Array.isArray(data) ? (data as string[]) : [];
    const value = items.length > 0 ? items[0] : '';
    const isStandard = standardValues.includes(value);
    const [otherSelected, setOtherSelected] = React.useState(
        () => !noOther && value !== '' && !isStandard,
    );
    const selectValue = isStandard ? value : (otherSelected ? OTHER_SENTINEL : '');
    const view = viewConfigFrom(config);

    if (view.readonly) {
        const display = value
            ? labelFor(value, standardValues.indexOf(value))
            : null;
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    {display != null ? <ValueDisplay value={display}/> : <span className='text-muted'>&mdash;</span>}
                    <FieldHistory
                        recId={view.recId}
                        scope='field'
                        fieldName={fieldName}
                        label={label ?? fieldName}
                    />
                    {commentsEnabled(uischema) && (
                        <FieldComments
                            recId={view.recId}
                            scope='field'
                            fieldName={fieldName}
                            label={label ?? fieldName}
                        />
                    )}
                </td>
            </tr>
        );
    }

    return (
        <tr>
            <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                <label htmlFor={inputId} className='mb-0'>{label}</label>
            </th>
            <td>
                <div className='d-flex' style={{ gap: 8 }}>
                    <select
                        id={inputId}
                        className='form-control'
                        style={{ maxWidth: 280 }}
                        value={selectValue}
                        onChange={(e) => {
                            const v = e.target.value;
                            if (v === OTHER_SENTINEL) {
                                setOtherSelected(true);
                                // Clear a previously-picked standard value so the
                                // free-text input starts empty.
                                if (isStandard) {handleChange(path, []);}
                            } else {
                                setOtherSelected(false);
                                handleChange(path, v ? [v] : []);
                            }
                        }}
                    >
                        <option value=''>(select)</option>
                        {standardValues.map((s, i) => (
                            <option key={s} value={s}>{labelFor(s, i)}</option>
                        ))}
                        {!noOther && (
                            <option value={OTHER_SENTINEL}>Other</option>
                        )}
                    </select>
                    {!noOther && otherSelected && (
                        <input
                            type='text'
                            id={`${inputId}-other`}
                            className='form-control'
                            placeholder='Please specify'
                            aria-label={`${label} (other)`}
                            value={value}
                            onChange={(e) => handleChange(path, e.target.value ? [e.target.value] : [])}
                        />
                    )}
                </div>
            </td>
        </tr>
    );
}

export const singleSelectRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'singleSelect'))),
    renderer: withJsonFormsControlProps(SingleSelectRenderer),
};
