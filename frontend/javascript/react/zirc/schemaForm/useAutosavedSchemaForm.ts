import * as React from 'react';
import { useQuery, UseQueryResult } from '@tanstack/react-query';
import type { JsonSchema, UISchemaElement } from '@jsonforms/core';
import { api } from '../api/client';
import { FormFor, seedFromDto, diffLeaves } from '../api/formHelpers';
import { SaveStatus } from '../components/SaveStatusBadge';

/**
 * The autosave state machine shared by every schema-driven aggregate
 * editor. Owns: the form-schema fetch, the null-gated seed, the
 * mirror-sync of server-managed sub-collections, and the debounced
 * field-path PATCH loop with save status.
 *
 * <p>Behavior is driven entirely by the fetched uiSchema's per-Control
 * flags — no hardcoded path lists:
 * <ul>
 *   <li>{@code managesOwnPersistence} Controls are excluded from the
 *       autosave diff (their writes go through dedicated POST/DELETE
 *       endpoints) and their top-level key is mirror-synced from the
 *       entity after each refetch.</li>
 *   <li>{@code refreshesParent} Controls, when changed, fire the
 *       {@code onRefreshParent} callback so the caller can invalidate the
 *       parent query (the field feeds the parent's collapsed card).</li>
 * </ul>
 *
 * <p>What it deliberately does NOT own: the entity fetch (callers pass
 * the already-fetched entity, since each aggregate has its own typed
 * React Query hook), the renderers array, the JsonForms mount, and the
 * loading/error JSX. Those stay in the component so this stays a pure
 * state hook. See reference/zirc-entity-editor-design.md.
 */

export type FormSchemaDTO = { schema: JsonSchema; uiSchema: UISchemaElement };

const AUTOSAVE_DEBOUNCE_MS = 800;

export type UseAutosavedSchemaFormOptions<TDto extends object> = {
    /** The fetched entity (e.g. mutationQuery.data); undefined until loaded. */
    entity: TDto | undefined;
    /** Entity id; gates the autosave loop (no id → no save). */
    entityId: number | null;
    /** React Query key suffix for the form-schema fetch (e.g. 'mutation-form-schema'). */
    schemaQueryKey: string;
    /** Endpoint serving { schema, uiSchema } (e.g. '/mutations/form-schema'). */
    schemaEndpoint: string;
    /** Builds the field-path PATCH endpoint (e.g. id => `/mutations/${id}`). */
    patchEndpointFor: (id: number) => string;
    /**
     * Called after a successful autosave when at least one PATCHed path was
     * flagged {@code refreshesParent} in the uiSchema. The seam for
     * parent-query invalidation (refresh the parent's collapsed card).
     */
    onRefreshParent?: () => void;
};

export type UseAutosavedSchemaFormResult<TDto extends object> = {
    formData: FormFor<TDto> | null;
    setFormData: React.Dispatch<React.SetStateAction<FormFor<TDto> | null>>;
    status: SaveStatus;
    errorMessage: string | null;
    schemaQuery: UseQueryResult<FormSchemaDTO>;
};

type ControlFlags = {
    /** managesOwnPersistence Controls → JSON-Pointer paths (autosave skips these). */
    skipPaths: Set<string>;
    /** managesOwnPersistence Controls → top-level keys (mirror-synced from the entity). */
    mirrorKeys: string[];
    /** refreshesParent Controls → JSON-Pointer paths (a change fires onRefreshParent). */
    refreshPaths: Set<string>;
};

const EMPTY_FLAGS: ControlFlags = {
    skipPaths: new Set(),
    mirrorKeys: [],
    refreshPaths: new Set(),
};

/**
 * Walk the uiSchema and collect the per-Control behavior flags. The
 * Control `scope` (`#/properties/attachments`) maps to a JSON Pointer
 * (`/attachments`); for managesOwnPersistence we also keep the top-level
 * key (`attachments`) for the mirror-sync, which only applies to
 * top-level array collections.
 */
