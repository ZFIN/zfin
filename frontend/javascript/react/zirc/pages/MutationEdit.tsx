import * as React from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { JsonForms } from '@jsonforms/react';
import { queryClient } from '../queryClient';
import { MutationDTO } from '../api/types';
import { FormFor } from '../api/formHelpers';
import { useMutationById } from '../api/queries';
import { useAutosavedSchemaForm } from '../schemaForm/useAutosavedSchemaForm';
import { SaveStatusBadge } from '../components/SaveStatusBadge';
import { sectionRendererEntry } from '../schemaForm/renderers/SectionRenderer';
import { rowControlRendererEntry } from '../schemaForm/renderers/RowControlRenderer';
import { verticalLayoutRendererEntry } from '../schemaForm/renderers/VerticalLayoutRenderer';
import { textareaRowRendererEntry } from '../schemaForm/renderers/TextareaRowRenderer';
import { yesNoRadioRendererEntry } from '../schemaForm/renderers/YesNoRadioRenderer';
import { selectWithOtherRendererEntry } from '../schemaForm/renderers/SelectWithOtherRenderer';
import { publicationsListRendererEntry } from '../schemaForm/renderers/PublicationsListRenderer';
import { assaysListRendererEntry } from '../schemaForm/renderers/AssaysListRenderer';
import { genesListRendererEntry } from '../schemaForm/renderers/GenesListRenderer';
import { lesionsListRendererEntry } from '../schemaForm/renderers/LesionsListRenderer';
import { phenotypesListRendererEntry } from '../schemaForm/renderers/PhenotypesListRenderer';
import { autocompleteRendererEntry } from '../schemaForm/renderers/AutocompleteRenderer';

export type MutationEditProps = {
    // From data-mutation-id on the JSP mount.
    mutationId?: string;
    submissionId?: string;
};

export default function MutationEdit(props: MutationEditProps) {
    return (
        <QueryClientProvider client={queryClient}>
            <MutationEditInner {...props} />
        </QueryClientProvider>
    );
}

type FormDataShape = FormFor<MutationDTO>;

const renderers = [
    verticalLayoutRendererEntry,
    sectionRendererEntry,
    rowControlRendererEntry,
    textareaRowRendererEntry,
    yesNoRadioRendererEntry,
    selectWithOtherRendererEntry,
    publicationsListRendererEntry,
    assaysListRendererEntry,
    genesListRendererEntry,
    lesionsListRendererEntry,
    phenotypesListRendererEntry,
    autocompleteRendererEntry,
];

function MutationEditInner({ mutationId, submissionId }: MutationEditProps) {
    const idNum = mutationId ? Number(mutationId) : null;
    const mutationQuery = useMutationById(idNum);

    // The hook derives its autosave skip-set + mirror-keys from the
    // uiSchema's managesOwnPersistence flags (assays/genes/lesions/
    // phenotypes are flagged server-side). No parent to refresh — the
    // mutation edit page is its own route, not an inline card.
    const { formData, setFormData, status, errorMessage, schemaQuery } =
        useAutosavedSchemaForm<MutationDTO>({
            entity: mutationQuery.data,
            entityId: idNum,
            schemaQueryKey: 'mutation-form-schema',
            schemaEndpoint: '/mutations/form-schema',
            patchEndpointFor: (id) => `/mutations/${id}`,
        });

    if (!idNum) {
        return <div className='alert alert-danger'>Missing mutation id.</div>;
    }
    if (schemaQuery.isLoading || mutationQuery.isLoading || formData == null) {
        return <p className='text-muted'>Loading…</p>;
    }
    if (schemaQuery.isError || mutationQuery.isError || !schemaQuery.data || !mutationQuery.data) {
        return <div className='alert alert-danger'>Failed to load mutation form.</div>;
    }

    return (
        <div>
            <div className='d-flex justify-content-end mb-1'>
                <SaveStatusBadge status={status} message={errorMessage} />
            </div>
            <JsonForms
                schema={schemaQuery.data.schema}
                uischema={schemaQuery.data.uiSchema}
                data={formData}
                renderers={renderers}
                cells={[]}
                config={{ mutationId: idNum, submissionId }}
                onChange={({ data }) => setFormData(data as FormDataShape)}
            />
        </div>
    );
}
