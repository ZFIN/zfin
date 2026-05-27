import * as React from 'react';
import { useQuery } from '@tanstack/react-query';
import { JsonForms } from '@jsonforms/react';
import type { JsonSchema, UISchemaElement } from '@jsonforms/core';
import { api } from '../api/client';
import { LineSubmissionDTO } from '../api/types';
import { diffLeaves } from '../api/formHelpers';
import { useCreateLineSubmission } from '../api/queries';
import { SaveStatusBadge, SaveStatus } from '../components/SaveStatusBadge';
import { sectionRendererEntry } from './renderers/SectionRenderer';
import { rowControlRendererEntry } from './renderers/RowControlRenderer';
import { verticalLayoutRendererEntry } from './renderers/VerticalLayoutRenderer';
import { textareaRowRendererEntry } from './renderers/TextareaRowRenderer';
import { yesNoRadioRendererEntry } from './renderers/YesNoRadioRenderer';
import { multipleChoiceWithOtherRendererEntry } from './renderers/MultipleChoiceWithOtherRenderer';
import { selectWithOtherRendererEntry } from './renderers/SelectWithOtherRenderer';
import { mutationsListRendererEntry } from './renderers/MutationsListRenderer';
import { linkedFeaturesListRendererEntry } from './renderers/LinkedFeaturesListRenderer';
import { publicationsListRendererEntry } from './renderers/PublicationsListRenderer';

type FormData = Record<string, unknown>;

type FormSchemaDTO = { schema: JsonSchema; uiSchema: UISchemaElement };

type Props = {
    submission: LineSubmissionDTO | null;
    onCreated: (s: LineSubmissionDTO) => void;
};

const AUTOSAVE_DEBOUNCE_MS = 800;

const renderers = [
    verticalLayoutRendererEntry,
    sectionRendererEntry,
    rowControlRendererEntry,
    textareaRowRendererEntry,
    yesNoRadioRendererEntry,
    multipleChoiceWithOtherRendererEntry,
    selectWithOtherRendererEntry,
    mutationsListRendererEntry,
    linkedFeaturesListRendererEntry,
    publicationsListRendererEntry,
];

// Paths whose changes flow through dedicated endpoints (POST/DELETE), not the
// field-path PATCH. SchemaForm's diff filters these out so an Add/Delete in
// the mutations list doesn't trigger a spurious PATCH /mutations attempt.
const EXTERNALLY_MANAGED_PATHS = new Set<string>(['/mutations', '/linkedFeatures']);

function initialDataFromSubmission(submission: LineSubmissionDTO | null): FormData {
    if (!submission) {
        return {
            name: '',
            previousNames: '',
            acceptance: { reasons: [], reasonsOther: '' },
            mutations: [],
            linkedFeatures: [],
            background: {
                singleAllelic: null,
                maternalBackground: '',
                paternalBackground: '',
                backgroundChangeable: null,
            },
            additionalInfo: {
                unreportedFeaturesDetails: '',
                husbandryInfo: '',
                additionalInfo: '',
            },
        };
    }
    return {
        name: submission.name ?? '',
        previousNames: submission.previousNames ?? '',
        acceptance: {
            reasons: submission.reasons ?? [],
            reasonsOther: submission.reasonsOther ?? '',
        },
        mutations: submission.mutations ?? [],
        linkedFeatures: submission.linkedFeatures ?? [],
        background: {
            singleAllelic: submission.singleAllelic,
            maternalBackground: submission.maternalBackground ?? '',
            paternalBackground: submission.paternalBackground ?? '',
            backgroundChangeable: submission.backgroundChangeable,
        },
        additionalInfo: {
            unreportedFeaturesDetails: submission.unreportedFeaturesDetails ?? '',
            husbandryInfo: submission.husbandryInfo ?? '',
            additionalInfo: submission.additionalInfo ?? '',
        },
    };
}

/**
 * Walks two form-data trees and emits one [path, value] entry per leaf that
 * changed. Arrays are treated as leaves (the whole list is sent as one
 * value, since the schema models reasons[] as an atomic chip-list). Path is
 * JSON Pointer (`/acceptance/reasons`).
 */
/**
 * Schema-driven submission form. Server's GET /api/zirc/form-schema returns
 * both the JSON Schema and the JSON Forms uiSchema; this component just
 * dispatches them to JsonForms with our reference-styled renderers, then
 * emits one field-path PATCH per changed leaf when the user pauses typing.
 */
