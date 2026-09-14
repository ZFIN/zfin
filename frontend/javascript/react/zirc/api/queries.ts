import * as React from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from './client';
import { AssayDTO, AutocompleteItemDTO, GeneDTO, LesionDTO, LineSubmissionDTO, LinkedFeatureDTO, MutationDTO, PhenotypeDTO, VocabularyTermDTO } from './types';

export const lineSubmissionKey = (id: string) => ['zirc', 'lineSubmission', id] as const;

export function useLineSubmission(id: string | null) {
    return useQuery({
        queryKey: lineSubmissionKey(id ?? ''),
        queryFn: () => api.get<LineSubmissionDTO>(`/line-submissions/${id}`),
        enabled: !!id,
    });
}

export function useCreateLineSubmission() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: () => api.post<LineSubmissionDTO>('/line-submissions'),
        // Seed the cache so the post-create GET is a hit, not a loading flash.
        onSuccess: (data) => {
            qc.setQueryData(lineSubmissionKey(data.zdbID), data);
        },
    });
}

export function useAddMutation() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (submissionId: string) =>
            api.post<MutationDTO>(`/line-submissions/${submissionId}/mutations`),
        onSuccess: (_data, submissionId) => {
            qc.invalidateQueries({ queryKey: lineSubmissionKey(submissionId) });
        },
    });
}

export function useDeleteMutation() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: ({ submissionId, mutationId }: { submissionId: string; mutationId: number }) =>
            api.delete<void>(`/line-submissions/${submissionId}/mutations/${mutationId}`),
        onSuccess: (_data, vars) => {
            qc.invalidateQueries({ queryKey: lineSubmissionKey(vars.submissionId) });
        },
    });
}

// ─── Linked Features (M5.3) ─────────────────────────────────────────────────

export function useAddLinkedFeature() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: ({ submissionId, mutationAId, mutationBId }:
                { submissionId: string; mutationAId: number; mutationBId: number }) =>
            api.post<LineSubmissionDTO>(
                `/line-submissions/${submissionId}/linked-features`,
                { mutationAId, mutationBId }),
        onSuccess: (_data, vars) => {
            qc.invalidateQueries({ queryKey: lineSubmissionKey(vars.submissionId) });
        },
    });
}

export function useDeleteLinkedFeature() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: ({ submissionId, aId, bId }:
                { submissionId: string; aId: number; bId: number }) =>
            api.delete<void>(
                `/line-submissions/${submissionId}/linked-features/${aId}/${bId}`),
        onSuccess: (_data, vars) => {
            qc.invalidateQueries({ queryKey: lineSubmissionKey(vars.submissionId) });
        },
    });
}

/**
 * Inline-edit PATCH for one linked feature. The renderer drives this
 * per-field via the same {path, value} shape used for the parent
 * aggregates; the URL pins the composite PK.
 */
export function usePatchLinkedFeature() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: ({ submissionId, aId, bId, path, value }:
                { submissionId: string; aId: number; bId: number; path: string; value: unknown }) =>
            api.patch<LinkedFeatureDTO>(
                `/line-submissions/${submissionId}/linked-features/${aId}/${bId}`,
                { path, value }),
        onSuccess: (_data, vars) => {
            qc.invalidateQueries({ queryKey: lineSubmissionKey(vars.submissionId) });
        },
    });
}

export const mutationKey = (id: number) => ['zirc', 'mutation', id] as const;

export function useMutationById(id: number | null) {
    return useQuery({
        queryKey: mutationKey(id ?? 0),
        queryFn: () => api.get<MutationDTO>(`/mutations/${id}`),
        enabled: !!id,
    });
}

export function useAddAssay() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (mutationId: number) =>
            api.post<MutationDTO>(`/mutations/${mutationId}/assays`),
        onSuccess: (_data, mutationId) => {
            qc.invalidateQueries({ queryKey: mutationKey(mutationId) });
        },
    });
}

export function useDeleteAssay() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: ({ assayId }: { mutationId: number; assayId: number }) =>
            api.delete<void>(`/assays/${assayId}`),
        onSuccess: (_data, vars) => {
            qc.invalidateQueries({ queryKey: mutationKey(vars.mutationId) });
        },
    });
}

export const assayKey = (id: number) => ['zirc', 'assay', id] as const;

