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
import { ValueDisplay } from '../../components/ValueDisplay';

const OTHER_SENTINEL = '__other';

/**
 * Generic "select from a standard list, or type Other" widget. The canonical
 * values come from uiSchema `options.standardValues`; the value the server
 * receives is either one of those strings or the user's free-text override.
 *
 * "Other selected" is tracked as local UI state so picking Other on an empty
 * field doesn't immediately hide the revealed input again.
 *
 * Used by Background's maternal/paternal strain selects, Mutagenesis's
 * protocol select, mutation type, and the phenotype segregation/type fields.
 *
 * TODO (optional cleanup): this is now the only select widget —
 * Consider renaming the widget "selectWithOther" -> plain "select" (the noOther
 * option already covers the closed-list case). Looks weird to have selectWithOther({noOther: true})
 */
function SelectWithOtherRenderer({ data, handleChange, path, label, uischema, visible, config }: ControlProps) {
    if (visible === false) {return null;}

    const fieldName = leafOf(path);
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const options = (uischema as {
        options?: {
            standardValues?: string[];
            standardLabels?: string[];
            noOther?: boolean;
        };
    } | undefined)?.options ?? {};
    const standardValues = options.standardValues ?? [];
    const standardLabels = options.standardLabels ?? null;
    const noOther = options.noOther === true;
    const labelFor = (v: string, i: number) =>
        standardLabels && standardLabels[i] != null ? standardLabels[i] : v;
    const value = (data as string | undefined) ?? '';
    const isStandard = standardValues.includes(value);
    const [otherSelected, setOtherSelected] = React.useState(
        () => !noOther && value !== '' && !isStandard,
    );
    const selectValue = isStandard ? value : (otherSelected ? OTHER_SENTINEL : '');
    const view = viewConfigFrom(config);

    if (view.readonly) {
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    <ValueDisplay value={data}/>
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
                <label htmlFor={inputId} className='mb-0'>{label}</label>
            </th>
            <td>
                <div className='d-flex' style={{ gap: 8 }}>
                    <select
                        id={inputId}
                        className='form-control'
                        style={{ maxWidth: 240 }}
                        value={selectValue}
                        onChange={(e) => {
                            const v = e.target.value;
                            if (v === OTHER_SENTINEL) {
                                setOtherSelected(true);
                                if (isStandard) {handleChange(path, '');}
                            } else {
                                setOtherSelected(false);
                                handleChange(path, v);
                            }
                        }}
                    >
                        <option value=''>(select)</option>
                        {standardValues.map((s, i) => (
                            <option key={s} value={s}>{labelFor(s, i)}</option>
                        ))}
                        {!noOther && (
                            <option value={OTHER_SENTINEL}>Other</option>
                        )}
                    </select>
                    {!noOther && otherSelected && (
                        <input
                            type='text'
                            id={`${inputId}-other`}
                            className='form-control'
                            placeholder='Other'
                            aria-label={`${label} (other)`}
                            value={value}
                            onChange={(e) => handleChange(path, e.target.value)}
                        />
                    )}
                </div>
            </td>
        </tr>
    );
}

export const selectWithOtherRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'selectWithOther'))),
    renderer: withJsonFormsControlProps(SelectWithOtherRenderer),
};
