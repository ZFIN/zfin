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
 * List-of-strings widget rendered as one text input per entry with a
 * Remove button beside each and an "+ Add …" button at the bottom.
 * Edits flow through {@code handleChange(path, nextArray)}.
 *
 * Recognized {@code options}:
 * <ul>
 *   <li>{@code placeholder} — per-input placeholder text.</li>
 *   <li>{@code addLabel} — label for the Add button (e.g.
 *     "+ Add publication"). Defaults to "+ Add" if unset.</li>
 * </ul>
 *
 * Used for previousNames (LineSubmission), publications (Mutation),
 * enzymeCleaves (Assay), and segregation / phenotype type (Phenotype).
 * Rendered by any uiSchema Control with {@code options.widget = "stringList"}.
 */
type StringListOptions = {
    placeholder?: string;
    addLabel?: string;
};

function PublicationsListRenderer({
    data,
    handleChange,
    path,
    label,
    visible,
    uischema,
    config,
}: ControlProps) {
    if (visible === false) {return null;}
    const fieldName = leafOf(path);
    const labelId = `fr-label-${fieldName}`;
    // Defensive: tolerate a non-array (e.g. a stale single-string value
    // pre-array migration) so the renderer never crashes mid-render.
    const items: string[] = Array.isArray(data)
        ? (data as string[])
        : typeof data === 'string' && data.length > 0
            ? [data]
            : [];

    const opts = ((uischema as { options?: StringListOptions } | undefined)?.options) ?? {};
    const placeholder = opts.placeholder ?? '';
    const addLabel = opts.addLabel ?? '+ Add';

    const view = viewConfigFrom(config);
    if (view.readonly) {
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    {items.length === 0
                        ? <span className='text-muted'>&mdash;</span>
                        : (
                            <ul className='list-unstyled mb-0'>
                                {items.map((p, i) => <li key={i}>{p}</li>)}
                            </ul>
                        )}
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

    // Don't filter blanks anywhere — the user needs a fresh empty input
    // when they click "+ Add", and a blank mid-edit shouldn't disappear.
    // If a curator leaves a blank entry on save, the server stores it as
    // an empty-string element; trim/filter is a server-side responsibility.
    const updateAt = (i: number, value: string) => {
        const next = items.slice();
        next[i] = value;
        handleChange(path, next);
    };

    const removeAt = (i: number) => {
        const next = items.slice();
        next.splice(i, 1);
        handleChange(path, next);
    };

    const addOne = () => {
        handleChange(path, [...items, '']);
    };

    return (
        <tr>
            <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                {label}
            </th>
            <td>
                <div style={{ maxWidth: '40em' }}>
                    {items.map((item, i) => (
                        <div key={i} className='d-flex mb-2' style={{ gap: '8px' }}>
                            <input
                                type='text'
                                className='form-control'
                                placeholder={placeholder}
                                value={item}
                                onChange={(e) => updateAt(i, e.target.value)}
                                autoComplete='off'
                            />
                            <button
                                type='button'
                                className='btn btn-sm btn-outline-danger'
                                onClick={() => removeAt(i)}
                            >
                                Remove
                            </button>
                        </div>
                    ))}
                    <button
                        type='button'
                        className='btn btn-sm btn-outline-secondary'
                        onClick={addOne}
                    >
                        {addLabel}
                    </button>
                </div>
            </td>
        </tr>
    );
}

export const publicationsListRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'stringList'))),
    renderer: withJsonFormsControlProps(PublicationsListRenderer),
};
