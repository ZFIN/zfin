import React, {useCallback} from 'react';
import SaveToast from '../components/zirc/SaveToast';
import FormRenderer from '../components/form-renderer/FormRenderer';
import {useAutosavedForm} from '../components/form-renderer/useAutosavedForm';
import {
    emptyLineSubmissionDTO,
    LINE_SUBMISSION_SCHEMA,
} from './zirc/lineSubmissionSchema';

// ─── Wire types (consumed by extracted section files + the schema) ────────

export interface LinkedFeatureDTO {
    mutationAId: number | null;
    mutationBId: number | null;
    /** Server-supplied display echo; absent on outbound. */
    mutationALabel?: string | null;
    mutationBLabel?: string | null;
    distanceKnown: boolean | null;
    /** Distance is exposed on the wire as a unified value + unit pair.
     *  Storage keeps two columns (cM, Mb) for legacy reasons; the
     *  server DTO converter handles the mapping. */
    distanceValue: number | null;
    /** "cM" or "Mb" — anything else with a non-null value is rejected
     *  server-side as "no distance". */
    distanceUnit: string | null;
    additionalInfo: string | null;
}

export interface MutationSummary {
    id: number;
    sortOrder: number | null;
    alleleDesignation: string | null;
    mutagenesisProtocol: string | null;
    mutationType: string | null;
    mutationDiscoverer: string | null;
}

export interface LineSubmissionDTO {
    zdbID: string;
    name: string | null;
    abbreviation: string | null;
    previousNames: string | null;
    maternalBackground: string | null;
    paternalBackground: string | null;
    backgroundChangeable: boolean | null;
    backgroundChangeConcerns: string | null;
    unreportedFeaturesDetails: string | null;
    additionalInfo: string | null;
    singleAllelic: boolean | null;
    husbandryInfo: string | null;
    reasons: string[] | null;
    reasonsOther: string | null;
    linkedFeatures: LinkedFeatureDTO[] | null;
    mutations: MutationSummary[] | null;
    isDraft: boolean | null;
}

interface LineSubmissionEditProps {
    /** Empty string for the /new flow — the first save creates the row. */
    submissionId: string;
}

// ─── Path label map for SaveToast ─────────────────────────────────────────

const PATH_LABELS: Record<string, string> = {
    name: 'Name',
    previousNames: 'Previous Names',
    singleAllelic: 'Single-allelic submission',
    maternalBackground: 'Maternal',
    paternalBackground: 'Paternal',
    backgroundChangeable: 'Background Changeable',
    backgroundChangeConcerns: 'Concerns',
    unreportedFeaturesDetails: 'Unreported Features Details',
    husbandryInfo: 'Husbandry Info',
    additionalInfo: 'Additional Info',
    reasons: 'Acceptance Reasons',
    reasonsOther: 'Acceptance Reasons',
    linkedFeatures: 'Linked Features',
};

function labelForPath(path: string): string {
    return PATH_LABELS[path] ?? path;
}

// ─── Save dispatch ────────────────────────────────────────────────────────

async function callPatch(id: string, path: string, value: unknown): Promise<LineSubmissionDTO> {
    // Client-side backstop for linkedFeatures: drop rows whose pair isn't
    // fully picked. The server does stricter validation; this just avoids
    // sending obviously-empty rows.
    let payloadValue = value;
    if (path === 'linkedFeatures' && Array.isArray(value)) {
        payloadValue = (value as LinkedFeatureDTO[])
            .filter(f => f.mutationAId != null && f.mutationBId != null);
    }
    const resp = await fetch('/action/zirc/line-submission/patch', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({zdbID: id || null, path, value: payloadValue}),
    });
    if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}`);
    }
    return resp.json() as Promise<LineSubmissionDTO>;
}

// ─── Container ────────────────────────────────────────────────────────────

const LineSubmissionEdit = ({submissionId}: LineSubmissionEditProps) => {
    const save = useCallback(async (
        id: string,
        dto: LineSubmissionDTO,
        path: string,
        value: unknown,
    ): Promise<LineSubmissionDTO> => {
        if (path === 'mutations') {
            // Mutations are read-only via the schema; defensive no-op.
            return dto;
        }
        // Single unified endpoint handles scalars, reasons (+other),
        // and linkedFeatures via path-based dispatch on the server.
        return callPatch(id, path, value);
    }, []);

    const load = useCallback(async (id: string): Promise<LineSubmissionDTO> => {
        const r = await fetch(`/action/zirc/line-submission/${id}.json`);
        if (!r.ok) {
            throw new Error(`HTTP ${r.status}`);
        }
        return r.json() as Promise<LineSubmissionDTO>;
    }, []);

    const onIdAssigned = useCallback((id: string) => {
        window.history.replaceState(null, '', `/action/zirc/line-submission/${id}/edit`);
    }, []);

    const form = useAutosavedForm<LineSubmissionDTO>({
        initialId: submissionId,
        emptyDto: emptyLineSubmissionDTO,
        load,
        save,
        onIdAssigned,
        labelForPath,
    });

    const removeMutation = useCallback(async (id: number, label: string) => {
        // eslint-disable-next-line no-alert
        if (!window.confirm(`Remove mutation "${label}"? This cannot be undone.`)) {
            return;
        }
        try {
            const resp = await fetch(`/action/zirc/mutation/${id}/delete`, {method: 'POST'});
            if (!resp.ok) {
                throw new Error(`HTTP ${resp.status}`);
            }
            const data = await resp.json() as LineSubmissionDTO;
            // Patch only the mutations array from the response rather than
            // replacing the entire dto -- in-flight scalar edits to other
            // fields would be clobbered by a full replace.
            if (form.dto) {
                form.setDto({...form.dto, mutations: data.mutations ?? []});
            }
        } catch (e) {
            // Surface the failure through the same toast path as other saves.
            // Reusing the seqRef-backed setSaveEvent isn't directly available
            // through the hook; a follow-up could expose `emit` for this.
            // eslint-disable-next-line no-alert
            window.alert(`Failed to remove mutation: ${e instanceof Error ? e.message : 'unknown error'}`);
        }
    }, [form]);

    if (form.loadError) {
        return <div className='alert alert-danger'>Failed to load submission: {form.loadError}</div>;
    }
    if (!form.dto) {
        return <div className='text-muted'>Loading…</div>;
    }

    const dto = form.dto;
    return <>
        <FormRenderer
            schema={LINE_SUBMISSION_SCHEMA}
            value={dto}
            onChange={(path, value) => {
                form.setDto({...dto, [path]: value} as LineSubmissionDTO);
            }}
            onCommit={form.commit}
            actions={{removeMutation: removeMutation as unknown as (...args: unknown[]) => unknown}}
        />
        <SaveToast event={form.saveEvent}/>
    </>;
};

export default LineSubmissionEdit;
