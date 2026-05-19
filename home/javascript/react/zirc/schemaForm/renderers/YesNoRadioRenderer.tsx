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

/**
 * 2-state Yes/No radio for nullable booleans. Matches the reference form's
 * pattern: neither radio is checked until the user picks, and once picked
 * stays one of true/false (no "Unspecified" option to un-set).
 *
 * Tester: any Control whose uiSchema sets options.widget = 'yesNoRadio'.
 */
function YesNoRadioRenderer({ data, handleChange, path, label, visible }: ControlProps) {
    if (visible === false) {return null;}
    const fieldName = path.split('.').pop() ?? path;
    const labelId = `fr-label-${fieldName}`;
    const name = `fr-${fieldName}-group`;

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
