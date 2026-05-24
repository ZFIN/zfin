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
import { viewConfigFrom, leafOf } from '../useViewConfig';
import { StatusBadge } from '../../components/StatusBadge';
import { FieldHistory } from '../../components/FieldHistory';
import { ValueDisplay } from '../../components/ValueDisplay';

/**
 * 2-state Yes/No radio for nullable booleans. Matches the reference form's
 * pattern: neither radio is checked until the user picks, and once picked
 * stays one of true/false (no "Unspecified" option to un-set).
 *
 * Tester: any Control whose uiSchema sets options.widget = 'yesNoRadio'.
 */
function YesNoRadioRenderer({ data, handleChange, path, label, visible, config }: ControlProps) {
    if (visible === false) {return null;}
    const fieldName = leafOf(path);
    const labelId = `fr-label-${fieldName}`;
    const name = `fr-${fieldName}-group`;
    const view = viewConfigFrom(config);

    if (view.readonly) {
        return (
            <tr>
                <th className='w-25' scope='row' id={labelId}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    <ValueDisplay value={data}/>
                    <FieldHistory
                        fieldKey={fieldName}
                        label={label ?? fieldName}
                        updates={view.fieldUpdates[fieldName]}
                    />
                </td>
            </tr>
        );
    }

    return (
        <tr>
            <th className='w-25' scope='row' id={labelId}>{label}</th>
            <td>
                <div role='radiogroup' aria-labelledby={labelId}>
                    <div className='form-check form-check-inline'>
                        <input
                            type='radio'
                            id={`fr-${fieldName}-true`}
                            className='form-check-input'
                            name={name}
                            value='true'
                            checked={data === true}
                            onChange={() => handleChange(path, true)}
                        />
                        <label className='form-check-label' htmlFor={`fr-${fieldName}-true`}>Yes</label>
                    </div>
                    <div className='form-check form-check-inline'>
                        <input
                            type='radio'
                            id={`fr-${fieldName}-false`}
                            className='form-check-input'
                            name={name}
                            value='false'
                            checked={data === false}
                            onChange={() => handleChange(path, false)}
                        />
                        <label className='form-check-label' htmlFor={`fr-${fieldName}-false`}>No</label>
                    </div>
                </div>
            </td>
        </tr>
    );
}

export const yesNoRadioRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'yesNoRadio'))),
    renderer: withJsonFormsControlProps(YesNoRadioRenderer),
};
