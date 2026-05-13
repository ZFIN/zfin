// Schema-driven form renderer. Walks a FormNode tree (see types.ts) and
// produces JSX. Containers stay out of the rendering business; their job
// shrinks to load the DTO, run the renderer, and dispatch commits to the
// server. See builders.ts for schema-construction helpers and
// useAutosavedForm.ts for the canonical container plumbing.
//
// This file covers the basic field types and sections only. ArrayNode and
// the composite field types (select-with-other, multi-checkbox-with-other,
// number-with-unit) land in follow-up commits.

import React, {useEffect, useRef, useState} from 'react';
import Autocomplete from './Autocomplete';
import {
    ArrayNode,
    CustomNode,
    FieldNode,
    FormNode,
    OptionsCtx,
    OptionsSource,
    SectionNode,
    SelectOption,
    Visible,
} from './types';

// ─── Public component API ─────────────────────────────────────────────────

export interface FormRendererProps {
    schema: FormNode[];
    /** Full form DTO. The renderer reads through paths but never mutates. */
    value: unknown;
    /**
     * Fires on every keystroke / radio click / select change. The parent
     * patches the named path into its DTO state. Field-level onChange is
     * cheap and high-frequency; do not save here.
     */
    onChange: (path: string, next: unknown) => void;
    /**
     * Fires on blur (text/textarea), on pick (autocomplete), or immediately
     * for select/radio (one-click choices). The parent typically enqueues a
     * server save here.
     */
    onCommit: (path: string, next: unknown) => void;
    /**
     * Container-supplied callbacks for non-schema actions reached by
     * CustomNode renderers (e.g. removeMutation, applyDTO).
     */
    actions?: Record<string, (...args: unknown[]) => unknown>;
}

const FormRenderer = ({schema, value, onChange, onCommit, actions = {}}: FormRendererProps) => {
    const ctx: RenderCtx = {dto: value, row: undefined, onChange, onCommit, actions};
    return <>{schema.map(node => renderNode(node, ctx))}</>;
};

export default FormRenderer;

// ─── Internal rendering machinery ─────────────────────────────────────────

interface RenderCtx {
    dto: unknown;
    /** Set when rendering inside an ArrayNode's childTemplate. */
    row?: unknown;
    onChange: (path: string, next: unknown) => void;
    onCommit: (path: string, next: unknown) => void;
    actions: Record<string, (...args: unknown[]) => unknown>;
    /**
     * True when the next renderNode call will land as a direct child of
     * <tbody>. CustomNodes use this to wrap their output in
     * <tr><td colSpan={2}> so they don't produce invalid DOM nesting.
     * FieldNodes ignore it -- they always render <tr> rows.
     */
    inTableRow?: boolean;
}

function renderNode(node: FormNode, ctx: RenderCtx): React.ReactNode {
    if (!evalVisible(node.visible, ctx)) {
        return null;
    }
    switch (node.kind) {
    case 'section':
        return <SectionView key={node.id} node={node} ctx={ctx}/>;
    case 'field':
        return <FieldView key={fieldKey(node)} node={node} ctx={ctx}/>;
    case 'custom':
        return <CustomView key={node.id} node={node} ctx={ctx}/>;
    case 'array':
        return <ArrayView key={node.id} node={node} ctx={ctx}/>;
    }
}

function fieldKey(node: FieldNode): string {
    // Two FieldNodes with the same path can co-exist when one is gated by
    // `visible` (e.g. autocomplete + free-text variants of the same column).
    // Include the type so React keys stay distinct.
    return `${node.path}|${node.type}`;
}

function evalVisible(pred: Visible | undefined, ctx: RenderCtx): boolean {
    return pred ? pred(ctx.dto, ctx.row) : true;
}

// ─── Section ──────────────────────────────────────────────────────────────

const SectionView = ({node, ctx}: {node: SectionNode; ctx: RenderCtx}) => {
    const tableCtx: RenderCtx = {...ctx, inTableRow: true};
    return (
        <section className='section' id={node.id} aria-labelledby={`${node.id}-heading`}>
            <h2 id={`${node.id}-heading`} className='heading'>{node.title}</h2>
            {hasTableChildren(node) ? (
                <table className='table table-borderless'>
                    <tbody>{node.children.map(child => renderNode(child, tableCtx))}</tbody>
                </table>
            ) : (
                node.children.map(child => renderNode(child, ctx))
            )}
        </section>
    );
};

