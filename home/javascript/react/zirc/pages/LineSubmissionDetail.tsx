import * as React from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from '../queryClient';
import { useLineSubmission } from '../api/queries';
import { SchemaForm } from '../schemaForm/SchemaForm';
import { StatusOverviewBar } from '../components/StatusOverviewBar';
import { StatusRefetchContext } from '../statusRefetchContext';
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
    const initialPayload = React.useMemo<StatusPayload>(() => {
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

    // Reactive copy so a comment change can refresh the badges in place.
    const [payload, setPayload] = React.useState<StatusPayload>(initialPayload);

    // Re-pull the server-computed status (incl. the open-comment IN_PROGRESS
    // overlay + section/overall rollups) without a full page reload. The
    // status endpoint lives under /action/zirc (not the /action/api/zirc api
    // client base), so we fetch it directly.
    const refetchStatus = React.useCallback(() => {
        if (!submissionId) {return;}
        fetch(`/action/zirc/line-submission/${encodeURIComponent(submissionId)}/status`, {
            headers: { Accept: 'application/json' },
        })
            .then((r) => (r.ok ? r.json() : Promise.reject(new Error(`HTTP ${r.status}`))))
            .then((p: StatusPayload) => setPayload(p))
            .catch((e) => console.error('Status refetch failed', e));
    }, [submissionId]);

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
        <StatusRefetchContext.Provider value={refetchStatus}>
            {submission && (
                <StatusOverviewBar
                    submission={submission}
                    sectionStatus={payload.sectionStatus}
                />
            )}
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
        </StatusRefetchContext.Provider>
    );
}
