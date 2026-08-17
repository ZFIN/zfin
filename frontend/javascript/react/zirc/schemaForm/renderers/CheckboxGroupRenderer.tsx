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

type CheckboxGroupOptions = {
    standardValues?: string[];
    standardLabels?: string[];
    /** A value that cannot coexist with any other, e.g. "unknown". */
    exclusiveValue?: string;
    helpText?: string;
};

/**
 * Check-all-that-apply over a fixed list, writing a string array.
 *
 * Checkboxes rather than a multi-select because "which of these apply" is
 * what a checkbox group says without explanation, while a multi-select
 * requires knowing to ctrl-click and loses the lot to a stray click.
 *
 * Deliberately does not handle an "other" free-text itself, unlike
 * MultipleChoiceWithOtherRenderer. The follow-up text is an ordinary field
 * revealed by a uiSchema rule on this array, which means every ticked box
 * reveals its own follow-up through one mechanism instead of one box being
 * special-cased in here.
 *
 * Overlaps with MultipleChoiceWithOtherRenderer, which predates it and is
 * bound to the acceptance object's fixed `reasons` / `reasonsOther` keys.
 * This one is the general shape; that one could fold into it later.
 */
function CheckboxGroupRenderer({
    data, handleChange, path, label, visible, uischema, config,
}: ControlProps) {
    if (visible === false) {return null;}

    const fieldName = leafOf(path);
    const labelId = `fr-label-${fieldName}`;
    const opts = ((uischema as { options?: CheckboxGroupOptions } | undefined)?.options) ?? {};
    const values = opts.standardValues ?? [];
    const labels = opts.standardLabels ?? null;
    const exclusive = opts.exclusiveValue;
    const selected = Array.isArray(data) ? (data as string[]) : [];
    const view = viewConfigFrom(config);

    const labelFor = (v: string, i: number) =>
        labels && labels[i] != null ? labels[i] : v;

    if (view.readonly) {
        const shown = selected.map((v) => labelFor(v, values.indexOf(v)));
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    {shown.length === 0
                        ? <span className='text-muted'>&mdash;</span>
                        : shown.join(', ')}
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

    const toggle = (value: string, checked: boolean) => {
        if (!checked) {
            handleChange(path, selected.filter((s) => s !== value));
            return;
        }
        // The exclusive value contradicts every specific one, so ticking it
        // clears the rest and ticking anything else clears it. Without this
        // a lesion could claim both "unknown" and "CRISPR".
        if (exclusive && value === exclusive) {
            handleChange(path, [exclusive]);
            return;
        }
        const kept = exclusive ? selected.filter((s) => s !== exclusive) : selected;
        // Ordered by the canonical list rather than by click order, so the
        // stored array reads the same however it was filled in.
        const next = [...kept, value];
        handleChange(path, values.filter((v) => next.includes(v)));
    };

    return (
        <tr>
            <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                {label}
            </th>
            <td>
                <fieldset className='border-0 p-0 m-0' aria-labelledby={labelId}>
                    {values.map((v, i) => {
                        const id = `fr-${fieldName}-${v}`;
                        return (
                            <div className='form-check' key={v}>
                                <input
                                    type='checkbox'
                                    id={id}
                                    className='form-check-input'
                                    value={v}
                                    checked={selected.includes(v)}
                                    onChange={(e) => toggle(v, e.target.checked)}
                                />
                                <label className='form-check-label' htmlFor={id}>
                                    {labelFor(v, i)}
                                </label>
                            </div>
                        );
                    })}
                </fieldset>
                {opts.helpText && (
                    <small className='form-text text-muted'>{opts.helpText}</small>
                )}
            </td>
        </tr>
    );
}

export const checkboxGroupRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'checkboxGroup'))),
    renderer: withJsonFormsControlProps(CheckboxGroupRenderer),
};