export function useAssayById(id: number | null) {
    return useQuery({
        queryKey: assayKey(id ?? 0),
        queryFn: () => api.get<AssayDTO>(`/assays/${id}`),
        enabled: !!id,
    });
}

export function useUploadAttachment() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: ({
            assayId,
            file,
            kind,
        }: { assayId: number; file: File; kind?: string }) => {
            const form = new FormData();
            form.append('file', file);
            // Which bucket the file lands in (af_kind). Omitted by callers
            // that predate the protocol-documentation bucket; the server
            // then defaults to the results bucket.
            if (kind) {form.append('kind', kind);}
            return api.upload<AssayDTO>(`/assays/${assayId}/attachments`, form);
        },
        onSuccess: (_data, vars) => {
            qc.invalidateQueries({ queryKey: assayKey(vars.assayId) });
        },
    });
}

export function useDeleteAttachment() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: ({ fileId }: { assayId: number; fileId: number }) =>
            api.delete<void>(`/assays/attachments/${fileId}`),
        onSuccess: (_data, vars) => {
            qc.invalidateQueries({ queryKey: assayKey(vars.assayId) });
        },
    });
}

// ─── Genes (M6.1) ───────────────────────────────────────────────────────────

export const geneKey = (id: number) => ['zirc', 'gene', id] as const;

export function useGeneById(id: number | null) {
    return useQuery({
        queryKey: geneKey(id ?? 0),
        queryFn: () => api.get<GeneDTO>(`/genes/${id}`),
        enabled: !!id,
    });
}

export function useAddGene() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (mutationId: number) =>
            api.post<MutationDTO>(`/mutations/${mutationId}/genes`),
        onSuccess: (_data, mutationId) => {
            qc.invalidateQueries({ queryKey: mutationKey(mutationId) });
        },
    });
}

export function useDeleteGene() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: ({ geneId }: { mutationId: number; geneId: number }) =>
            api.delete<void>(`/genes/${geneId}`),
        onSuccess: (_data, vars) => {
            qc.invalidateQueries({ queryKey: mutationKey(vars.mutationId) });
        },
    });
}

// ─── Lesions (M7.1) ─────────────────────────────────────────────────────────

export const lesionKey = (id: number) => ['zirc', 'lesion', id] as const;

export function useLesionById(id: number | null) {
    return useQuery({
        queryKey: lesionKey(id ?? 0),
        queryFn: () => api.get<LesionDTO>(`/lesions/${id}`),
        enabled: !!id,
    });
}

export function useAddLesion() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (mutationId: number) =>
            api.post<MutationDTO>(`/mutations/${mutationId}/lesions`),
        onSuccess: (_data, mutationId) => {
            qc.invalidateQueries({ queryKey: mutationKey(mutationId) });
        },
    });
}

export function useDeleteLesion() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: ({ lesionId }: { mutationId: number; lesionId: number }) =>
            api.delete<void>(`/lesions/${lesionId}`),
        onSuccess: (_data, vars) => {
            qc.invalidateQueries({ queryKey: mutationKey(vars.mutationId) });
        },
    });
}

// ─── Phenotypes (M8.1) ──────────────────────────────────────────────────────

export const phenotypeKey = (id: number) => ['zirc', 'phenotype', id] as const;

export function usePhenotypeById(id: number | null) {
    return useQuery({
        queryKey: phenotypeKey(id ?? 0),
        queryFn: () => api.get<PhenotypeDTO>(`/phenotypes/${id}`),
        enabled: !!id,
    });
}

export function useAddPhenotype() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (mutationId: number) =>
            api.post<MutationDTO>(`/mutations/${mutationId}/phenotypes`),
        onSuccess: (_data, mutationId) => {
            qc.invalidateQueries({ queryKey: mutationKey(mutationId) });
        },
    });
}

export function useDeletePhenotype() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: ({ phenotypeId }: { mutationId: number; phenotypeId: number }) =>
            api.delete<void>(`/phenotypes/${phenotypeId}`),
        onSuccess: (_data, vars) => {
            qc.invalidateQueries({ queryKey: mutationKey(vars.mutationId) });
        },
    });
}

// ─── Controlled vocabularies ────────────────────────────────────────────────

