import * as React from 'react';
import {
    and,
    ControlProps,
    isControl,
    JsonFormsRendererRegistryEntry,
    optionIs,
    rankWith,
} from '@jsonforms/core';
import { useJsonForms, withJsonFormsControlProps } from '@jsonforms/react';
import { viewConfigFrom, leafOf, commentsEnabled } from '../useViewConfig';
import { labelById, termLabel } from '../vocabularyHelpers';
import { useVocabulary, VocabularyName } from '../../api/queries';
import { StatusBadge } from '../../components/StatusBadge';
import { FieldHistory } from '../../components/FieldHistory';
import { FieldComments } from '../../components/FieldComments';

type AminoAcidChangeOptions = {
    vocabulary?: VocabularyName;
    toField?: string;
    positionField?: string;
    /** Optional; omit for a single position box rather than a range. */
    positionEndField?: string;
};

/**
 * The curation interface's amino-acid change control: `from > to` residue
 * selects plus a position, replacing the free-text box that asked curators
 * to hand-write HGVS.
 *
 * Bound to the "from" field and writing two named siblings — the
 * PhenotypeTimingRenderer arrangement — rather than claiming a nested
 * object. The lesion schema is flat, and nesting these three would push
 * `from` / `to` / `position` into LesionDTO as component names (the
 * invariants test matches on leaf segments) where they'd read as belonging
 * to nothing.
 *
 * Sibling names come from `options.toField` / `options.positionField` rather
 * than being derived from the bound path, so field names stay declared in
 * one place: the schema.
 */
function AminoAcidChangeRenderer({
    data, handleChange, path, label, visible, uischema, config,
}: ControlProps) {
    const ctx = useJsonForms();
    const opts = ((uischema as { options?: AminoAcidChangeOptions } | undefined)?.options) ?? {};
    const { data: terms, isLoading, isError } = useVocabulary(
        opts.vocabulary ?? 'amino_acid_term',
    );

    if (visible === false) {return null;}

    const fieldName = leafOf(path);
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const view = viewConfigFrom(config);

    const toField = opts.toField;
    const positionField = opts.positionField;
    const positionEndField = opts.positionEndField;
    // Bound path is e.g. "aaChangeFrom"; siblings sit alongside it under the
    // same parent, so swap the last segment.
    const siblingPath = (name: string) =>
        path.includes('.') ? `${path.slice(0, path.lastIndexOf('.') + 1)}${name}` : name;

    const root = (ctx.core?.data ?? {}) as Record<string, unknown>;
    const from = (data as string | undefined) ?? '';
    const to = toField ? ((root[toField] as string | undefined) ?? '') : '';
    const position = positionField
        ? ((root[positionField] as number | null | undefined) ?? null)
        : null;
    const positionEnd = positionEndField
        ? ((root[positionEndField] as number | null | undefined) ?? null)
        : null;

    if (view.readonly) {
        const parts = [
            from === '' ? null : labelById(terms, from),
            to === '' ? null : labelById(terms, to),
        ];
        // "at 12" for a single residue, "at 12–15" for a range. An end with
        // no start would be meaningless, so it only shows alongside one.
        const where = position == null
            ? ''
            : ` at ${position}${positionEnd == null ? '' : `\u2013${positionEnd}`}`;
        const display = parts[0] || parts[1]
            ? `${parts[0] ?? '?'} > ${parts[1] ?? '?'}${where}`
            : null;
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    {display ?? <span className='text-muted'>&mdash;</span>}
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

    const residueSelect = (
        id: string,
        aria: string,
        value: string,
        onPick: (v: string) => void,
    ) => (
        <select
            id={id}
            className='form-control'
            style={{ maxWidth: 140 }}
            aria-label={aria}
            value={value}
            disabled={isLoading || isError}
            onChange={(e) => onPick(e.target.value)}
        >
            <option value=''>{isLoading ? 'Loading…' : isError ? 'Unavailable' : '----'}</option>
            {(terms ?? []).map((t) => (
                <option key={t.id} value={t.id}>{termLabel(t)}</option>
            ))}
        </select>
    );

    const positionInput = (
        id: string,
        aria: string,
        value: number | null,
        field: string | undefined,
    ) => (
        <input
            id={id}
            type='number'
            min={1}
            step={1}
            className='form-control'
            style={{ maxWidth: 120 }}
            aria-label={aria}
            value={value == null ? '' : String(value)}
            onChange={(e) => {
                if (!field) {return;}
                const raw = e.target.value;
                const n = raw === '' ? null : Number(raw);
                handleChange(siblingPath(field), n !== null && Number.isFinite(n) ? n : null);
            }}
        />
    );

    return (
        <tr>
            <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                <label htmlFor={inputId} className='mb-0'>{label}</label>
            </th>
            <td>
                <div className='d-flex align-items-center' style={{ gap: 8 }}>
                    {residueSelect(inputId, `${label} from`, from,
                        (v) => handleChange(path, v))}
                    <span aria-hidden='true'>&gt;</span>
                    {residueSelect(`${inputId}-to`, `${label} to`, to,
                        (v) => toField && handleChange(siblingPath(toField), v))}
                    <label htmlFor={`${inputId}-position`} className='mb-0 ml-2'>Position</label>
                    {positionInput(`${inputId}-position`, `${label} position`, position, positionField)}
                    {positionEndField && (
                        <>
                            <span aria-hidden='true'>&ndash;</span>
                            {positionInput(
                                `${inputId}-position-end`,
                                `${label} position end`,
                                positionEnd,
                                positionEndField,
                            )}
                        </>
                    )}
                </div>
                {isError && (
                    <small className='text-danger'>
                        Could not load the amino-acid list.
                    </small>
                )}
            </td>
        </tr>
    );
}

export const aminoAcidChangeRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'aminoAcidChange'))),
    renderer: withJsonFormsControlProps(AminoAcidChangeRenderer),
};