/**
 * Field rows render as <tr><th><td>; sections containing field children get
 * wrapped in a <table>. Sections whose children are arrays / sub-sections
 * skip the wrapper.
 */
function hasTableChildren(node: SectionNode): boolean {
    return node.children.some(c => c.kind === 'field');
}

// ─── Field ────────────────────────────────────────────────────────────────

const FieldView = ({node, ctx}: {node: FieldNode; ctx: RenderCtx}) => {
    const scope = ctx.row ?? ctx.dto;
    const rawValue = readPath(scope, node.path);
    const inputId = `fr-${node.path}`;
    const labelId = `fr-label-${node.path}`;

    const emit = (raw: string, commit: boolean) => {
        const normalized = normalizeForWire(raw, node.type);
        ctx.onChange(node.path, normalized);
        if (commit) {
            ctx.onCommit(node.path, normalized);
        }
    };

    const headerless = node.type === 'bool' || node.type === 'readonly';
    return (
        <tr>
            <th className='w-25' scope='row' id={labelId}>
                {headerless
                    ? <FieldLabel label={node.label} infoHref={node.infoHref}/>
                    : <label htmlFor={inputId} className='mb-0'>
                        <FieldLabel label={node.label} infoHref={node.infoHref}/>
                    </label>}
            </th>
            <td>
                <FieldInput
                    node={node}
                    ctx={ctx}
                    inputId={inputId}
                    labelId={labelId}
                    rawValue={rawValue}
                    emit={emit}
                />
                {node.helpText && <small className='form-text text-muted'>{node.helpText}</small>}
            </td>
        </tr>
    );
};

const FieldLabel = ({label, infoHref}: {label: string; infoHref?: string}) => (
    <>
        {label}
        {infoHref && (
            <a
                href={infoHref}
                target='_blank'
                rel='noopener noreferrer'
                className='text-info small ml-1'
                title={`More about ${label}`}
                style={{textDecoration: 'none'}}
            >
                (i)
            </a>
        )}
    </>
);

interface FieldInputProps {
    node: FieldNode;
    ctx: RenderCtx;
    inputId: string;
    labelId: string;
    rawValue: unknown;
    emit: (raw: string, commit: boolean) => void;
}

const FieldInput = ({node, ctx, inputId, labelId, rawValue, emit}: FieldInputProps) => {
    const value = valueAsInputString(rawValue);
    switch (node.type) {
    case 'textarea':
        return (
            <textarea
                id={inputId}
                className='form-control'
                rows={3}
                placeholder={node.placeholder}
                value={value}
                onChange={e => emit(e.target.value, false)}
                onBlur={e => emit(e.target.value, true)}
            />
        );
    case 'autocomplete':
        return (
            <Autocomplete
                id={inputId}
                value={value}
                placeholder={node.placeholder}
                fetchUrl={node.autocompleteUrl ?? ''}
                onChange={next => emit(next, false)}
                onCommit={next => emit(next, true)}
            />
        );
    case 'select': {
        const options = resolveOptions(node.options, ctx);
        return (
            <select
                id={inputId}
                className='form-control'
                value={value}
                onChange={e => emit(e.target.value, true)}
            >
                <option value=''>(select)</option>
                {options.map(opt => (
                    <option key={String(opt.value)} value={String(opt.value)}>{opt.label}</option>
                ))}
            </select>
        );
    }
    case 'bool': {
        const options: Array<[string, string]> = [['true', 'Yes'], ['false', 'No']];
        const groupName = `${inputId}-group`;
        return (
            <div role='radiogroup' aria-labelledby={labelId}>
                {options.map(([v, lbl]) => {
                    const radioId = `${inputId}-${v}`;
                    return (
                        <div className='form-check form-check-inline' key={v}>
                            <input
                                type='radio'
                                id={radioId}
                                className='form-check-input'
                                name={groupName}
                                value={v}
                                checked={value === v}
                                onChange={() => emit(v, true)}
                            />
                            <label className='form-check-label' htmlFor={radioId}>{lbl}</label>
                        </div>
                    );
                })}
            </div>
        );
    }
    case 'int':
    case 'float':
        return (
            <div className='d-flex align-items-center' style={{gap: 6}}>
                <input
                    id={inputId}
                    type='number'
                    step={node.type === 'int' ? '1' : 'any'}
                    className='form-control'
                    style={{maxWidth: 200}}
                    placeholder={node.placeholder}
                    value={value}
                    onChange={e => emit(e.target.value, false)}
                    onBlur={e => emit(e.target.value, true)}
                />
                {node.suffix && <span className='text-muted small'>{node.suffix}</span>}
            </div>
        );
    case 'checkbox':
        return (
            <div className='form-check'>
                <input
                    type='checkbox'
                    id={inputId}
                    className='form-check-input'
                    checked={value === 'true'}
                    onChange={e => emit(e.target.checked ? 'true' : 'false', true)}
                />
            </div>
        );
    case 'readonly':
        return value === ''
            ? <span className='text-muted small'>{node.placeholder ?? ''}</span>
            : <code>{value}</code>;
    case 'text':
        return (
            <input
                id={inputId}
                type='text'
                className='form-control'
                placeholder={node.placeholder}
                value={value}
                onChange={e => emit(e.target.value, false)}
                onBlur={e => emit(e.target.value, true)}
            />
        );
    case 'select-with-other':
        return <SelectWithOtherInput node={node} ctx={ctx} inputId={inputId}/>;
    case 'multi-checkbox-with-other':
        return <MultiCheckboxWithOtherInput node={node} ctx={ctx} inputId={inputId} labelId={labelId}/>;
    }
};

