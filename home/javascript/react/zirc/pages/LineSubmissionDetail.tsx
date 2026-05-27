import * as React from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from '../queryClient';
import { useLineSubmission } from '../api/queries';
import { SchemaForm } from '../schemaForm/SchemaForm';
import type { FieldStatus } from '../components/StatusBadge';

export type LineSubmissionDetailProps = {
    submissionId: string;
    // The JSP serializes the status + audit maps that ZircDashboardController
    // already computes for the existing detail page into a script block; we
    // pull them off the DOM here instead of re-fetching, so the React detail
    // page renders in one round-trip without a new endpoint.
    statusPayloadElementId?: string;
};

type PerEntityStatus = Record<string, Record<string, FieldStatus>>;

type StatusPayload = {
    fieldStatus: Record<string, FieldStatus>;
    sectionStatus: Record<string, FieldStatus>;
    mutationFieldStatus: PerEntityStatus;
    mutationSectionStatus: PerEntityStatus;
    mutationOverallStatus: Record<string, FieldStatus>;
    geneFieldStatus: PerEntityStatus;
    geneSectionStatus: PerEntityStatus;
    lesionFieldStatus: PerEntityStatus;
    lesionSectionStatus: PerEntityStatus;
    assayFieldStatus: PerEntityStatus;
    assaySectionStatus: PerEntityStatus;
    phenotypeFieldStatus: PerEntityStatus;
    phenotypeSectionStatus: PerEntityStatus;
};

export default function LineSubmissionDetail(props: LineSubmissionDetailProps) {
    return (
        <QueryClientProvider client={queryClient}>
            <LineSubmissionDetailInner {...props}/>
        </QueryClientProvider>
    );
}

function LineSubmissionDetailInner({
    submissionId,
    statusPayloadElementId = 'ls-detail-status-payload',
}: LineSubmissionDetailProps) {
    const payload = React.useMemo<StatusPayload>(() => {
        const el = document.getElementById(statusPayloadElementId);
        const empty: StatusPayload = {
            fieldStatus: {},
            sectionStatus: {},
            mutationFieldStatus: {},
            mutationSectionStatus: {},
            mutationOverallStatus: {},
            geneFieldStatus: {},
            geneSectionStatus: {},
            lesionFieldStatus: {},
            lesionSectionStatus: {},
            assayFieldStatus: {},
            assaySectionStatus: {},
            phenotypeFieldStatus: {},
            phenotypeSectionStatus: {},
        };
        if (!el || !el.textContent) {return empty;}
        try {
            return JSON.parse(el.textContent) as StatusPayload;
        } catch (e) {
            console.error('Failed to parse status payload', e);
            return empty;
        }
    }, [statusPayloadElementId]);

    const query = useLineSubmission(submissionId);
    if (query.isLoading) {return <p className='text-muted'>Loading submission…</p>;}
    if (query.isError) {
        return (
            <div className='alert alert-danger'>
                Failed to load submission: {(query.error as Error)?.message ?? 'unknown error'}
            </div>
        );
    }
    const submission = query.data ?? null;
    return (
        <SchemaForm
            submission={submission}
            mode='view'
            fieldStatus={payload.fieldStatus}
            sectionStatus={payload.sectionStatus}
            mutationFieldStatus={payload.mutationFieldStatus}
            mutationSectionStatus={payload.mutationSectionStatus}
            mutationOverallStatus={payload.mutationOverallStatus}
            geneFieldStatus={payload.geneFieldStatus}
            geneSectionStatus={payload.geneSectionStatus}
            lesionFieldStatus={payload.lesionFieldStatus}
            lesionSectionStatus={payload.lesionSectionStatus}
            assayFieldStatus={payload.assayFieldStatus}
            assaySectionStatus={payload.assaySectionStatus}
            phenotypeFieldStatus={payload.phenotypeFieldStatus}
            phenotypeSectionStatus={payload.phenotypeSectionStatus}
        />
    );
}