function collectControlFlags(uiSchema: UISchemaElement | undefined): ControlFlags {
    if (!uiSchema) {return EMPTY_FLAGS;}
    const skipPaths = new Set<string>();
    const mirrorKeys: string[] = [];
    const refreshPaths = new Set<string>();

    const walk = (el: unknown): void => {
        if (!el || typeof el !== 'object') {return;}
        const node = el as {
            type?: string;
            scope?: string;
            options?: { managesOwnPersistence?: boolean; refreshesParent?: boolean };
            elements?: unknown[];
        };
        if (node.type === 'Control' && typeof node.scope === 'string') {
            const pointer = node.scope.replace(/^#/, '').replace(/\/properties\//g, '/');
            if (node.options?.managesOwnPersistence) {
                skipPaths.add(pointer);
                const key = pointer.replace(/^\//, '');
                if (key && !key.includes('/')) {mirrorKeys.push(key);}
            }
            if (node.options?.refreshesParent) {
                refreshPaths.add(pointer);
            }
        }
        (node.elements ?? []).forEach(walk);
    };
    walk(uiSchema);
    return { skipPaths, mirrorKeys, refreshPaths };
}

export function useAutosavedSchemaForm<TDto extends object>(
    opts: UseAutosavedSchemaFormOptions<TDto>,
): UseAutosavedSchemaFormResult<TDto> {
    const {
        entity,
        entityId,
        schemaQueryKey,
        schemaEndpoint,
        patchEndpointFor,
        onRefreshParent,
    } = opts;

    const schemaQuery = useQuery<FormSchemaDTO>({
        queryKey: ['zirc', schemaQueryKey],
        queryFn: () => api.get<FormSchemaDTO>(schemaEndpoint),
        staleTime: Infinity,
    });

    const flags = React.useMemo(
        () => collectControlFlags(schemaQuery.data?.uiSchema),
        [schemaQuery.data],
    );

    // formData stays null until the seed effect runs. Holding off on
    // rendering JsonForms eliminates the window where formData and
    // lastSavedRef disagree — the root cause of spurious "clear field"
    // PATCHes under slow loads (memory: zirc-schema-form-known-issues).
    const [formData, setFormData] = React.useState<FormFor<TDto> | null>(null);
    const lastSavedRef = React.useRef<FormFor<TDto> | null>(null);

    const seedKey = (entity as { id?: number } | undefined)?.id;
    React.useEffect(() => {
        if (!entity || formData !== null) {return;}
        const seed = seedFromDto(entity);
        setFormData(seed);
        lastSavedRef.current = seed;
    }, [seedKey, formData]);

    // Mirror server-managed sub-collections from the entity into form data
    // after each refetch (Add/Delete go through dedicated endpoints), so the
    // list renderers see the new rows without triggering the autosave diff.
    // One effect for all such keys, keyed on their combined JSON.
    const ent = entity as Record<string, unknown> | undefined;
    const mirrorKey = JSON.stringify(flags.mirrorKeys.map((k) => ent?.[k] ?? []));
    React.useEffect(() => {
        if (!ent || formData == null || flags.mirrorKeys.length === 0) {return;}
        setFormData((d) => {
            if (d == null) {return d;}
            const next = { ...d } as Record<string, unknown>;
            for (const k of flags.mirrorKeys) {next[k] = ent[k] ?? [];}
            return next as FormFor<TDto>;
        });
        if (lastSavedRef.current != null) {
            const ref = { ...lastSavedRef.current } as Record<string, unknown>;
            for (const k of flags.mirrorKeys) {ref[k] = ent[k] ?? [];}
            lastSavedRef.current = ref as FormFor<TDto>;
        }
    }, [mirrorKey]);

    const [status, setStatus] = React.useState<SaveStatus>('idle');
    const [errorMessage, setErrorMessage] = React.useState<string | null>(null);

    const formDataKey = formData == null ? 'null' : JSON.stringify(formData);

    React.useEffect(() => {
        // No autosave until the seed has applied and the schema (and thus the
        // skip-set) has loaded — guarantees any diff is between two real
        // values and that server-managed paths are correctly excluded.
        if (entityId == null || formData == null
            || lastSavedRef.current == null || !schemaQuery.data) {return;}

        const handle = window.setTimeout(async () => {
            const changes = diffLeaves(lastSavedRef.current, formData)
                .filter(([path]) => !flags.skipPaths.has(path));
            if (changes.length === 0) {return;}

            setStatus('saving');
            setErrorMessage(null);
            try {
                for (const [path, value] of changes) {
                    await api.patch(patchEndpointFor(entityId), { path, value });
                }
                lastSavedRef.current = formData;
                setStatus('saved');
                if (changes.some(([p]) => flags.refreshPaths.has(p))) {
                    onRefreshParent?.();
                }
            } catch (e: unknown) {
                setStatus('error');
                setErrorMessage(e instanceof Error ? e.message : 'Save failed');
            }
        }, AUTOSAVE_DEBOUNCE_MS);

        return () => window.clearTimeout(handle);
    }, [formDataKey]);

    return { formData, setFormData, status, errorMessage, schemaQuery };
}