// ─── Composite field types ────────────────────────────────────────────────

interface CompositeProps {
    node: FieldNode;
    ctx: RenderCtx;
    inputId: string;
    labelId?: string;
}

/**
 * Dropdown of canonical options plus an "Other" affordance that reveals a
 * free-text input. Two modes, picked by whether `otherPath` is defined:
 *
 *  - Single-column (otherPath undefined): canonical picks write to `path`;
 *    picking Other reveals a text input that also writes to `path`. Used
 *    when the column accepts any string and the canonical list is a hint
 *    (e.g. ZIRC's maternal/paternal background).
 *
 *  - Two-column (otherPath defined): canonical picks write to `path` and
 *    clear `otherPath`; picking the "Other" option (must have value
 *    'Other') writes 'Other' to `path` and reveals a text input that
 *    writes to `otherPath`. Used when the wire keeps a separate column
 *    for the free-text (e.g. mutagenesisProtocol + mutagenesisProtocolOther).
 */
const SelectWithOtherInput = ({node, ctx, inputId}: CompositeProps) => {
    const scope = ctx.row ?? ctx.dto;
    const options = resolveOptions(node.options, ctx);
    const otherPath = node.otherPath;

    const pathValue = valueAsInputString(readPath(scope, node.path));
    const otherValue = otherPath ? valueAsInputString(readPath(scope, otherPath)) : '';

    const canonicalValues = new Set(options.map(o => String(o.value)));
    const isCanonical = pathValue !== '' && canonicalValues.has(pathValue);

    // In two-column mode, "Other" is just another canonical value (the
    // schema must include it in options; the sentinel defaults to
    // 'Other' but is overridable via node.otherValue for wire enums
    // that use a different token). In single-column mode, Other is
    // purely a UI affordance gated by local state plus value-shape
    // heuristics.
    const otherSentinel = node.otherValue ?? 'Other';
    const [otherPicked, setOtherPicked] = React.useState<boolean>(false);
    const otherMode = otherPath
        ? pathValue === otherSentinel
        : otherPicked || (pathValue !== '' && !isCanonical);

    const selectValue = otherMode
        ? (otherPath ? otherSentinel : '__other')
        : (isCanonical ? pathValue : '');

    const otherInputId = `${inputId}-other`;

    function handleSelectChange(picked: string) {
        if (picked === '__other') {
            // Single-column: revealing the text input, no save yet.
            setOtherPicked(true);
            if (isCanonical) {
                // Clear so the text input starts empty rather than echoing canonical.
                ctx.onChange(node.path, null);
                ctx.onCommit(node.path, null);
            }
            return;
        }
        setOtherPicked(false);
        if (otherPath && picked !== otherSentinel) {
            // Two-column: clear the free-text column when picking canonical.
            ctx.onChange(otherPath, null);
            ctx.onCommit(otherPath, null);
        }
        const wire = normalizeForWire(picked, 'text');
        ctx.onChange(node.path, wire);
        ctx.onCommit(node.path, wire);
    }

    function handleOtherChange(raw: string, commit: boolean) {
        const targetPath = otherPath ?? node.path;
        const wire = normalizeForWire(raw, 'text');
        ctx.onChange(targetPath, wire);
        if (commit) {
            ctx.onCommit(targetPath, wire);
        }
    }

    return (
        <div className='d-flex' style={{gap: 8}}>
            <select
                id={inputId}
                className='form-control'
                value={selectValue}
                onChange={e => handleSelectChange(e.target.value)}
                style={{maxWidth: 200}}
            >
                <option value=''>(select)</option>
                {options.map(opt => (
                    <option key={String(opt.value)} value={String(opt.value)}>{opt.label}</option>
                ))}
                {!otherPath && <option value='__other'>Other</option>}
            </select>
            {otherMode && (
                <input
                    type='text'
                    id={otherInputId}
                    className='form-control'
                    placeholder={node.placeholder ?? 'Other'}
                    value={otherValue || (otherPath ? '' : pathValue)}
                    onChange={e => handleOtherChange(e.target.value, false)}
                    onBlur={e => handleOtherChange(e.target.value, true)}
                    aria-label={`${node.label} (other)`}
                    autoFocus={otherPicked}
                />
            )}
        </div>
    );
};

