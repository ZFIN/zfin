import * as React from 'react';
import {
    and,
    ControlProps,
    isControl,
    JsonFormsRendererRegistryEntry,
    rankWith,
    schemaTypeIs,
} from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';

/**
 * Renders a string-typed Control as a table row matching the reference
 * markup: <tr><th class="w-25" id="fr-label-X"><label for="fr-X">…</th>
 * <td><input id="fr-X" class="form-control" /></td></tr>.
 *
 * Path comes from JSON Forms as e.g. "name" or "previousNames"; we don't
 * touch it. The data round-trips through handleChange(path, value).
 */
type RowOptions = {
    placeholder?: string;
    helpText?: string;
    infoHref?: string;
    suffix?: string;
};

function RowControlRenderer({
    data,
    handleChange,
    path,
    label,
    required,
    errors,
    visible,
    uischema,
}: ControlProps) {
    if (visible === false) {return null;}

    const fieldName = path.split('.').pop() ?? path;
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const opts = ((uischema as { options?: RowOptions } | undefined)?.options) ?? {};
    const { placeholder, helpText, infoHref, suffix } = opts;

    const input = (
        <input
            id={inputId}
            type='text'
            className='form-control'
            value={(data as string | undefined) ?? ''}
            onChange={(e) => handleChange(path, e.target.value)}
            autoComplete='off'
            placeholder={placeholder}
        />
    );

    return (
        <tr>
            <th className='w-25' scope='row' id={labelId}>
                <label htmlFor={inputId} className='mb-0'>
                    {label}{required ? ' *' : ''}
                </label>
                {infoHref && (
                    <a
                        href={infoHref}
                        target='_blank'
                        rel='noopener noreferrer'
                        className='ml-1 small'
                        aria-label={`More info about ${label}`}
                        title='More info'
                    >
                        (info)
                    </a>
                )}
            </th>
            <td>
                {suffix ? (
                    <div className='input-group'>
                        {input}
                        <div className='input-group-append'>
                            <span className='input-group-text'>{suffix}</span>
                        </div>
                    </div>
                ) : input}
                {helpText && (
                    <small className='form-text text-muted'>{helpText}</small>
                )}
                {errors && (
                    <small className='text-danger'>{errors}</small>
                )}
            </td>
        </tr>
    );
}

export const rowControlRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(10, and(isControl, schemaTypeIs('string'))),
    renderer: withJsonFormsControlProps(RowControlRenderer),
};
