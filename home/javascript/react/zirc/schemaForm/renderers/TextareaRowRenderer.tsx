import * as React from 'react';
import {
    and,
    ControlProps,
    isControl,
    JsonFormsRendererRegistryEntry,
    optionIs,
    rankWith,
    schemaTypeIs,
} from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';

/**
 * Multi-line string renderer. Tester claims any string Control whose uiSchema
 * carries `options.multi: true`. Same table-row shape + fr-* ids as the
 * single-line RowControlRenderer; only the input element differs.
 */
function TextareaRowRenderer({
    data,
    handleChange,
    path,
    label,
    uischema,
    errors,
    visible,
}: ControlProps) {
    if (visible === false) {return null;}
    const fieldName = path.split('.').pop() ?? path;
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const placeholder = (uischema as { options?: { placeholder?: string } })?.options?.placeholder;
    return (
        <tr>
            <th className='w-25' scope='row' id={labelId}>
                <label htmlFor={inputId} className='mb-0'>{label}</label>
            </th>
            <td>
                <textarea
                    id={inputId}
                    className='form-control'
                    rows={3}
                    placeholder={placeholder}
                    value={(data as string | undefined) ?? ''}
                    onChange={(e) => handleChange(path, e.target.value)}
                />
                {errors && <small className='text-danger'>{errors}</small>}
            </td>
        </tr>
    );
}

export const textareaRowRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, schemaTypeIs('string'), optionIs('multi', true))),
    renderer: withJsonFormsControlProps(TextareaRowRenderer),
};