/**
 * Checkbox group writing the picked values to `path` (string[]). Optional
 * `otherPath` adds a free-text "Other" affordance that writes to a
 * separate string column. Used by ZIRC's "Acceptance Reasons" section.
 */
const MultiCheckboxWithOtherInput = ({node, ctx, inputId, labelId}: CompositeProps) => {
    const scope = ctx.row ?? ctx.dto;
    const options = resolveOptions(node.options, ctx);
    const otherPath = node.otherPath;

    const rawPicked = readPath(scope, node.path);
    const picked: string[] = Array.isArray(rawPicked) ? rawPicked.map(String) : [];
    const otherText = otherPath ? valueAsInputString(readPath(scope, otherPath)) : '';

    // "Other checked" state: persisted indirectly via otherText being
    // non-empty. Local override for the in-between case where the user
    // just ticked the checkbox but hasn't typed yet.
    const [otherChecked, setOtherChecked] = React.useState<boolean>(otherText.trim() !== '');

    function toggleCanonical(optValue: string, checked: boolean) {
        const next = checked
            ? [...picked.filter(p => p !== optValue), optValue]
            : picked.filter(p => p !== optValue);
        ctx.onChange(node.path, next);
        ctx.onCommit(node.path, next);
    }

    function toggleOther(checked: boolean) {
        setOtherChecked(checked);
        if (!checked && otherPath) {
            ctx.onChange(otherPath, null);
            ctx.onCommit(otherPath, null);
        }
    }

    function changeOther(raw: string, commit: boolean) {
        if (!otherPath) {
            return;
        }
        const wire = normalizeForWire(raw, 'text');
        ctx.onChange(otherPath, wire);
        if (commit) {
            ctx.onCommit(otherPath, wire);
        }
    }

    const otherTextId = `${inputId}-other-text`;
    return (
        <fieldset className='border-0 p-0 m-0' aria-labelledby={labelId}>
            {options.map(opt => {
                const optValue = String(opt.value);
                const id = `${inputId}-${optValue}`;
                return (
                    <div className='form-check' key={optValue}>
                        <input
                            type='checkbox'
                            id={id}
                            className='form-check-input'
                            checked={picked.includes(optValue)}
                            onChange={e => toggleCanonical(optValue, e.target.checked)}
                        />
                        <label className='form-check-label' htmlFor={id}>{opt.label}</label>
                    </div>
                );
            })}
            {otherPath && (
                <>
                    <div className='form-check'>
                        <input
                            type='checkbox'
                            id={`${inputId}-other`}
                            className='form-check-input'
                            checked={otherChecked}
                            onChange={e => toggleOther(e.target.checked)}
                        />
                        <label className='form-check-label' htmlFor={`${inputId}-other`}>Other</label>
                    </div>
                    {otherChecked && (
                        <div className='mt-2 ml-4' style={{maxWidth: 600}}>
                            <label htmlFor={otherTextId} className='sr-only'>
                                {node.label} (other details)
                            </label>
                            <input
                                type='text'
                                id={otherTextId}
                                className='form-control'
                                placeholder={node.placeholder ?? 'Describe'}
                                value={otherText}
                                onChange={e => changeOther(e.target.value, false)}
                                onBlur={e => changeOther(e.target.value, true)}
                                autoFocus
                            />
                        </div>
                    )}
                </>
            )}
        </fieldset>
    );
};

