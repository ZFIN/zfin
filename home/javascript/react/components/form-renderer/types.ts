// Schema types for the declarative form renderer.
//
// A FormNode tree describes what a curator-facing form looks like, in a
// shape that mirrors the underlying JSON DTO. The renderer (see
// FormRenderer.tsx) walks the tree and produces JSX; containers stay
// out of the rendering business and just provide the DTO + commit
// callbacks. See the design discussion under
// `reference/schema-form-design.md` (or the original PR for ZFIN-10265)
// for the rationale.

import React from 'react';

// ─── Primitive types ──────────────────────────────────────────────────────

export interface SelectOption {
    /** Wire value. May be number or string; renderer stringifies for the DOM. */
    value: string | number;
    label: string;
}

/**
 * Options can be a static list or a function that derives them from form
 * state. The function form is needed when one field's options depend on
 * another field — e.g. "Mutation B" excluding whatever's picked for
 * "Mutation A" in the same linked-feature row.
 */
export type OptionsSource =
    | SelectOption[]
    | ((ctx: OptionsCtx) => SelectOption[]);

export interface OptionsCtx {
    /** Full form DTO (read-only). */
    dto: unknown;
    /** Current row when inside an ArrayNode's childTemplate; undefined elsewhere. */
    row?: unknown;
}

/**
 * Visibility predicate. Returning false hides the node entirely (and the
 * renderer skips reading/writing its path). Top-level fields receive only
 * `values`; array-child fields also receive the current row.
 */
export type Visible = (values: unknown, rowCtx?: unknown) => boolean;

// ─── Field types ──────────────────────────────────────────────────────────

export type FieldType =
    | 'text'
    | 'textarea'
    | 'int'                          // numeric input; integer-only; value stored as number
    | 'float'                        // numeric input; decimals allowed; value stored as number
    | 'bool'                         // Yes/No radio pair
    | 'checkbox'                     // single boolean checkbox (binary on/off; distinct from bool's tristate)
    | 'readonly'                     // read-only display row (e.g. server-assigned IDs)
    | 'select'                       // single-select dropdown
    | 'autocomplete'                 // server-backed typeahead
    | 'select-with-other'            // select + "Other" sentinel revealing free-text
    | 'multi-checkbox-with-other';   // checkbox group + optional "Other" free-text

// ─── Node types ───────────────────────────────────────────────────────────

interface NodeBase {
    visible?: Visible;
}

/** Leaf node — reads/writes one (or more, via units) DTO path. */
export interface FieldNode extends NodeBase {
    kind: 'field';
    /**
     * Dot-free DTO key for simple cases. Inside an ArrayNode's childTemplate
     * this is read relative to the current row. For `number-with-unit` the
     * path may be synthetic — actual columns are listed in `units[].path`.
     */
    path: string;
    label: string;
    type: FieldType;
    /** HTML placeholder; meaningful for text, textarea, int. */
    placeholder?: string;
    /** Help text rendered under the input. */
    helpText?: string;
    /** Optional "(i)" external link rendered next to the label. */
    infoHref?: string;
    /** Right-side suffix for numeric inputs ("bp", "%", etc.). */
    suffix?: string;
    // ─── Type-specific extras (each optional; validated at runtime) ─────
    /** Required for `select`, `select-with-other`, `multi-checkbox-with-other`. */
    options?: OptionsSource;
    /** Required for `autocomplete`. Renderer hits `${url}?term=…`. */
    autocompleteUrl?: string;
    /** Required for `select-with-other` and `multi-checkbox-with-other`. */
    otherPath?: string;
    /**
     * For two-column `select-with-other` (i.e. `otherPath` set), the
     * canonical value in `options` that means "use the free-text in
     * otherPath". Defaults to 'Other'. Override when the wire enum uses
     * a different token (e.g. 'other' lowercase).
     */
    otherValue?: string;
}

/** Object section — labeled group of child nodes. */
export interface SectionNode extends NodeBase {
    kind: 'section';
    id: string;
    title: string;
    children: FormNode[];
}

/** Array section — a list of repeating rows rendered from `childTemplate`. */
export interface ArrayNode extends NodeBase {
    kind: 'array';
    id: string;
    /** Path to the array on the DTO. */
    path: string;
    title: string;
    /** Schema applied per row. Field paths inside resolve against the row. */
    childTemplate: FormNode[];
    /** Factory for a blank row when the user clicks "+ Add". */
    newRow: () => Record<string, unknown>;
    /** Optional cap on the number of rows. */
    maxItems?: number;
    /** Message shown when the array is empty. */
    emptyMessage?: string;
    /** Per-row label, e.g. `(idx) => \`Lesion ${idx + 1}\``. */
    itemLabel?: (idx: number) => string;
    /** Summary text shown when a row is collapsed. */
    summarize?: (row: unknown, ctx: {dto: unknown}) => string;
    /** Auto-collapse predicate: when true, the row starts collapsed. */
    collapseWhen?: (row: unknown) => boolean;
    /** Disable the "+ Add" button with a reason (e.g. "need 2 mutations first"). */
    addDisabledWhen?: (dto: unknown) => {reason: string} | null;
    /**
     * Optional "pick a type before adding" modal. When set, clicking "+ Add"
     * pops a modal whose options the user picks from; the picked value is
     * written to `targetPath` on the new row before it's appended.
     */
    addRequiresTypePick?: {
        title: string;
        description?: string;
        options: SelectOption[];
        targetPath: string;
    };
}

/**
 * Escape hatch — full custom rendering. Use sparingly: every CustomNode is
 * a place where the abstraction is leaking, and they bypass the renderer's
 * visibility/save unification. Examples that justify it: read-only tables
 * with cross-page navigation (e.g. mutations table), genuinely unusual
 * widgets (e.g. phenotype hpf/dpf timing with derived stage).
 */
export interface CustomNode extends NodeBase {
    kind: 'custom';
    id: string;
    render: (ctx: CustomCtx) => React.ReactNode;
}

export interface CustomCtx {
    /** Full form DTO. */
    dto: unknown;
    /** Current row when inside an ArrayNode's childTemplate. */
    row?: unknown;
    /** Container-supplied callbacks for non-schema actions (e.g. removeMutation). */
    actions: Record<string, (...args: unknown[]) => unknown>;
    /** Patch helper bound to the current scope (row if any, dto otherwise). */
    onChange: (patch: Record<string, unknown>) => void;
    /** Same shape as onChange, but also fires a commit (server save). */
    onCommit: (patch: Record<string, unknown>) => void;
}

export type FormNode = SectionNode | ArrayNode | FieldNode | CustomNode;
