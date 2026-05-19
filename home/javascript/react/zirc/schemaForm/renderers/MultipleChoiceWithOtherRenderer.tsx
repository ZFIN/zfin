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

type ReasonsObject = {
    reasons?: string[];
    reasonsOther?: string | null;
};

type ReasonChoice = { const: string; title: string };

/**
 * Acceptance-reasons widget: claims the parent object scope (the Control
 * pointing at `acceptance`) and renders both children as a single coordinated
 * UI — checkbox group for the canonical reasons, plus a free-text "Describe"
 * input revealed when the special "other" value is selected.
 *
 * Canonical reasons come from the schema's `properties.reasons.items.oneOf`,
 * so adding/removing options is a server-side schema change only.
 */
function MultipleChoiceWithOtherRenderer({
    data,
    handleChange,
    path,
    schema,
    uischema,
    visible,
}: ControlProps) {
    if (visible === false) {return null;}
    const obj: ReasonsObject = (data as ReasonsObject) ?? {};
    const reasons = obj.reasons ?? [];
    const reasonsOther = obj.reasonsOther ?? '';
    const options = (uischema as { options?: { label?: string; otherValue?: string } })?.options ?? {};
    const labelText = options.label ?? 'Reasons';
    const otherValue = options.otherValue ?? 'other';
    const showOther = reasons.includes(otherValue);

    const choices: ReasonChoice[] =
        ((schema as { properties?: { reasons?: { items?: { oneOf?: ReasonChoice[] } } } })
            .properties?.reasons?.items?.oneOf) ?? [];

    const toggle = (value: string, checked: boolean) => {
        const next = checked ? [...reasons, value] : reasons.filter((r) => r !== value);
        handleChange(path, { ...obj, reasons: next });
    };

    const updateOther = (next: string) => {
        handleChange(path, { ...obj, reasonsOther: next });
    };

    return (
        <tr>
            <th className='w-25' scope='row' id='fr-label-reasons'>
                <label htmlFor='fr-reasons' className='mb-0'>{labelText}</label>
            </th>
            <td>
                <fieldset className='border-0 p-0 m-0' aria-labelledby='fr-label-reasons'>
                    {choices.map((c) => (
                        <div key={c.const} className='form-check'>
                            <input
                                type='checkbox'
                                id={`fr-reasons-${c.const}`}
                                className='form-check-input'
                                value={c.const}
                                checked={reasons.includes(c.const)}
                                onChange={(e) => toggle(c.const, e.target.checked)}
                            />
                            <label
                                className='form-check-label'
                                htmlFor={`fr-reasons-${c.const}`}
                            >
                                {c.title}
                            </label>
                        </div>
                    ))}
                    {showOther && (
                        <div className='mt-2 ml-4' style={{ maxWidth: 600 }}>
                            <label htmlFor='fr-reasons-other-text' className='sr-only'>
                                {labelText} (other details)
                            </label>
                            <input
                                type='text'
                                id='fr-reasons-other-text'
                                className='form-control'
                                placeholder='Describe'
                                value={reasonsOther}
                                onChange={(e) => updateOther(e.target.value)}
                            />
                        </div>
                    )}
                </fieldset>
            </td>
        </tr>
    );
}

export const multipleChoiceWithOtherRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'multipleChoiceWithOther'))),
    renderer: withJsonFormsControlProps(MultipleChoiceWithOtherRenderer),
};
