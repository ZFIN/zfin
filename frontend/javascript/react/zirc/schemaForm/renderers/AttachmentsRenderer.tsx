import * as React from 'react';
import {
    and,
    ControlProps,
    isControl,
    JsonFormsRendererRegistryEntry,
    optionIs,
    rankWith,
} from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import {
    AttachmentOwner,
    attachmentContentUrl,
    useUploadAttachment,
    useDeleteAttachment,
} from '../../api/queries';
import { viewConfigFrom } from '../useViewConfig';

/**
 * Uploaded files on one aggregate. Originally assay-only; ZFIN-10449 added
 * phenotype images, so the owner comes from uischema `options.owner` and
 * decides which endpoints are called, which config key holds the id, and
 * which React Query cache entry is invalidated.
 *
 * <p>Assays show a single section regardless of assayType — the original
 * four-kind matrix (chromatogram / gel_image / result_image / melt_curve) is
 * collapsed to a generic uploader. Phenotypes have one bucket by definition.
 *
 * <p>Uploads go through a dedicated multipart endpoint, not the field-path
 * PATCH: the Control declares managesOwnPersistence, which keeps the array out
 * of the autosave diff and mirror-syncs it from the entity instead.
 *
 * <p>The owner's id arrives via JsonForms' config prop under `<owner>Id`.
 *
 * <p>AttachmentFile is the structural shape common to AssayFileDTO and
 * PhenotypeFileDTO, and deliberately neither of them: both are generated from
 * their own Java DTO, and naming one here would make the renderer lie about
 * the other.
 */
type AttachmentFile = {
    id: number;
    originalFilename: string;
    contentType: string | null;
    fileSize: number | null;
};

function AttachmentsRenderer({ data, schema, config, uischema, visible }: ControlProps) {
    if (visible === false) {return null;}
    const files = (data as AttachmentFile[] | undefined) ?? [];
    const options = ((uischema as { options?: Record<string, unknown> } | undefined)?.options)
        ?? {};
    // Absent means assay: attachments were assay-only before ZFIN-10449, and
    // the assay uiSchema does not set the key.
    const owner = ((options.owner as AttachmentOwner | undefined) ?? 'assay');
    const ownerId = (config as Record<string, number | undefined> | undefined)
        ?.[`${owner}Id`];
    const upload = useUploadAttachment();
    const remove = useDeleteAttachment();
    const inputRef = React.useRef<HTMLInputElement | null>(null);
    const view = viewConfigFrom(config);
    // Section heading, set via uischema options.label: per-assay-type buckets
    // ("Annotated gel images", "Chromatograms") for assays, "Phenotype images"
    // for a phenotype.
    const bucketLabel = options.label as string | undefined;

    if (view.readonly) {
        return (
            <div className='mb-3'>
                {bucketLabel && <h6>{bucketLabel}</h6>}
                {files.length === 0 ? (
                    <p className='text-muted small mb-0'>No attachments.</p>
                ) : (
                    <ul className='list-unstyled mb-0'>
                        {files.map((f) => (
                            <li key={f.id}>{f.originalFilename}</li>
                        ))}
                    </ul>
                )}
            </div>
        );
    }

    const [errorMsg, setErrorMsg] = React.useState<string | null>(null);

    // The owner's server-side cap, published as the array's maxItems, so the
    // disabled input and the server's rejection cannot disagree.
    const maxItems = (schema as { maxItems?: number } | undefined)?.maxItems;
    const atCapacity = maxItems != null && files.length >= maxItems;
    const capTitle = atCapacity
        ? `Maximum ${maxItems} ${owner === 'phenotype' ? 'images' : 'attachments'} per ${owner}.`
        : undefined;

    const handlePick = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file || !ownerId) {return;}
        setErrorMsg(null);
        upload.mutate(
            { owner, ownerId, file },
            {
                onError: (err) => {
                    setErrorMsg(err instanceof Error ? err.message : 'Upload failed');
                },
                onSettled: () => {
                    // Reset so the same filename can be re-uploaded after delete.
                    if (inputRef.current) {inputRef.current.value = '';}
                },
            },
        );
    };

    const handleDelete = (fileId: number) => {
        if (!ownerId) {return;}
        // eslint-disable-next-line no-alert
        if (!window.confirm('Delete this attachment? This action cannot be undone.')) {return;}
        remove.mutate({ owner, ownerId, fileId });
    };

    const fmtSize = (bytes: number | null) => {
        if (!bytes) {return '';}
        if (bytes < 1024) {return `${bytes} B`;}
        if (bytes < 1024 * 1024) {return `${(bytes / 1024).toFixed(1)} KB`;}
        return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    };

    return (
        <div className='mb-3'>
            {bucketLabel && <h6>{bucketLabel}</h6>}
            {files.length === 0 ? (
                <p className='text-muted'>No attachments yet.</p>
            ) : (
                <ul className='list-unstyled'>
                    {files.map((f) => (
                        <li
                            key={f.id}
                            className='border rounded p-2 mb-2 d-flex justify-content-between align-items-center'
                        >
                            <div>
                                <a
                                    href={attachmentContentUrl(owner, f.id)}
                                    target='_blank'
                                    rel='noopener noreferrer'
                                >
                                    {f.originalFilename}
                                </a>
                                <span className='text-muted small ml-2'>
                                    {f.contentType ?? 'unknown'} {fmtSize(f.fileSize)}
                                </span>
                            </div>
                            <button
                                type='button'
                                className='btn btn-sm btn-outline-danger'
                                onClick={() => handleDelete(f.id)}
                                disabled={remove.isPending}
                            >
                                Delete
                            </button>
                        </li>
                    ))}
                </ul>
            )}
            <div className='d-flex align-items-center'>
                <input
                    ref={inputRef}
                    type='file'
                    onChange={handlePick}
                    disabled={!ownerId || upload.isPending || atCapacity}
                    title={capTitle}
                />
                {upload.isPending && (
                    <span className='text-muted small ml-2'>Uploading…</span>
                )}
            </div>
            {errorMsg && (
                <div className='alert alert-danger mt-2 mb-0' role='alert'>
                    {errorMsg}
                </div>
            )}
        </div>
    );
}

export const attachmentsRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'attachmentsList'))),
    renderer: withJsonFormsControlProps(AttachmentsRenderer),
};
