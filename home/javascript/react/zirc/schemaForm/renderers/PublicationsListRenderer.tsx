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

/**
 * Simple list-of-strings widget rendered as one textarea, one entry per line.
 * Blank lines are filtered on change so the server receives a clean array.
 *
 * Used for mutation publications (free-text citations / PMIDs / DOIs);
 * appropriate for any uiSchema Control with options.widget = 'stringList'.
 */
function PublicationsListRenderer({
    data,
    handleChange,
    path,
    label,
    visible,
    config,
}: ControlProps) {
    if (visible === false) {return null;}
    const fieldName = leafOf(path);
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const value = ((data as string[] | undefined) ?? []).join('\n');

    const view = viewConfigFrom(config);
    if (view.readonly) {
        const items = (data as string[] | undefined) ?? [];
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }}>{label}</th>
                <td>
                    {items.length === 0
                        ? <span className='text-muted'>&mdash;</span>
                        : (
                            <ul className='list-unstyled mb-0'>
                                {items.map((p, i) => <li key={i}>{p}</li>)}
                            </ul>
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
                <textarea
                    id={inputId}
                    className='form-control'
                    rows={4}
                    placeholder='One citation, PMID, or DOI per line'
                    value={value}
                    onChange={(e) => {
                        const next = e.target.value
                            .split('\n')
                            .map((s) => s.trim())
                            .filter((s) => s.length > 0);
                        handleChange(path, next);
                    }}
                />
            </td>
        </tr>
    );
}

export const publicationsListRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'stringList'))),
    renderer: withJsonFormsControlProps(PublicationsListRenderer),
};
