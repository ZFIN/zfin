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
import { AssayFileDTO } from '../../api/types';
import { useUploadAttachment, useDeleteAttachment } from '../../api/queries';

/**
 * Per-assay attachments. Single "Attachments" section regardless of
 * assayType — the original four-kind matrix (chromatogram / gel_image /
 * result_image / melt_curve) is collapsed to a generic uploader for now.
 * Uploads go through a dedicated multipart endpoint, not the field-path
 * PATCH; AssayEdit's diff filter skips /attachments.
 *
 * assayId arrives via JsonForms' config prop.
 */
function AttachmentsRenderer({ data, schema, config }: ControlProps) {
    const files = (data as AssayFileDTO[] | undefined) ?? [];
    const assayId = (config as { assayId?: number } | undefined)?.assayId;
    const upload = useUploadAttachment();
    const remove = useDeleteAttachment();
    const inputRef = React.useRef<HTMLInputElement | null>(null);

    const [errorMsg, setErrorMsg] = React.useState<string | null>(null);

    // Server-published MAX_ATTACHMENTS_PER_ASSAY via JSON Schema maxItems.
    const maxItems = (schema as { maxItems?: number } | undefined)?.maxItems;
    const atCapacity = maxItems != null && files.length >= maxItems;
    const capTitle = atCapacity
        ? `Maximum ${maxItems} attachments per assay.`
        : undefined;

    const handlePick = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file || !assayId) {return;}
        setErrorMsg(null);
        upload.mutate(
            { assayId, file },
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
        if (!assayId) {return;}
        // eslint-disable-next-line no-alert
        if (!window.confirm('Delete this attachment? This action cannot be undone.')) {return;}
        remove.mutate({ assayId, fileId });
    };

    const fmtSize = (bytes: number | null) => {
        if (!bytes) {return '';}
        if (bytes < 1024) {return `${bytes} B`;}
        if (bytes < 1024 * 1024) {return `${(bytes / 1024).toFixed(1)} KB`;}
        return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    };

    return (
        <div>
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
                                    href={`/action/api/zirc/assays/attachments/${f.id}/content`}
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
                    disabled={!assayId || upload.isPending || atCapacity}
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
