// Schema for the ZIRC line-submission editor. Consumed by
// LineSubmissionEdit.tsx via <FormRenderer schema={LINE_SUBMISSION_SCHEMA} …/>.
//
// The schema mirrors the LineSubmissionDTO wire shape: top-level fields
// for scalars, a multi-checkbox-with-other for reasons, an array for
// linkedFeatures (the only true repeater), and a CustomNode escape
// hatch for the mutations table (read-mostly with external Edit/Remove).

import React from 'react';
import {array, custom, field, section} from '../../components/form-renderer/builders';
import type {FormNode, OptionsCtx} from '../../components/form-renderer/types';
import MutationsSection from '../../components/zirc/MutationsSection';
import type {LineSubmissionDTO, LinkedFeatureDTO, MutationSummary} from '../LineSubmissionEdit';

// ─── Option lists ─────────────────────────────────────────────────────────

// Canonical-but-revisable list. The "Other" sentinel is rendered by the
// renderer (single-column mode); whatever the curator types lands in the
// same backgrounds column. The ZIRC team is expected to refine this
// enum; once stable, B10 will lock it down with a check constraint.
const BACKGROUND_OPTIONS = [
    {value: 'AB',      label: 'AB'},
    {value: 'TU',      label: 'TU'},
    {value: 'WIK',     label: 'WIK'},
    {value: 'AB/TU',   label: 'AB/TU'},
    {value: 'unknown', label: 'unknown'},
];

const REASON_OPTIONS = [
    {value: 'frequently_requested',     label: 'Currently frequently requested'},
    {value: 'expect_high_demand',       label: 'Expect high demand'},
    {value: 'interesting_gene',         label: 'Interesting gene'},
    {value: 'community_resource',       label: 'Community resource/tool'},
    {value: 'mutant_gene_cloned',       label: 'Mutant gene cloned'},
    {value: 'danger_of_losing',         label: 'Danger of losing line'},
    {value: 'lack_of_space_or_funding', label: 'Lack of space or funding to maintain line'},
];

// ─── Helpers ──────────────────────────────────────────────────────────────

function mutationLabel(m: MutationSummary): string {
    if (m.alleleDesignation && m.alleleDesignation.trim()) {
        return m.alleleDesignation;
    }
    return `#${m.sortOrder ?? '?'}`;
}

export function emptyLineSubmissionDTO(): LineSubmissionDTO {
    return {
        zdbID: '',
        name: null,
        abbreviation: null,
        previousNames: null,
        maternalBackground: null,
        paternalBackground: null,
        backgroundChangeable: null,
        backgroundChangeConcerns: null,
        unreportedFeaturesDetails: null,
        additionalInfo: null,
        singleAllelic: null,
        husbandryInfo: null,
        reasons: [],
        reasonsOther: null,
        linkedFeatures: [],
        mutations: [],
        isDraft: null,
    };
}

// ─── Schema ───────────────────────────────────────────────────────────────

