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

/**
 * Single boolean rendered as one checkbox. Distinct from yesNoRadio (two
 * radio buttons forced to a yes/no decision) — checkbox is appropriate
 * for fields where "not yet decided" / null is meaningful and the box
 * just turns on a flag.
 *
 * Reads {@code data} as a tri-state Boolean | null. Toggling produces
 * true/false; there's no UI affordance to return to null once toggled,
 * matching the old form's behavior.
 *
 * Triggered by uiSchema {@code options.widget = "checkbox"}.
 */
function CheckboxRenderer({
    data, handleChange, path, label, visible, uischema, schema, config,
}: ControlProps) {
    if (visible === false) {return null;}
    const fieldName = leafOf(path);
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const checked = data === true;

    const view = viewConfigFrom(config);
    const fieldReadOnly = (schema as { readOnly?: boolean } | undefined)?.readOnly === true;
    if (view.readonly || fieldReadOnly) {
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    {data === true ? 'Yes' : data === false ? 'No' : <span className='text-muted'>&mdash;</span>}
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
                <div className='form-check'>
                    <input
                        type='checkbox'
                        id={inputId}
                        className='form-check-input'
                        checked={checked}
                        onChange={(e) => handleChange(path, e.target.checked)}
                    />
                </div>
            </td>
        </tr>
    );
}

export const checkboxRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'checkbox'))),
    renderer: withJsonFormsControlProps(CheckboxRenderer),
};
