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
     * Top-level keys that are server-managed sub-collections (assays,
     * genes, attachments, …). Each is mirrored from the entity back into
     * form data after a refetch, and its `/key` path is excluded from the
     * autosave diff — those writes go through dedicated POST/DELETE
     * endpoints, not the field-path PATCH.
     *
     * <p>Pass a module-level constant so the reference is stable across
     * renders. A later step derives this from the uiSchema's
     * `managesOwnPersistence` flags instead of a hand-passed list.
     */
    serverManagedKeys?: readonly string[];
    /**
     * Called after a successful autosave with the JSON-Pointer paths that
     * were PATCHed. The seam for parent-query invalidation (e.g. refresh
     * the parent card when a discriminator/abbreviation field changed).
     */
    onSaved?: (changedPaths: string[]) => void;
};

export type UseAutosavedSchemaFormResult<TDto extends object> = {
    formData: FormFor<TDto> | null;
    setFormData: React.Dispatch<React.SetStateAction<FormFor<TDto> | null>>;
    status: SaveStatus;
    errorMessage: string | null;
    schemaQuery: UseQueryResult<FormSchemaDTO>;
};

export function useAutosavedSchemaForm<TDto extends object>(
    opts: UseAutosavedSchemaFormOptions<TDto>,
): UseAutosavedSchemaFormResult<TDto> {
    const {
        entity,
        entityId,
        schemaQueryKey,
        schemaEndpoint,
        patchEndpointFor,
        serverManagedKeys = [],
        onSaved,
    } = opts;

    const schemaQuery = useQuery<FormSchemaDTO>({
        queryKey: ['zirc', schemaQueryKey],
        queryFn: () => api.get<FormSchemaDTO>(schemaEndpoint),
        staleTime: Infinity,
    });

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
    const mirrorKey = JSON.stringify(
        serverManagedKeys.map(
            (k) => (entity as Record<string, unknown> | undefined)?.[k] ?? [],
        ),
    );
    React.useEffect(() => {
        if (!entity || formData == null || serverManagedKeys.length === 0) {return;}
        const ent = entity as Record<string, unknown>;
        setFormData((d) => {
            if (d == null) {return d;}
            const next = { ...d } as Record<string, unknown>;
            for (const k of serverManagedKeys) {next[k] = ent[k] ?? [];}
            return next as FormFor<TDto>;
        });
        if (lastSavedRef.current != null) {
            const ref = { ...lastSavedRef.current } as Record<string, unknown>;
            for (const k of serverManagedKeys) {ref[k] = ent[k] ?? [];}
            lastSavedRef.current = ref as FormFor<TDto>;
        }
    }, [mirrorKey]);

    const [status, setStatus] = React.useState<SaveStatus>('idle');
    const [errorMessage, setErrorMessage] = React.useState<string | null>(null);

    const skipPaths = React.useMemo(
        () => new Set(serverManagedKeys.map((k) => `/${k}`)),
        [serverManagedKeys],
    );

    const formDataKey = formData == null ? 'null' : JSON.stringify(formData);

    React.useEffect(() => {
        // No autosave until the seed has applied — guarantees any diff is
        // between two real values, not "empty default vs seed".
        if (entityId == null || formData == null || lastSavedRef.current == null) {return;}

        const handle = window.setTimeout(async () => {
            const changes = diffLeaves(lastSavedRef.current, formData)
                .filter(([path]) => !skipPaths.has(path));
            if (changes.length === 0) {return;}

            setStatus('saving');
            setErrorMessage(null);
            try {
                for (const [path, value] of changes) {
                    await api.patch(patchEndpointFor(entityId), { path, value });
                }
                lastSavedRef.current = formData;
                setStatus('saved');
                onSaved?.(changes.map(([p]) => p));
            } catch (e: unknown) {
                setStatus('error');
                setErrorMessage(e instanceof Error ? e.message : 'Save failed');
            }
        }, AUTOSAVE_DEBOUNCE_MS);

        return () => window.clearTimeout(handle);
    }, [formDataKey]);

    return { formData, setFormData, status, errorMessage, schemaQuery };
}