/**
 * An {@code mdcv_used_in} discriminator value. Must stay in step with
 * {@code ZircVocabularyService.VOCABULARIES}; an unknown name 404s rather
 * than returning an empty list, so a typo surfaces instead of rendering a
 * silently empty dropdown.
 */
export type VocabularyName =
    | 'amino_acid_term'
    | 'protein_consequence_term'
    | 'transcript_consequence_term'
    | 'dna_mutation_term'
    | 'gene_localization_term';

/**
 * Pick-list terms for the vocabulary-backed select widgets, already in
 * display order (the server sorts by {@code mdcv_term_order}, breaking ties
 * on display name).
 *
 * These lists are curator-managed reference data that changes roughly once
 * a year, so cache for the life of the page: no {@code staleTime} expiry,
 * and no refetch on remount or window focus. Every lesion card on a
 * submission mounts the same pick lists, and refetching per card would be
 * pure waste.
 */
export function useVocabulary(name: VocabularyName) {
    return useQuery({
        queryKey: ['zirc', 'vocabulary', name],
        queryFn: () => api.get<VocabularyTermDTO[]>(`/vocabulary/${name}`),
        staleTime: Infinity,
        refetchOnMount: false,
        refetchOnWindowFocus: false,
    });
}

// ─── Autocomplete (M5.2) ────────────────────────────────────────────────────

export type AutocompleteEndpoint = 'markers' | 'features' | 'persons' | 'pis';

/**
 * Type-ahead lookup against {@code /api/zirc/autocomplete/{endpoint}}.
 * The {@code term} state should already be debounced by the caller —
 * see {@link useDebouncedValue}. Empty/whitespace-only terms short-
 * circuit to an empty list without firing a request.
 *
 * {@code typeGroup} narrows the markers endpoint to one of the
 * {@code Marker.TypeGroup} values (e.g. {@code "GENEDOM"} for the gene
 * picker, {@code "SSLP"} for sequence-tagged sites). Ignored on the
 * features and persons endpoints.
 */
export function useAutocomplete(
    endpoint: AutocompleteEndpoint,
    term: string,
    typeGroup?: string | null,
) {
    const trimmed = term.trim();
    const params = new URLSearchParams({ term: trimmed });
    if (typeGroup) { params.set('typeGroup', typeGroup); }
    return useQuery({
        queryKey: ['zirc', 'autocomplete', endpoint, typeGroup ?? null, trimmed],
        queryFn: () =>
            api.get<AutocompleteItemDTO[]>(
                `/autocomplete/${endpoint}?${params.toString()}`,
            ),
        enabled: trimmed.length > 0,
        // The server hard-caps at 20 results; cache aggressively so re-
        // typing the same prefix doesn't re-fetch.
        staleTime: 60_000,
    });
}

/**
 * Resolves a single stored ZDB-ID back to its {@link AutocompleteItemDTO}
 * via {@code exactMatch=true}, so the form's record chip can show the
 * symbol for a value that was saved earlier (the form data only carries
 * the raw id). Returns at most one row; an empty list means the id didn't
 * resolve. {@code typeGroup} is accepted for call-site symmetry with
 * {@link useAutocomplete} but the server ignores it for exact lookups.
 */
export function useAutocompleteResolve(
    endpoint: AutocompleteEndpoint,
    value: string | null | undefined,
    typeGroup?: string | null,
) {
    const id = (value ?? '').trim();
    const params = new URLSearchParams({ term: id, exactMatch: 'true' });
    if (typeGroup) { params.set('typeGroup', typeGroup); }
    return useQuery({
        queryKey: ['zirc', 'autocomplete-resolve', endpoint, id],
        queryFn: () =>
            api.get<AutocompleteItemDTO[]>(
                `/autocomplete/${endpoint}?${params.toString()}`,
            ),
        enabled: id.length > 0,
        staleTime: 60_000,
    });
}

/**
 * Debounces a fast-changing value (e.g. a search-input keystroke
 * stream) so downstream effects see at most one value per {@code delay}
 * window. Used with {@link useAutocomplete} to keep request volume
 * sane during typing.
 */
export function useDebouncedValue<T>(value: T, delay = 200): T {
    const [debounced, setDebounced] = React.useState(value);
    React.useEffect(() => {
        const t = window.setTimeout(() => setDebounced(value), delay);
        return () => window.clearTimeout(t);
    }, [value, delay]);
    return debounced;
}