// NumberWithUnitInput removed -- it was added for the linked-features
// distance dual-column representation; that's now a unified
// {distanceValue, distanceUnit} pair on the wire (ZFIN-10265 task 5)
// and the schema uses two simple fields (float + select). Restore from
// git history if a multi-column UI ever recurs.

// ─── Custom node ──────────────────────────────────────────────────────────

const CustomView = ({node, ctx}: {node: CustomNode; ctx: RenderCtx}) => {
    const rendered = node.render({
        dto: ctx.dto,
        row: ctx.row,
        actions: ctx.actions,
        onChange: patch => Object.entries(patch).forEach(([k, v]) => ctx.onChange(k, v)),
        onCommit: patch => Object.entries(patch).forEach(([k, v]) => {
            ctx.onChange(k, v);
            ctx.onCommit(k, v);
        }),
    });
    // When we're inside a <tbody> (a section or array with field
    // children), wrap the custom output in a row so the result stays
    // valid DOM. Schema authors writing custom renders shouldn't have
    // to know whether they're rendered in a table or not.
    if (ctx.inTableRow) {
        return <tr><td colSpan={2}>{rendered}</td></tr>;
    }
    return <>{rendered}</>;
};

// ─── Array node ───────────────────────────────────────────────────────────

const ArrayView = ({node, ctx}: {node: ArrayNode; ctx: RenderCtx}) => {
    const arrFromDto = readPath(ctx.row ?? ctx.dto, node.path);
    const rows: unknown[] = Array.isArray(arrFromDto) ? arrFromDto : [];

    // Mirror the array in a ref so multiple commits in one event handler
    // (e.g. number-with-unit's "clear old + set new" pattern) each read
    // the latest in-progress state instead of stale parent state. Runs on
    // every render to stay in sync with externally-driven updates (e.g.
    // server response repopulating the array).
    const arrRef = useRef<unknown[]>(rows);
    useEffect(() => {
        arrRef.current = rows;
    });

    // Per-row collapse state. Rows whose data satisfies `collapseWhen`
    // default to collapsed; explicit Done/Edit overrides take precedence.
    // Keyed by row index — fine for the ZIRC use cases where rows are
    // append/remove only; revisit if mid-array inserts become common.
    const [explicitlyExpanded, setExplicitlyExpanded] = useState<Set<number>>(new Set());
    const [explicitlyCollapsed, setExplicitlyCollapsed] = useState<Set<number>>(new Set());

    const [pickerOpen, setPickerOpen] = useState<boolean>(false);

    function applyChange(rowIdx: number, rowPath: string, value: unknown): unknown[] {
        const arr = [...arrRef.current];
        const cur = (arr[rowIdx] ?? {}) as Record<string, unknown>;
        arr[rowIdx] = {...cur, [rowPath]: value};
        arrRef.current = arr;
        return arr;
    }

    function makeRowCtx(rowIdx: number, row: unknown): RenderCtx {
        return {
            dto: ctx.dto,
            row,
            actions: ctx.actions,
            onChange: (rowPath, value) => ctx.onChange(node.path, applyChange(rowIdx, rowPath, value)),
            onCommit: (rowPath, value) => ctx.onCommit(node.path, applyChange(rowIdx, rowPath, value)),
        };
    }

    function isExpanded(rowIdx: number, row: unknown): boolean {
        if (explicitlyExpanded.has(rowIdx)) {
            return true;
        }
        if (explicitlyCollapsed.has(rowIdx)) {
            return false;
        }
        // Default: collapsed if `collapseWhen` says so, otherwise expanded.
        return node.collapseWhen ? !node.collapseWhen(row) : true;
    }

    function expandRow(rowIdx: number) {
        setExplicitlyExpanded(prev => new Set(prev).add(rowIdx));
        setExplicitlyCollapsed(prev => {
            const next = new Set(prev);
            next.delete(rowIdx);
            return next;
        });
    }

    function collapseRow(rowIdx: number) {
        setExplicitlyCollapsed(prev => new Set(prev).add(rowIdx));
        setExplicitlyExpanded(prev => {
            const next = new Set(prev);
            next.delete(rowIdx);
            return next;
        });
    }

    function appendRow(seed?: Record<string, unknown>) {
        const fresh: Record<string, unknown> = {...node.newRow(), ...(seed ?? {})};
        const next = [...arrRef.current, fresh];
        arrRef.current = next;
        // Mark the new row as explicitly expanded so collapseWhen doesn't
        // fire on it the moment the user types something that would
        // otherwise satisfy the predicate (e.g. one character in the
        // phenotype description). The user just clicked + Add — they want
        // to fill the row in, not have it collapse on them.
        setExplicitlyExpanded(prev => new Set(prev).add(arrRef.current.length - 1));
        // Only onChange — adding a blank row shouldn't trigger a save. The
        // server typically filters incomplete rows from its persisted view,
        // so a round-trip on Add would erase the row we just added. The row
        // gets committed when the user fills in fields that pass the
        // schema's "row is complete" criteria via the regular onCommit path.
        ctx.onChange(node.path, next);
    }

    function removeRow(rowIdx: number) {
        const arr = [...arrRef.current];
        arr.splice(rowIdx, 1);
        arrRef.current = arr;
        ctx.onChange(node.path, arr);
        ctx.onCommit(node.path, arr);
        // Drop any explicit expand/collapse state for the removed row
        // and re-index the rest.
        const shift = (s: Set<number>) => new Set(
            Array.from(s)
                .filter(i => i !== rowIdx)
                .map(i => (i > rowIdx ? i - 1 : i)),
        );
        setExplicitlyExpanded(shift);
        setExplicitlyCollapsed(shift);
    }

    function handleAddClick() {
        if (node.addRequiresTypePick) {
            setPickerOpen(true);
            return;
        }
        appendRow();
    }

    function handleTypePicked(pickedValue: string) {
        setPickerOpen(false);
        if (!node.addRequiresTypePick) {
            return;
        }
        appendRow({[node.addRequiresTypePick.targetPath]: pickedValue});
    }

    const atCap = node.maxItems != null && rows.length >= node.maxItems;
    const capReason = atCap && node.maxItems != null
        ? `Maximum ${node.maxItems} items.`
        : null;
    const blocked = node.addDisabledWhen?.(ctx.dto) ?? null;
    const addDisabled = atCap || blocked != null;
    const addTitle = blocked?.reason ?? capReason ?? undefined;

    return (
        <div className='form-renderer-array'>
            {rows.length === 0 && (
                <p className='text-muted'>{node.emptyMessage ?? 'No items.'}</p>
            )}
            {rows.map((row, idx) => {
                const expanded = isExpanded(idx, row);
                const itemLabel = node.itemLabel?.(idx) ?? `${node.title} ${idx + 1}`;
                if (!expanded) {
                    const summary = node.summarize
                        ? node.summarize(row, {dto: ctx.dto})
                        : itemLabel;
                    return (
                        <div
                            key={`${node.id}-${idx}`}
                            className='border rounded p-2 mb-2 d-flex align-items-center'
                        >
                            <div className='flex-grow-1' style={{minWidth: 0}}>
                                <span className='text-muted small mr-2'>{itemLabel}</span>
                                <span style={{overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap'}}>
                                    {summary}
                                </span>
                            </div>
                            <div style={{flexShrink: 0}}>
                                <button
                                    type='button'
                                    className='btn btn-sm btn-outline-secondary mr-2'
                                    onClick={() => expandRow(idx)}
                                >
                                    Edit
                                </button>
                                <button
                                    type='button'
                                    className='btn btn-sm btn-outline-danger'
                                    onClick={() => removeRow(idx)}
                                >
                                    Remove
                                </button>
                            </div>
                        </div>
                    );
                }
                const rowCtx = makeRowCtx(idx, row);
                const hasFieldChildren = node.childTemplate.some(c => c.kind === 'field');
                const childCtx: RenderCtx = hasFieldChildren
                    ? {...rowCtx, inTableRow: true}
                    : rowCtx;
                return (
                    <fieldset
                        key={`${node.id}-${idx}`}
                        className='border rounded p-3 mb-3'
                    >
                        <legend className='h6 px-2' style={{width: 'auto'}}>{itemLabel}</legend>
                        {hasFieldChildren ? (
                            <table className='table table-borderless'>
                                <tbody>
                                    {node.childTemplate.map(child => renderNode(child, childCtx))}
                                </tbody>
                            </table>
                        ) : (
                            node.childTemplate.map(child => renderNode(child, childCtx))
                        )}
                        <div className='text-right mt-2'>
                            <button
                                type='button'
                                className='btn btn-sm btn-outline-primary mr-2'
                                onClick={() => collapseRow(idx)}
                            >
                                Done
                            </button>
                            <button
                                type='button'
                                className='btn btn-sm btn-outline-danger'
                                onClick={() => removeRow(idx)}
                            >
                                Remove
                            </button>
                        </div>
                    </fieldset>
                );
            })}
            <button
                type='button'
                className='btn btn-sm btn-outline-secondary'
                onClick={handleAddClick}
                disabled={addDisabled}
                title={addTitle}
            >
                + Add {node.itemLabel ? '' : (node.title.toLowerCase().endsWith('s') ? node.title.slice(0, -1).toLowerCase() : 'item')}
            </button>
            {pickerOpen && node.addRequiresTypePick && (
                <TypePickerModal
                    title={node.addRequiresTypePick.title}
                    description={node.addRequiresTypePick.description}
                    options={node.addRequiresTypePick.options}
                    onPick={handleTypePicked}
                    onCancel={() => setPickerOpen(false)}
                />
            )}
        </div>
    );
};

interface TypePickerModalProps {
    title: string;
    description?: string;
    options: SelectOption[];
    onPick: (value: string) => void;
    onCancel: () => void;
}

const TypePickerModal = ({title, description, options, onPick, onCancel}: TypePickerModalProps) => (
    <>
        <div
            className='modal-backdrop fade show'
            style={{display: 'block'}}
            onClick={onCancel}
        />
        <div
            className='modal fade show'
            style={{display: 'block'}}
            role='dialog'
            aria-modal='true'
        >
            <div className='modal-dialog' role='document'>
                <div className='modal-content'>
                    <div className='modal-header'>
                        <h5 className='modal-title'>{title}</h5>
                        <button
                            type='button'
                            className='close'
                            aria-label='Close'
                            onClick={onCancel}
                        >
                            <span aria-hidden='true'>&times;</span>
                        </button>
                    </div>
                    <div className='modal-body'>
                        {description && <p className='text-muted'>{description}</p>}
                        <div className='list-group'>
                            {options.map(opt => (
                                <button
                                    type='button'
                                    key={String(opt.value)}
                                    className='list-group-item list-group-item-action'
                                    onClick={() => onPick(String(opt.value))}
                                >
                                    {opt.label}
                                </button>
                            ))}
                        </div>
                    </div>
                    <div className='modal-footer'>
                        <button
                            type='button'
                            className='btn btn-secondary'
                            onClick={onCancel}
                        >
                            Cancel
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </>
);

// ─── Value coercion helpers ───────────────────────────────────────────────

function readPath(scope: unknown, path: string): unknown {
    if (scope == null || typeof scope !== 'object') {
        return undefined;
    }
    return (scope as Record<string, unknown>)[path];
}

/** Render any DTO value as the string a controlled input expects. */
function valueAsInputString(v: unknown): string {
    if (v === null || v === undefined) {
        return '';
    }
    if (typeof v === 'boolean') {
        return v ? 'true' : 'false';
    }
    if (typeof v === 'number') {
        return String(v);
    }
    if (typeof v === 'string') {
        return v;
    }
    return String(v);
}

/**
 * Convert an input's raw string back to its wire-format value. Bools become
 * actual booleans (or null on '' for "not answered"); ints become numbers
 * (or null on blank / unparseable); everything else stays a string with
 * blank converted to null for nullable text columns.
 */
function normalizeForWire(raw: string, type: FieldNode['type']): unknown {
    if (type === 'bool') {
        if (raw === 'true') {
            return true;
        }
        if (raw === 'false') {
            return false;
        }
        return null;
    }
    if (type === 'checkbox') {
        return raw === 'true';
    }
    if (type === 'int') {
        if (!raw.trim()) {
            return null;
        }
        const n = parseInt(raw, 10);
        return Number.isFinite(n) ? n : null;
    }
    if (type === 'float') {
        if (!raw.trim()) {
            return null;
        }
        const n = Number(raw);
        return Number.isFinite(n) ? n : null;
    }
    // text / textarea / select / autocomplete: empty becomes null so the
    // server can clear the column; non-empty passes through as-is.
    return raw === '' ? null : raw;
}

function resolveOptions(src: OptionsSource | undefined, ctx: RenderCtx): SelectOption[] {
    if (!src) {
        return [];
    }
    if (typeof src === 'function') {
        const optCtx: OptionsCtx = {dto: ctx.dto, row: ctx.row};
        return src(optCtx);
    }
    return src;
}
