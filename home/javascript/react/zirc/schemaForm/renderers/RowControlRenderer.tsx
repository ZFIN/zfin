import * as React from 'react';
import {
    and,
    ControlProps,
    isControl,
    JsonFormsRendererRegistryEntry,
    or,
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
 * Renders a scalar (string- or number-typed) Control as a table row
 * matching the reference markup: <tr><th class="w-25" id="fr-label-X">
 * <label for="fr-X">…</th><td><input id="fr-X" class="form-control" />
 * </td></tr>.
 *
 * Numeric Controls render an <input type="number"> and round-trip a
 * number (or null when cleared) through handleChange, so the diff and
 * the field-path PATCH carry a number rather than a string. String
 * Controls keep the plain text input.
 *
 * Path comes from JSON Forms as e.g. "name" or "lesionSizeBp"; we don't
 * touch it. The data round-trips through handleChange(path, value).
 */
type RowOptions = {
    placeholder?: string;
    helpText?: string;
    infoHref?: string;
    suffix?: string;
};

function isNumericSchema(schemaType: unknown): boolean {
    if (schemaType === 'number' || schemaType === 'integer') {return true;}
    return Array.isArray(schemaType)
        && (schemaType.includes('number') || schemaType.includes('integer'));
}

function RowControlRenderer({
    data,
    handleChange,
    path,
    label,
    required,
    errors,
    visible,
    uischema,
    schema,
    config,
}: ControlProps) {
    if (visible === false) {return null;}

    const fieldName = leafOf(path);
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const opts = ((uischema as { options?: RowOptions } | undefined)?.options) ?? {};
    const { placeholder, helpText, infoHref, suffix } = opts;
    const view = viewConfigFrom(config);

    // Read-only sources: (a) the whole form is in view mode; (b) the schema
    // marks this field as server-managed (e.g. createdAt). Both render the
    // value as text — never an editable input.
    const fieldReadOnly = (schema as { readOnly?: boolean } | undefined)?.readOnly === true;
    if (view.readonly || fieldReadOnly) {
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    <ValueDisplay value={data} schema={schema}/>
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

    const numeric = isNumericSchema((schema as { type?: unknown } | undefined)?.type);

    const onChange = (raw: string) => {
        if (!numeric) {
            handleChange(path, raw);
            return;
        }
        // Empty clears to null; otherwise a real number. Guard NaN even
        // though a type=number input shouldn't produce it.
        const num = raw === '' ? null : Number(raw);
        handleChange(path, num !== null && Number.isNaN(num) ? null : num);
    };

    const input = (
        <input
            id={inputId}
            type={numeric ? 'number' : 'text'}
            step={numeric ? 'any' : undefined}
            className='form-control'
            value={(data as string | number | undefined) ?? ''}
            onChange={(e) => onChange(e.target.value)}
            autoComplete='off'
            placeholder={placeholder}
        />
    );

    return (
        <tr>
            <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
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
    tester: rankWith(10, and(isControl,
        or(schemaTypeIs('string'), schemaTypeIs('number'), schemaTypeIs('integer')))),
    renderer: withJsonFormsControlProps(RowControlRenderer),
};
