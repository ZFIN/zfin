import type { FieldStatus } from '../components/StatusBadge';

/**
 * Shared lookup for the per-renderer view-mode config. SchemaForm threads
 * the view payload through JsonForms' `config` prop, which lands on every
 * renderer as `props.config`. Renderers call this with their config prop
 * and branch on `readonly`.
 *
 * Status / updates maps are keyed by the field's leaf path segment (matches
 * what the LineSubmissionStatusComputer.byField already emits — no
 * controller-side rekey needed).
 */
export type ViewConfig = {
    readonly: boolean;
    fieldStatus: Record<string, FieldStatus>;
    sectionStatus: Record<string, FieldStatus>;
    /** When set, SectionRenderer prefixes its DOM id with this string + '-'. */
    idPrefix: string;
    /**
     * recId for the comments + audit APIs. Submission level: the ZDB-ID
     * directly. Nested aggregates: ZIRC-MUT-{id}, ZIRC-GENE-{id}, etc.
     * Renderers pass this + the leaf-field name to {@code FieldComments}
     * and {@code FieldHistory}.
     */
    recId: string | null;
    /**
     * Per-field display-label overrides keyed by leaf field name. Used in
     * view mode when the raw stored value is a server-resolvable
     * identifier (e.g. a Feature ZDB-ID picked from autocomplete) but the
     * UI should render a human-friendly label instead. Renderers fall
     * back to the raw value when there is no override.
     */
    displayLabels: Record<string, string>;
};

export function viewConfigFrom(config: unknown): ViewConfig {
    const cfg = (config ?? {}) as Partial<ViewConfig>;
    return {
        readonly: !!cfg.readonly,
        fieldStatus: cfg.fieldStatus ?? {},
        sectionStatus: cfg.sectionStatus ?? {},
        idPrefix: cfg.idPrefix ?? '',
        recId: cfg.recId ?? null,
        displayLabels: cfg.displayLabels ?? {},
    };
}

export function leafOf(path: string): string {
    return path.split('.').pop() ?? path;
}

/**
 * Schema-driven opt-out for the comments icon. A uiSchema Control/Group can
 * set {@code options.comments: false} to hide the icon on that field /
 * section. Default (null/undefined): enabled. Renderers call this with
 * their {@code uischema} prop and skip {@code <FieldComments/>} when it
 * returns false.
 */
export function commentsEnabled(uischema: unknown): boolean {
    const opts = (uischema as { options?: { comments?: boolean } } | undefined)?.options;
    return opts?.comments !== false;
}