export const LINE_SUBMISSION_SCHEMA: FormNode[] = [

    section('overview', 'Overview', [
        field({
            path: 'zdbID',
            label: 'ID',
            type: 'readonly',
            placeholder: '(assigned on first save)',
        }),
        field({path: 'name',           label: 'Name',            type: 'text'}),
        field({path: 'previousNames',  label: 'Previous Names',  type: 'text'}),
    ]),

    section('acceptance-reasons', 'Acceptance Reasons', [
        field({
            path: 'reasons',
            otherPath: 'reasonsOther',
            label: 'Why ZIRC should accept this line',
            type: 'multi-checkbox-with-other',
            options: REASON_OPTIONS,
        }),
    ]),

    section('mutations', 'Mutations', [
        // Read-mostly table with external Edit links and a Remove handler.
        // Doesn't fit a generic schema; use the renderer's escape hatch.
        custom({
            id: 'mutations-table',
            render: ({dto, actions}) => {
                const d = dto as LineSubmissionDTO;
                const remove = actions.removeMutation as
                    | ((id: number, label: string) => void)
                    | undefined;
                return (
                    <MutationsSection
                        submissionId={d.zdbID}
                        mutations={d.mutations ?? []}
                        onRemove={(id, label) => remove?.(id, label)}
                    />
                );
            },
        }),
    ]),

    section('linked-features', 'Linked Features', [
        array({
            id: 'linkedFeatures',
            path: 'linkedFeatures',
            title: 'Linked Features',
            emptyMessage: 'No linked features.',
            itemLabel: idx => `Linked feature ${idx + 1}`,
            newRow: () => ({
                mutationAId: null,
                mutationBId: null,
                distanceKnown: null,
                distanceValue: null,
                distanceUnit: null,
                additionalInfo: null,
            }),
            collapseWhen: row => {
                const r = row as Partial<LinkedFeatureDTO>;
                return r.mutationAId != null && r.mutationBId != null;
            },
            summarize: (row, {dto}) => {
                const r = row as Partial<LinkedFeatureDTO>;
                const muts = ((dto as LineSubmissionDTO).mutations ?? []);
                const lookup = (id: number | null | undefined) =>
                    id == null ? null : muts.find(m => m.id === id);
                const a = lookup(r.mutationAId);
                const b = lookup(r.mutationBId);
                const pair = `${a ? mutationLabel(a) : '?'} ↔ ${b ? mutationLabel(b) : '?'}`;
                if (r.distanceKnown === true && r.distanceValue != null && r.distanceUnit) {
                    return `${pair} · ${r.distanceValue} ${r.distanceUnit}`;
                }
                return pair;
            },
            addDisabledWhen: dto => {
                const muts = ((dto as LineSubmissionDTO).mutations ?? []);
                if (muts.length < 2) {
                    return {reason: 'Add at least two mutations first'};
                }
                return null;
            },
            childTemplate: [
                field({
                    path: 'mutationAId',
                    label: 'Mutation A',
                    type: 'select',
                    options: ({dto}: OptionsCtx) => {
                        const muts = ((dto as LineSubmissionDTO).mutations ?? []);
                        return muts.map(m => ({value: m.id, label: mutationLabel(m)}));
                    },
                }),
                field({
                    path: 'mutationBId',
                    label: 'Mutation B',
                    type: 'select',
                    options: ({dto, row}: OptionsCtx) => {
                        const muts = ((dto as LineSubmissionDTO).mutations ?? []);
                        const aId = (row as Partial<LinkedFeatureDTO> | undefined)?.mutationAId;
                        return muts
                            .filter(m => m.id !== aId)
                            .map(m => ({value: m.id, label: mutationLabel(m)}));
                    },
                }),
                field({path: 'distanceKnown', label: 'Distance known?', type: 'bool'}),
                field({
                    path: 'distanceValue',
                    label: 'Distance',
                    type: 'int',
                    visible: (_dto, row) =>
                        (row as Partial<LinkedFeatureDTO> | undefined)?.distanceKnown === true,
                }),
                field({
                    path: 'distanceUnit',
                    label: 'Unit',
                    type: 'select',
                    options: [
                        {value: 'cM', label: 'cM'},
                        {value: 'Mb', label: 'Mb'},
                    ],
                    visible: (_dto, row) =>
                        (row as Partial<LinkedFeatureDTO> | undefined)?.distanceKnown === true,
                }),
                field({path: 'additionalInfo', label: 'Additional info', type: 'textarea'}),
            ],
        }),
    ]),

    section('background', 'Background', [
        field({path: 'singleAllelic', label: 'Single-allelic submission', type: 'bool'}),
        field({
            path: 'maternalBackground',
            label: 'Maternal',
            type: 'select-with-other',
            options: BACKGROUND_OPTIONS,
        }),
        field({
            path: 'paternalBackground',
            label: 'Paternal',
            type: 'select-with-other',
            options: BACKGROUND_OPTIONS,
        }),
        field({path: 'backgroundChangeable', label: 'Background Changeable', type: 'bool'}),
        field({
            path: 'backgroundChangeConcerns',
            label: 'Concerns',
            type: 'textarea',
            visible: dto => (dto as LineSubmissionDTO).backgroundChangeable === false,
        }),
    ]),

    section('additional-info', 'Additional Info', [
        field({path: 'unreportedFeaturesDetails', label: 'Unreported Features Details', type: 'textarea'}),
        field({
            path: 'husbandryInfo',
            label: 'Husbandry Info',
            type: 'textarea',
            placeholder: 'Husbandry-specific information, e.g. special feeding regime',
        }),
        field({path: 'additionalInfo', label: 'Additional Info', type: 'textarea'}),
    ]),
];