export function SchemaForm({ submission, onCreated }: Props) {
    const { data: schemaResponse, isLoading: schemaLoading, isError: schemaError } =
        useQuery<FormSchemaDTO>({
            queryKey: ['zirc', 'form-schema'],
            queryFn: () => api.get<FormSchemaDTO>('/form-schema'),
            staleTime: Infinity,
        });

    // formData stays null until the seed effect has applied. This eliminates
    // the closure-stale window where the autosave's diff could fire PATCHes
    // against fields that hadn't been seeded yet — see
    // memory/project_zirc_schema_form_known_issues.md for the failure mode.
    const [formData, setFormData] = React.useState<FormData | null>(null);
    const submissionIdRef = React.useRef<string | null>(submission?.zdbID ?? null);
    const lastSavedRef = React.useRef<FormData | null>(null);
    const create = useCreateLineSubmission();

    // Seed once per submission identity (null counts as a valid identity —
    // /new gets the empty-defaults shape, /edit gets the server's shape).
    React.useEffect(() => {
        if (formData !== null) {return;}
        const seed = initialDataFromSubmission(submission);
        setFormData(seed);
        lastSavedRef.current = seed;
    }, [submission?.zdbID, formData]);

    // Mutations live on the server; the React Query cache is authoritative.
    // When the parent refetches (after an Add or Delete) we mirror the new
    // list into the local form data so JsonForms re-renders the section.
    // Other fields stay locally-owned to preserve unsaved keystrokes.
    const mutationsKey = JSON.stringify(submission?.mutations ?? []);
    React.useEffect(() => {
        if (!submission || formData === null) {return;}
        const next = submission.mutations ?? [];
        setFormData((d) => (d == null ? d : { ...d, mutations: next }));
        if (lastSavedRef.current != null) {
            lastSavedRef.current = { ...lastSavedRef.current, mutations: next };
        }
    }, [mutationsKey]);

    // Same mirror pattern for linkedFeatures — Add/Delete/PATCH go through
    // their own endpoints; the React Query refetch is authoritative.
    const linkedFeaturesKey = JSON.stringify(submission?.linkedFeatures ?? []);
    React.useEffect(() => {
        if (!submission || formData === null) {return;}
        const next = submission.linkedFeatures ?? [];
        setFormData((d) => (d == null ? d : { ...d, linkedFeatures: next }));
        if (lastSavedRef.current != null) {
            lastSavedRef.current = { ...lastSavedRef.current, linkedFeatures: next };
        }
    }, [linkedFeaturesKey]);

    const [status, setStatus] = React.useState<SaveStatus>('idle');
    const [errorMessage, setErrorMessage] = React.useState<string | null>(null);

    const formDataKey = formData == null ? 'null' : JSON.stringify(formData);

    React.useEffect(() => {
        // No autosave until the seed has applied.
        if (formData == null || lastSavedRef.current == null) {return;}

        const handle = window.setTimeout(async () => {
            const changes = diffLeaves(lastSavedRef.current, formData)
                .filter(([path]) => !EXTERNALLY_MANAGED_PATHS.has(path));
            if (changes.length === 0) {return;}

            setStatus('saving');
            setErrorMessage(null);
            try {
                let id = submissionIdRef.current;
                if (!id) {
                    const created = await create.mutateAsync();
                    submissionIdRef.current = created.zdbID;
                    id = created.zdbID;
                    onCreated(created);
                    window.history.replaceState(
                        {},
                        '',
                        `/action/zirc/line-submission/${id}/edit`,
                    );
                }
                for (const [path, value] of changes) {
                    await api.patch<LineSubmissionDTO>(
                        `/line-submissions/${id}`,
                        { path, value },
                    );
                }
                lastSavedRef.current = formData;
                setStatus('saved');
            } catch (e: unknown) {
                setStatus('error');
                setErrorMessage(e instanceof Error ? e.message : 'Save failed');
            }
        }, AUTOSAVE_DEBOUNCE_MS);

        return () => window.clearTimeout(handle);
    }, [formDataKey]);

    if (schemaLoading) {return <p className='text-muted'>Loading form schema…</p>;}
    if (schemaError || !schemaResponse) {
        return <div className='alert alert-danger'>Failed to load form schema.</div>;
    }
    if (formData == null) {
        return <p className='text-muted'>Loading…</p>;
    }

    return (
        <div>
            <div className='d-flex justify-content-end mb-1'>
                <SaveStatusBadge status={status} message={errorMessage} />
            </div>
            <JsonForms
                schema={schemaResponse.schema}
                uischema={schemaResponse.uiSchema}
                data={formData}
                renderers={renderers}
                cells={[]}
                config={{
                    submissionId: submissionIdRef.current ?? submission?.zdbID,
                    mutations: submission?.mutations ?? [],
                }}
                onChange={({ data }) => setFormData(data as FormData)}
            />
        </div>
    );
}
