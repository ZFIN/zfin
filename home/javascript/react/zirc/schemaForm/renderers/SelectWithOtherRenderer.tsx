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

const OTHER_SENTINEL = '__other';

/**
 * Generic "select from a standard list, or type Other" widget. The canonical
 * values come from uiSchema `options.standardValues`; the value the server
 * receives is either one of those strings or the user's free-text override.
 *
 * "Other selected" is tracked as local UI state so picking Other on an empty
 * field doesn't immediately hide the revealed input again.
 *
 * Used by Background's maternal/paternal strain selects and Mutagenesis's
 * protocol select.
 */
function SelectWithOtherRenderer({ data, handleChange, path, label, uischema, visible }: ControlProps) {
    if (visible === false) {return null;}

    const fieldName = path.split('.').pop() ?? path;
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const options = (uischema as { options?: { standardValues?: string[] } } | undefined)?.options ?? {};
    const standardValues = options.standardValues ?? [];
    const value = (data as string | undefined) ?? '';
    const isStandard = standardValues.includes(value);
    const [otherSelected, setOtherSelected] = React.useState(() => value !== '' && !isStandard);
    const selectValue = isStandard ? value : (otherSelected ? OTHER_SENTINEL : '');

    return (
        <tr>
            <th className='w-25' scope='row' id={labelId}>
                <label htmlFor={inputId} className='mb-0'>{label}</label>
            </th>
            <td>
                <div className='d-flex' style={{ gap: 8 }}>
                    <select
                        id={inputId}
                        className='form-control'
                        style={{ maxWidth: 200 }}
                        value={selectValue}
                        onChange={(e) => {
                            const v = e.target.value;
                            if (v === OTHER_SENTINEL) {
                                setOtherSelected(true);
                                if (isStandard) {handleChange(path, '');}
                            } else {
                                setOtherSelected(false);
                                handleChange(path, v);
                            }
                        }}
                    >
                        <option value=''>(select)</option>
                        {standardValues.map((s) => (
                            <option key={s} value={s}>{s}</option>
                        ))}
                        <option value={OTHER_SENTINEL}>Other</option>
                    </select>
                    {otherSelected && (
                        <input
                            type='text'
                            id={`${inputId}-other`}
                            className='form-control'
                            placeholder='Other'
                            aria-label={`${label} (other)`}
                            value={value}
                            onChange={(e) => handleChange(path, e.target.value)}
                        />
                    )}
                </div>
            </td>
        </tr>
    );
}

export const selectWithOtherRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'selectWithOther'))),
    renderer: withJsonFormsControlProps(SelectWithOtherRenderer),
};
