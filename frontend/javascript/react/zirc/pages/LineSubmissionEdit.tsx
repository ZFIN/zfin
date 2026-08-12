import * as React from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from '../queryClient';
import { useLineSubmission } from '../api/queries';
import { SchemaForm } from '../schemaForm/SchemaForm';
import { LineSubmissionDTO } from '../api/types';

export type LineSubmissionEditProps = {
    // From data-submission-id on the JSP mount. Empty string for /new.
    submissionId?: string;
};

export default function LineSubmissionEdit(props: LineSubmissionEditProps) {
    return (
        <QueryClientProvider client={queryClient}>
            <LineSubmissionEditInner {...props} />
        </QueryClientProvider>
    );
}

function LineSubmissionEditInner({ submissionId }: LineSubmissionEditProps) {
    const initialId = submissionId && submissionId.length > 0 ? submissionId : null;
    const [createdSubmission, setCreatedSubmission] =
        React.useState<LineSubmissionDTO | null>(null);

    const effectiveId = createdSubmission?.zdbID ?? initialId;
    const query = useLineSubmission(effectiveId);

    // Only show the loading state when we have nothing to render yet. After
    // first save the create mutation seeds the cache, so any post-create GET
    // is a hit and we never unmount the form mid-autosave.
    if (effectiveId && !createdSubmission && query.isLoading) {
        return <p className='text-muted'>Loading submission…</p>;
    }
    if (effectiveId && query.isError) {
        return (
            <div className='alert alert-danger'>
                Failed to load submission: {(query.error as Error)?.message ?? 'unknown error'}
            </div>
        );
    }

    const submission = query.data ?? createdSubmission ?? null;

    return (
        <SchemaForm
            submission={submission}
            onCreated={setCreatedSubmission}
        />
    );
}
