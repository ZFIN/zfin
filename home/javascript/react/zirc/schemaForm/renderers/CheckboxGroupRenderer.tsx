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
 * Multi-select pick list over a {@code string[]} scope: a checkbox per
 * canonical value, plus an optional "Other" checkbox that reveals a
 * free-text input. The selected canonical values and the (single) Other
 * free-text entry are stored together in the underlying array, so the
 * widget is a drop-in replacement for the free-text {@code stringList}
 * on array-typed fields.
 *
 * Recognized {@code options}:
 * <ul>
 *   <li>{@code standardValues} — canonical values; each renders a checkbox.</li>
 *   <li>{@code standardLabels} — parallel display labels (falls back to value).</li>
 *   <li>{@code noOther} — when true, suppress the "Other" checkbox + input
 *     (closed enum, e.g. segregation).</li>
 * </ul>
 *
 * Used by Phenotype's segregation (noOther) and type (Other allowed) —
 * ZFIN-10348 / ZFIN-10349. Rendered by any uiSchema Control with
 * {@code options.widget = "checkboxGroup"}.
 */
type CheckboxGroupOptions = {
    standardValues?: string[];
    standardLabels?: string[];
    noOther?: boolean;
};

function CheckboxGroupRenderer({
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
    const items: string[] = Array.isArray(data) ? (data as string[]) : [];

    const options = ((uischema as { options?: CheckboxGroupOptions } | undefined)?.options) ?? {};
    const standardValues = options.standardValues ?? [];
    const standardLabels = options.standardLabels ?? null;
    const noOther = options.noOther === true;
    const labelFor = (v: string, i: number) =>
        standardLabels && standardLabels[i] != null ? standardLabels[i] : v;

    const selectedStandard = items.filter((v) => standardValues.includes(v));
    // At most one non-standard entry is treated as the Other free text.
    const otherValue = items.find((v) => !standardValues.includes(v)) ?? '';
    const [otherSelected, setOtherSelected] = React.useState(
        () => !noOther && otherValue !== '',
    );

    const view = viewConfigFrom(config);

    if (view.readonly) {
        const titles = standardValues
            .filter((v) => selectedStandard.includes(v))
            .map((v) => labelFor(v, standardValues.indexOf(v)));
        if (otherValue) {titles.push(`Other: ${otherValue}`);}
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    {titles.length === 0
                        ? <span className='text-muted'>&mdash;</span>
                        : (
                            <ul className='list-unstyled mb-0'>
                                {titles.map((t, i) => <li key={i}>{t}</li>)}
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

    // Rebuild the array as [canonical values in standard order, Other text].
    // The Other entry is preserved across canonical toggles and dropped
    // when its checkbox is cleared.
    const commit = (nextStandard: Set<string>, nextOther: string) => {
        const std = standardValues.filter((v) => nextStandard.has(v));
        const next = nextOther ? [...std, nextOther] : std;
        handleChange(path, next);
    };

    const toggleStandard = (value: string, checked: boolean) => {
        const set = new Set(selectedStandard);
        if (checked) {set.add(value);} else {set.delete(value);}
        commit(set, otherSelected ? otherValue : '');
    };

    const toggleOther = (checked: boolean) => {
        setOtherSelected(checked);
        commit(new Set(selectedStandard), checked ? otherValue : '');
    };

    const updateOther = (next: string) => {
        commit(new Set(selectedStandard), next);
    };

    return (
        <tr>
            <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                {label}
            </th>
            <td>
                <fieldset className='border-0 p-0 m-0' aria-labelledby={labelId}>
                    {standardValues.map((v, i) => (
                        <div key={v} className='form-check'>
                            <input
                                type='checkbox'
                                id={`fr-${fieldName}-${i}`}
                                className='form-check-input'
                                value={v}
                                checked={selectedStandard.includes(v)}
                                onChange={(e) => toggleStandard(v, e.target.checked)}
                            />
                            <label
                                className='form-check-label'
                                htmlFor={`fr-${fieldName}-${i}`}
                            >
                                {labelFor(v, i)}
                            </label>
                        </div>
                    ))}
                    {!noOther && (
                        <div className='form-check'>
                            <input
                                type='checkbox'
                                id={`fr-${fieldName}-other`}
                                className='form-check-input'
                                checked={otherSelected}
                                onChange={(e) => toggleOther(e.target.checked)}
                            />
                            <label
                                className='form-check-label'
                                htmlFor={`fr-${fieldName}-other`}
                            >
                                Other
                            </label>
                        </div>
                    )}
                    {!noOther && otherSelected && (
                        <div className='mt-2 ml-4' style={{ maxWidth: 600 }}>
                            <label htmlFor={`fr-${fieldName}-other-text`} className='sr-only'>
                                {label} (other details)
                            </label>
                            <input
                                type='text'
                                id={`fr-${fieldName}-other-text`}
                                className='form-control'
                                placeholder='Please specify'
                                aria-label={`${label} (other)`}
                                value={otherValue}
                                onChange={(e) => updateOther(e.target.value)}
                            />
                        </div>
                    )}
                </fieldset>
            </td>
        </tr>
    );
}

export const checkboxGroupRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'checkboxGroup'))),
    renderer: withJsonFormsControlProps(CheckboxGroupRenderer),
};
