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
import { viewConfigFrom, leafOf, commentsEnabled } from '../useViewConfig';
import { StatusBadge } from '../../components/StatusBadge';
import { FieldHistory } from '../../components/FieldHistory';
import { FieldComments } from '../../components/FieldComments';
import { ValueDisplay } from '../../components/ValueDisplay';

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
    config,
}: ControlProps) {
    if (visible === false) {return null;}
    const fieldName = leafOf(path);
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const placeholder = (uischema as { options?: { placeholder?: string } })?.options?.placeholder;
    const view = viewConfigFrom(config);

    if (view.readonly) {
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    <div style={{ whiteSpace: 'pre-wrap' }}>
                        <ValueDisplay value={data}/>
                    </div>
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
                <div style={{ maxWidth: '40em' }}>
                    <textarea
                        id={inputId}
                        className='form-control'
                        rows={3}
                        placeholder={placeholder}
                        value={(data as string | undefined) ?? ''}
                        onChange={(e) => handleChange(path, e.target.value)}
                    />
                    {errors && <small className='text-danger'>{errors}</small>}
                </div>
            </td>
        </tr>
    );
}

export const textareaRowRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, schemaTypeIs('string'), optionIs('multi', true))),
    renderer: withJsonFormsControlProps(TextareaRowRenderer),
};
