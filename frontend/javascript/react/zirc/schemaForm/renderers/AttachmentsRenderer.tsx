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
import { viewConfigFrom } from '../useViewConfig';

/**
 * Per-assay attachments. The bucket heading and the accepted file types both
 * arrive from the uischema (options.label / options.acceptedExtensions), so
 * the same widget serves all four per-assayType buckets — gel images,
 * chromatograms, result images, melt curves. Server-side source of truth is
 * ZircAttachmentKind, which the upload endpoint validates against.
 *
 * Uploads go through a dedicated multipart endpoint, not the field-path
 * PATCH; AssayEdit's diff filter skips /attachments.
 *
 * assayId arrives via JsonForms' config prop.
 */
interface AttachmentsOptions {
    label?: string;
    /** Lowercase, dot-less; empty or absent means any extension. */
    acceptedExtensions?: string[];
}

/**
 * Lowercase extension without the dot, or null when the name has none.
 * Mirrors ZircAttachmentKind.extensionOf — reads the last dot so
 * "trace.raw.ab1" resolves to "ab1".
 */
function extensionOf(filename: string): string | null {
    const dot = filename.lastIndexOf('.');
    if (dot < 0 || dot === filename.length - 1) {return null;}
    return filename.slice(dot + 1).toLowerCase();
}

function hasAcceptedExtension(filename: string, accepted: string[]): boolean {
    const ext = extensionOf(filename);
    return ext != null && accepted.includes(ext);
}

function AttachmentsRenderer({ data, schema, config, uischema, visible }: ControlProps) {
    if (visible === false) {return null;}
    const files = (data as AssayFileDTO[] | undefined) ?? [];
    const assayId = (config as { assayId?: number } | undefined)?.assayId;
    const upload = useUploadAttachment();
    const remove = useDeleteAttachment();
    const inputRef = React.useRef<HTMLInputElement | null>(null);
    const view = viewConfigFrom(config);
    // Per-assay-type bucket label (e.g. "Annotated gel images", "Chromatograms")
    // — set via uischema options.label so the same Attachments widget can
    // appear under different headings depending on the assay type.
    const opts = (uischema as { options?: AttachmentsOptions } | undefined)?.options;
    const bucketLabel = opts?.label;
    // Extensions this bucket accepts, lowercase and dot-less. Absent means
    // the bucket takes any extension (the melt-curve case), so the accept
    // attribute and the helper text are both omitted rather than empty.
    const acceptedExtensions = opts?.acceptedExtensions;
    // ".abi,.ab1,.scf" for the file picker's filter, and the same list
    // spelled out for the helper text below it. Tested through the optional
    // chain rather than a precomputed boolean so the array narrows.
    const acceptAttr = acceptedExtensions?.length
        ? acceptedExtensions.map((e) => `.${e}`).join(',')
        : undefined;
    const acceptedDisplay = acceptedExtensions?.length
        ? acceptedExtensions.map((e) => `.${e}`).join(', ')
        : undefined;

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
        // The accept attribute only filters the picker's default view — a
        // curator can still choose "All files", and drag-and-drop bypasses
        // it entirely. Check here so the rejection is immediate and names
        // the allowed types; the endpoint enforces the same rule regardless.
        if (acceptedExtensions?.length
            && !hasAcceptedExtension(file.name, acceptedExtensions)) {
            setErrorMsg(`${file.name} is not an accepted file type. Accepted: ${acceptedDisplay}`);
            if (inputRef.current) {inputRef.current.value = '';}
            return;
        }
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
                    accept={acceptAttr}
                    onChange={handlePick}
                    disabled={!assayId || upload.isPending || atCapacity}
                    title={capTitle}
                />
                {upload.isPending && (
                    <span className='text-muted small ml-2'>Uploading…</span>
                )}
            </div>
            {acceptedDisplay && (
                <small className='form-text text-muted'>
                    Accepted file types: {acceptedDisplay}
                </small>
            )}
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
