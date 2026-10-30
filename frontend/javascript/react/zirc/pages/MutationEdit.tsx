import * as React from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { JsonForms } from '@jsonforms/react';
import { MutationDTO } from '../api/types';
import { FormFor } from '../api/formHelpers';
import { lineSubmissionKey, mutationKey, useMutationById } from '../api/queries';
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
import { nucleotideSequenceRendererEntry } from '../schemaForm/renderers/NucleotideSequenceRenderer';
import { StatusRefetchContext } from '../statusRefetchContext';
import { perEntityStatusConfig } from '../statusPayload';
import { StatusPayloadContext } from '../statusPayloadContext';

export type MutationEditProps = {
    mutationId: number;
    submissionId?: string;
    /** 1-based position on the submission; scopes the section anchors
     *  (#mutation-2-general) the left nav links to. */
    position: number;
};

type FormDataShape = FormFor<MutationDTO>;

/**
 * Mutation-page registry. Unlike ZircEntityEditor's fieldRenderers this
 * includes the four child-list renderers, because the mutation uiSchema
 * nests assays / genes / lesions / phenotypes. Exported for
 * widgetRegistry.test.ts.
 */
export const mutationRenderers = [
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
    // ZFIN-10475 put the CRISPR / TALEN reagent boxes in the Mutagenesis
    // section, so this page now renders sequence fields too.
    nucleotideSequenceRendererEntry,
];

/**
 * Inline editor for one mutation, mounted in an expanded card of the
 * submission page's MutationsListRenderer (ZFIN-10543). Mutations used to be
 * edited on their own page, /mutation/{id}/edit, which now redirects to the
 * card. Status and its refresh come from the submission page's contexts
 * instead of a fetch of its own.
 */
export function MutationEdit({ mutationId, submissionId, position }: MutationEditProps) {
    const mutationQuery = useMutationById(mutationId);
    const qc = useQueryClient();
    const statusPayload = React.useContext(StatusPayloadContext);
    const refetchStatus = React.useContext(StatusRefetchContext);

    // The hook derives its autosave skip-set + mirror-keys from the
    // uiSchema's managesOwnPersistence flags (assays/genes/lesions/
    // phenotypes are flagged server-side). A save refreshes the mutation's
    // own cached GET (so reopening the card reseeds from the server), the
    // submission (its card header shows allele, protocol and type), the
    // history icons, and the page's status, since a saved edit can move a
    // badge (e.g. the primer reaching 10bp, ZFIN-10407).
    const onSaved = React.useCallback(() => {
        qc.invalidateQueries({ queryKey: mutationKey(mutationId) });
        if (submissionId) {
            qc.invalidateQueries({ queryKey: lineSubmissionKey(submissionId) });
        }
        qc.invalidateQueries({ queryKey: ['zirc', 'audit'] });
        refetchStatus();
    }, [qc, mutationId, submissionId, refetchStatus]);

    const { formData, setFormData, status, errorMessage, schemaQuery } =
        useAutosavedSchemaForm<MutationDTO>({
            entity: mutationQuery.data,
            entityId: mutationId,
            schemaQueryKey: 'mutation-form-schema',
            schemaEndpoint: '/mutations/form-schema',
            patchEndpointFor: (id) => `/mutations/${id}`,
            onSaved,
        });

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
                renderers={mutationRenderers}
                cells={[]}
                config={{
                    mutationId,
                    submissionId,
                    idPrefix: `mutation-${position}`,
                    recId: `ZIRC-MUT-${mutationId}`,
                    fieldStatus: statusPayload?.mutationFieldStatus?.[String(mutationId)] ?? {},
                    sectionStatus: statusPayload?.mutationSectionStatus?.[String(mutationId)] ?? {},
                    ...perEntityStatusConfig(statusPayload),
                }}
                onChange={({ data }) => setFormData(data as FormDataShape)}
            />
        </div>
    );
}
