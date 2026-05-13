import React, {useState} from 'react';
import {useDropzone} from 'react-dropzone';

export interface UploadedAssayFile {
    id: number;
    kind: string;
    originalFilename: string;
    contentType: string | null;
    fileSize: number | null;
    uploadedAt: string | null;
    uploadedBy: string | null;
}

interface ZircAssayFileUploadProps {
    assayId: number;
    fileKind: string;
    files: UploadedAssayFile[];
    /** Called with the server's updated MutationDTO so the parent
     *  container can refresh all of its sub-collections in one shot. */
    onMutationRefresh: (dto: unknown) => void;
}

function formatSize(bytes: number | null): string {
    if (bytes == null) {
        return '';
    }
    if (bytes < 1024) {
        return `${bytes} B`;
    }
    if (bytes < 1024 * 1024) {
        return `${(bytes / 1024).toFixed(1)} KB`;
    }
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

/**
 * File upload + list widget for one (assay, kind) pair on the mutation
 * editor. Renders the current files with download / delete buttons and
 * a drag-and-drop zone for new uploads. Uses react-dropzone (already
 * a project dep). Re-fetches happen via the server-returned
 * MutationDTO — uploads / deletes both echo the parent mutation, so
 * the caller's onMutationRefresh keeps the rest of the form in sync.
 */
const ZircAssayFileUpload = ({assayId, fileKind, files, onMutationRefresh}: ZircAssayFileUploadProps) => {
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState<string>('');

    async function upload(droppedFiles: File[]) {
        if (droppedFiles.length === 0) {
            return;
        }
        setBusy(true);
        setError('');
        try {
            // Upload sequentially so server-side row ids stay in
            // creation order. Multiple files at once is rare for a
            // single curator action; the sequential overhead is fine.
            let last: unknown = null;
            for (const file of droppedFiles) {
                const form = new FormData();
                form.append('kind', fileKind);
                form.append('file', file);
                const resp = await fetch(`/action/zirc/assay/${assayId}/file`, {
                    method: 'POST',
                    body: form,
                });
                if (!resp.ok) {
                    let detail = `HTTP ${resp.status}`;
                    try {
                        const err = await resp.json();
                        if (err && err.detail) {
                            detail = err.detail;
                        }
                    } catch { /* swallow */ }
                    throw new Error(detail);
                }
                last = await resp.json();
            }
            if (last) {
                onMutationRefresh(last);
            }
        } catch (e) {
            setError(e instanceof Error ? e.message : 'Upload failed');
        } finally {
            setBusy(false);
        }
    }

    async function remove(fileId: number) {
        // eslint-disable-next-line no-alert
        if (!window.confirm('Remove this file? This cannot be undone.')) {
            return;
        }
        setBusy(true);
        setError('');
        try {
            const resp = await fetch(`/action/zirc/assay-file/${fileId}`, {
                method: 'DELETE',
            });
            if (!resp.ok) {
                throw new Error(`HTTP ${resp.status}`);
            }
            const data = await resp.json();
            onMutationRefresh(data);
        } catch (e) {
            setError(e instanceof Error ? e.message : 'Delete failed');
        } finally {
            setBusy(false);
        }
    }

    const {getRootProps, getInputProps, isDragActive} = useDropzone({
        onDrop: upload,
        multiple: true,
        disabled: busy,
    });

    return (
        <div>
            {files.length > 0 && (
                <ul className='list-unstyled mb-2'>
                    {files.map(f => (
                        <li key={f.id} className='d-flex align-items-center' style={{gap: 8}}>
                            <a href={`/action/zirc/assay-file/${f.id}/download`}>
                                {f.originalFilename}
                            </a>
                            <span className='text-muted small'>{formatSize(f.fileSize)}</span>
                            <button
                                type='button'
                                className='btn btn-sm btn-link text-danger'
                                onClick={() => remove(f.id)}
                                disabled={busy}
                            >
                                Remove
                            </button>
                        </li>
                    ))}
                </ul>
            )}
            <div
                {...getRootProps()}
                className={'file-drag-target' + (isDragActive ? ' hover' : '')}
                style={{
                    border: '1px dashed #999',
                    borderRadius: 4,
                    padding: 12,
                    textAlign: 'center',
                    cursor: busy ? 'wait' : 'pointer',
                    background: isDragActive ? '#f0f8ff' : 'transparent',
                }}
            >
                <input {...getInputProps()}/>
                <span className='text-muted small'>
                    {busy
                        ? 'Uploading…'
                        : isDragActive
                            ? 'Drop here to upload'
                            : 'Drop files here, or click to select'}
                </span>
            </div>
            {error && <div className='text-danger small mt-1'>{error}</div>}
        </div>
    );
};

export default ZircAssayFileUpload;
