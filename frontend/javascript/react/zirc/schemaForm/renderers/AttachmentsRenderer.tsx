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

interface AttachmentsOptions {
    label?: string;
    /** Lowercase, dot-less; empty or absent means any extension. */
    acceptedExtensions?: string[];
    /** af_kind to file the upload under; see GenotypingAssayFile.KINDS. */
    attachmentKind?: string;
}

/**
 * Uploaded files on one aggregate, in one bucket. Two dimensions ride on the
 * uischema options, and they are independent:
 *
 * <p><b>Owner</b> — `options.owner` decides which endpoints are called, which
 * config key holds the id, and which React Query cache entry is invalidated.
 * Attachments were assay-only until ZFIN-10449 added phenotype images.
 *
 * <p><b>Bucket</b> — one widget serves every bucket on the assay form: the
 * four per-assayType results buckets bound to `attachments` (gel images,
 * chromatograms, result images, melt curves) and Protocol Documentation
 * bound to `protocolDocuments` (ZFIN-10415). `options.label` is the heading,
 * `options.attachmentKind` is the af_kind sent with the upload so the server
 * files it in the right bucket, and `options.acceptedExtensions` drives both
 * the picker's accept filter and the "Accepted file types" line under it.
 * Deriving the filter and the text from one list is what keeps them from
 * disagreeing. Server-side source of truth is ZircAttachmentKind, which the
 * upload endpoint validates against.
 *
 * Uploads go through a dedicated multipart endpoint, not the field-path
 * PATCH; the editor's diff filter skips both managesOwnPersistence paths.
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
    // Bucket heading, af_kind, picker filter and helper text all ride on the
    // uischema options so one widget can serve every bucket. Read through the
    // typed view of the same object `options` above is read from untyped for
    // the owner key.
    const opts = ((uischema as { options?: AttachmentsOptions } | undefined)?.options) ?? {};
    // Section heading: per-assay-type buckets ("Annotated gel images",
    // "Chromatograms") for assays, "Phenotype images" for a phenotype.
    const bucketLabel = opts.label;
    // Extensions this bucket accepts, lowercase and dot-less. Absent means
    // the bucket takes any extension (the melt-curve case, and every
    // phenotype bucket), so the accept attribute and the helper text are
    // both omitted rather than empty.
    const acceptedExtensions = opts.acceptedExtensions;
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
            { owner, ownerId, file, kind: opts.attachmentKind },
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
                    accept={acceptAttr}
                    onChange={handlePick}
                    disabled={!ownerId || upload.isPending || atCapacity}
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
