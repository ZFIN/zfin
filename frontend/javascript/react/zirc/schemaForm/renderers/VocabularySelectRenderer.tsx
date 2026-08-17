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
import { labelById, termLabel } from '../vocabularyHelpers';
import { useVocabulary, VocabularyName } from '../../api/queries';
import { StatusBadge } from '../../components/StatusBadge';
import { FieldHistory } from '../../components/FieldHistory';
import { FieldComments } from '../../components/FieldComments';

/**
 * Single-select bound to a controlled vocabulary from
 * /api/zirc/vocabulary/{name}, named by `options.vocabulary`.
 *
 * Stores the term's ZDB ID, not its display name, so a curator renaming a
 * term doesn't orphan saved values — which is also why view mode resolves
 * through the vocabulary rather than printing the stored value: the raw form
 * of this field is an id no curator should have to read.
 */
function VocabularySelectRenderer({
    data, handleChange, path, label, required, visible, uischema, config,
}: ControlProps) {
    const opts = ((uischema as { options?: { vocabulary?: VocabularyName } } | undefined)?.options) ?? {};
    // Hook order can't depend on `visible`; bail after it. The query is
    // shared and cached across every card on the page, so an extra
    // subscription from a hidden control costs nothing.
    const { data: terms, isLoading, isError } = useVocabulary(
        opts.vocabulary ?? 'amino_acid_term',
    );

    if (visible === false) {return null;}

    const fieldName = leafOf(path);
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const view = viewConfigFrom(config);
    const value = (data as string | undefined) ?? '';

    if (view.readonly) {
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    {value === ''
                        ? <span className='text-muted'>&mdash;</span>
                        : labelById(terms, value)}
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
                <label htmlFor={inputId} className='mb-0'>
                    {label}{required ? ' *' : ''}
                </label>
            </th>
            <td>
                <select
                    id={inputId}
                    className='form-control'
                    style={{ maxWidth: 360 }}
                    value={value}
                    disabled={isLoading || isError}
                    onChange={(e) => handleChange(path, e.target.value)}
                >
                    <option value=''>
                        {isLoading ? 'Loading…' : isError ? 'Unavailable' : '----'}
                    </option>
                    {/* A stored value the vocabulary no longer offers still
                        needs an <option> or the select would silently show
                        the placeholder while the field holds a value. */}
                    {value !== '' && !terms?.some((t) => t.id === value) && !isLoading && (
                        <option value={value}>{value} (unrecognized)</option>
                    )}
                    {(terms ?? []).map((t) => (
                        <option key={t.id} value={t.id}>{termLabel(t)}</option>
                    ))}
                </select>
                {isError && (
                    <small className='text-danger'>
                        Could not load the {opts.vocabulary} list.
                    </small>
                )}
            </td>
        </tr>
    );
}

export const vocabularySelectRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'vocabularySelect'))),
    renderer: withJsonFormsControlProps(VocabularySelectRenderer),
};
