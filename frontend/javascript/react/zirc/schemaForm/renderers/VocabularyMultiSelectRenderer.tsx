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

type MultiSelectOptions = {
    vocabulary?: VocabularyName;
    addLabel?: string;
    helpText?: string;
};

/**
 * Multi-select over a controlled vocabulary: a picker that adds one term at
 * a time, plus removable chips for what's chosen. Stores an array of term
 * ZDB IDs.
 *
 * Add-one-at-a-time rather than a native multiple-select or a checkbox
 * column because these lists run to seventeen entries (transcript
 * consequences) and a lesion normally carries one or two. A `<select
 * multiple>` also loses selections to a stray click, with no undo.
 *
 * The whole array is one PATCH: diffLeaves treats arrays as atomic leaves,
 * the same way the previousNames stringList behaves.
 */
function VocabularyMultiSelectRenderer({
    data, handleChange, path, label, visible, uischema, config,
}: ControlProps) {
    const opts = ((uischema as { options?: MultiSelectOptions } | undefined)?.options) ?? {};
    const { data: terms, isLoading, isError } = useVocabulary(
        opts.vocabulary ?? 'transcript_consequence_term',
    );
    const [pending, setPending] = React.useState('');

    if (visible === false) {return null;}

    const fieldName = leafOf(path);
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const view = viewConfigFrom(config);
    const selected = Array.isArray(data) ? (data as string[]) : [];

    if (view.readonly) {
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    {selected.length === 0
                        ? <span className='text-muted'>&mdash;</span>
                        : selected.map((id) => labelById(terms, id)).join(', ')}
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

    // Offering an already-chosen term would either duplicate it or no-op;
    // hiding it also shrinks a long list as the curator works through it.
    const available = (terms ?? []).filter((t) => !selected.includes(t.id));

    const add = () => {
        if (pending === '' || selected.includes(pending)) {return;}
        handleChange(path, [...selected, pending]);
        setPending('');
    };

    const remove = (id: string) => {
        handleChange(path, selected.filter((s) => s !== id));
    };

    return (
        <tr>
            <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                <label htmlFor={inputId} className='mb-0'>{label}</label>
            </th>
            <td>
                {selected.length > 0 && (
                    <ul className='list-unstyled mb-2'>
                        {selected.map((id) => (
                            <li key={id} className='d-flex align-items-center' style={{ gap: 8 }}>
                                <span>{labelById(terms, id)}</span>
                                <button
                                    type='button'
                                    className='btn btn-link btn-sm p-0 text-danger'
                                    onClick={() => remove(id)}
                                    aria-label={`Remove ${labelById(terms, id)}`}
                                >
                                    remove
                                </button>
                            </li>
                        ))}
                    </ul>
                )}
                <div className='d-flex' style={{ gap: 8, maxWidth: '40em' }}>
                    <select
                        id={inputId}
                        className='form-control'
                        style={{ maxWidth: 360 }}
                        value={pending}
                        disabled={isLoading || isError}
                        onChange={(e) => setPending(e.target.value)}
                    >
                        <option value=''>
                            {isLoading ? 'Loading…' : isError ? 'Unavailable' : '----'}
                        </option>
                        {available.map((t) => (
                            <option key={t.id} value={t.id}>{termLabel(t)}</option>
                        ))}
                    </select>
                    <button
                        type='button'
                        className='btn btn-outline-secondary btn-sm text-nowrap'
                        onClick={add}
                        disabled={pending === ''}
                    >
                        {opts.addLabel ?? '+ Add'}
                    </button>
                </div>
                {isError && (
                    <small className='text-danger'>
                        Could not load the {opts.vocabulary} list.
                    </small>
                )}
                {opts.helpText && (
                    <small className='form-text text-muted'>{opts.helpText}</small>
                )}
            </td>
        </tr>
    );
}

export const vocabularyMultiSelectRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'vocabularyMultiSelect'))),
    renderer: withJsonFormsControlProps(VocabularyMultiSelectRenderer),
};
