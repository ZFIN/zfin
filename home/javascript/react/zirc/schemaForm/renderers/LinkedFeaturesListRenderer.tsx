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
import { LinkedFeatureDTO, MutationDTO } from '../../api/types';
import {
    useAddLinkedFeature,
    useDeleteLinkedFeature,
    usePatchLinkedFeature,
} from '../../api/queries';
import { viewConfigFrom } from '../useViewConfig';

/**
 * Renders pairwise linkages between mutations on the submission page.
 *
 * <p>Each row carries (mutationA, mutationB, distanceKnown,
 * distanceValue+unit, additionalInfo). A/B are picked from a dropdown
 * of mutations already on the submission — there's no cross-submission
 * linkage. Add/Delete go through dedicated endpoints; per-field edits
 * fire PATCHes on commit (blur for text, change for selects/radios).
 *
 * <p>Distance handling: the entity carries two columns
 * (distanceCentimorgans, distanceMegabases). The renderer presents one
 * (value, unit) combo. Switching units PATCHes both columns — the
 * previously-active one to null, the newly-active one to the value.
 *
 * <p>Submission id is threaded through {@code config.submissionId} by
 * SchemaForm; mutations list comes through {@code config.mutations}.
 */
function LinkedFeaturesListRenderer({ data, config }: ControlProps) {
    const links = (data as LinkedFeatureDTO[] | undefined) ?? [];
    const cfg = (config as
        { submissionId?: string; mutations?: MutationDTO[] } | undefined) ?? {};
    const submissionId = cfg.submissionId;
    const mutations = cfg.mutations ?? [];
    const view = viewConfigFrom(config);

    if (view.readonly) {
        if (links.length === 0) {
            return <p className='text-muted'>No linked features.</p>;
        }
        const nameFor = (id: number) =>
            mutations.find((m) => m.id === id)?.alleleDesignation ?? `#${id}`;
        return (
            <ul className='list-unstyled'>
                {links.map((lf) => (
                    <li key={`${lf.mutationAId}-${lf.mutationBId}`}>
                        {nameFor(lf.mutationAId)} ↔ {nameFor(lf.mutationBId)}
                    </li>
                ))}
                <li className='small text-muted mt-2'>
                    Detailed read-only linked-feature view not yet ported.
                </li>
            </ul>
        );
    }

    const add = useAddLinkedFeature();
    const remove = useDeleteLinkedFeature();
    const patch = usePatchLinkedFeature();

    // The "add" panel sits below the list; user picks A and B, hits Add.
    const [pendingA, setPendingA] = React.useState<number | ''>('');
    const [pendingB, setPendingB] = React.useState<number | ''>('');
    const [addError, setAddError] = React.useState<string | null>(null);

    const handleAdd = () => {
        if (!submissionId || pendingA === '' || pendingB === '' || pendingA === pendingB) {
            setAddError('Pick two distinct mutations.');
            return;
        }
        setAddError(null);
        add.mutate(
            { submissionId, mutationAId: pendingA, mutationBId: pendingB },
            {
                onSuccess: () => { setPendingA(''); setPendingB(''); },
                onError: (e) => setAddError(e instanceof Error ? e.message : 'Add failed'),
            },
        );
    };

    const handleDelete = (lf: LinkedFeatureDTO) => {
        if (!submissionId) {return;}
        // eslint-disable-next-line no-alert
        if (!window.confirm('Delete this linkage? This action cannot be undone.')) {return;}
        remove.mutate({ submissionId, aId: lf.mutationAId, bId: lf.mutationBId });
    };

    const handlePatch = (lf: LinkedFeatureDTO, path: string, value: unknown) => {
        if (!submissionId) {return;}
        patch.mutate({ submissionId, aId: lf.mutationAId, bId: lf.mutationBId, path, value });
    };

    const mutationLabel = (id: number) => {
        const m = mutations.find((x) => x.id === id);
        if (!m) {return `Mutation #${id}`;}
        return m.alleleDesignation || `Mutation #${m.sortOrder}`;
    };

    // Compute the currently-active distance unit + value from the
    // entity's two columns. Both null → "unknown distance" state.
    const distanceValueOf = (lf: LinkedFeatureDTO): { value: number | null; unit: 'cM' | 'Mb' | null } => {
        if (lf.distanceCentimorgans != null) {return { value: lf.distanceCentimorgans, unit: 'cM' };}
        if (lf.distanceMegabases != null)    {return { value: lf.distanceMegabases,    unit: 'Mb' };}
        return { value: null, unit: null };
    };

    const handleDistanceValue = (lf: LinkedFeatureDTO, raw: string) => {
        const { unit } = distanceValueOf(lf);
        const next: number | null = raw.trim() === '' ? null : Number(raw);
        if (next !== null && Number.isNaN(next)) {return;}
        // Default to cM if no unit picked yet but a value is being entered.
        const targetUnit = unit ?? (next !== null ? 'cM' : null);
        if (targetUnit === 'cM') {
            handlePatch(lf, '/distanceCentimorgans', next);
            if (lf.distanceMegabases != null) {handlePatch(lf, '/distanceMegabases', null);}
        } else if (targetUnit === 'Mb') {
            handlePatch(lf, '/distanceMegabases', next);
            if (lf.distanceCentimorgans != null) {handlePatch(lf, '/distanceCentimorgans', null);}
        }
    };

    const handleDistanceUnit = (lf: LinkedFeatureDTO, nextUnit: 'cM' | 'Mb') => {
        const { value } = distanceValueOf(lf);
        if (nextUnit === 'cM') {
            handlePatch(lf, '/distanceCentimorgans', value);
            handlePatch(lf, '/distanceMegabases', null);
        } else {
            handlePatch(lf, '/distanceMegabases', value);
            handlePatch(lf, '/distanceCentimorgans', null);
        }
    };

    return (
        <div>
            {links.length === 0 ? (
                <p className='text-muted'>No linked features recorded for this submission.</p>
            ) : (
                <ul className='list-unstyled'>
                    {links.map((lf) => {
                        const { value, unit } = distanceValueOf(lf);
                        return (
                            <li
                                key={`${lf.mutationAId}-${lf.mutationBId}`}
                                className='border rounded p-2 mb-2'
                            >
                                <div className='d-flex justify-content-between align-items-center mb-2'>
                                    <strong>
                                        {mutationLabel(lf.mutationAId)} ↔ {mutationLabel(lf.mutationBId)}
                                    </strong>
                                    <button
                                        type='button'
                                        className='btn btn-sm btn-outline-danger'
                                        onClick={() => handleDelete(lf)}
                                        disabled={remove.isPending}
                                    >
                                        Delete
                                    </button>
                                </div>
                                <div className='form-group row mb-2'>
                                    <label className='col-sm-3 col-form-label'>Distance Known</label>
                                    <div className='col-sm-9' role='radiogroup'>
                                        {[
                                            ['Yes', true],
                                            ['No', false],
                                            ['Unknown', null],
                                        ].map(([lbl, val]) => (
                                            <label key={String(lbl)} className='form-check-inline'>
                                                <input
                                                    type='radio'
                                                    name={`fr-distanceKnown-${lf.mutationAId}-${lf.mutationBId}`}
                                                    checked={lf.distanceKnown === val}
                                                    onChange={() => handlePatch(lf, '/distanceKnown', val)}
                                                />{' '}{lbl}
                                            </label>
                                        ))}
                                    </div>
                                </div>
                                <div className='form-group row mb-2'>
                                    <label className='col-sm-3 col-form-label'>Distance</label>
                                    <div className='col-sm-9 d-flex align-items-center'>
                                        <input
                                            type='number'
                                            step='any'
                                            className='form-control'
                                            style={{ maxWidth: 160 }}
                                            defaultValue={value ?? ''}
                                            onBlur={(e) => handleDistanceValue(lf, e.target.value)}
                                            disabled={lf.distanceKnown !== true}
                                        />
                                        <select
                                            className='form-control ml-2'
                                            style={{ maxWidth: 100 }}
                                            value={unit ?? 'cM'}
                                            onChange={(e) =>
                                                handleDistanceUnit(lf, e.target.value as 'cM' | 'Mb')
                                            }
                                            disabled={lf.distanceKnown !== true}
                                        >
                                            <option value='cM'>cM</option>
                                            <option value='Mb'>Mb</option>
                                        </select>
                                    </div>
                                </div>
                                <div className='form-group row mb-0'>
                                    <label className='col-sm-3 col-form-label'>Additional Info</label>
                                    <div className='col-sm-9'>
                                        <textarea
                                            className='form-control'
                                            rows={2}
                                            defaultValue={lf.additionalInfo ?? ''}
                                            onBlur={(e) =>
                                                handlePatch(lf, '/additionalInfo', e.target.value || null)
                                            }
                                        />
                                    </div>
                                </div>
                            </li>
                        );
                    })}
                </ul>
            )}

            {/* Add panel — only useful when there are >=2 mutations to link. */}
            {mutations.length >= 2 ? (
                <div className='border rounded p-2 bg-light'>
                    <div className='d-flex align-items-center'>
                        <span className='mr-2'>Link:</span>
                        <select
                            className='form-control mr-2'
                            style={{ maxWidth: 240 }}
                            value={pendingA}
                            onChange={(e) => setPendingA(e.target.value === '' ? '' : Number(e.target.value))}
                        >
                            <option value=''>— pick mutation —</option>
                            {mutations.map((m) => (
                                <option key={m.id} value={m.id}>
                                    {m.alleleDesignation || `Mutation #${m.sortOrder}`}
                                </option>
                            ))}
                        </select>
                        <span className='mr-2'>↔</span>
                        <select
                            className='form-control mr-2'
                            style={{ maxWidth: 240 }}
                            value={pendingB}
                            onChange={(e) => setPendingB(e.target.value === '' ? '' : Number(e.target.value))}
                        >
                            <option value=''>— pick mutation —</option>
                            {mutations.map((m) => (
                                <option key={m.id} value={m.id}>
                                    {m.alleleDesignation || `Mutation #${m.sortOrder}`}
                                </option>
                            ))}
                        </select>
                        <button
                            type='button'
                            className='btn btn-sm btn-outline-secondary'
                            onClick={handleAdd}
                            disabled={!submissionId || add.isPending
                                || pendingA === '' || pendingB === '' || pendingA === pendingB}
                        >
                            + Add link
                        </button>
                    </div>
                    {addError && (
                        <div className='text-danger small mt-1'>{addError}</div>
                    )}
                </div>
            ) : (
                <p className='text-muted small'>
                    Add at least two mutations to this submission before linking them.
                </p>
            )}
        </div>
    );
}

export const linkedFeaturesListRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(20, and(isControl, optionIs('widget', 'linkedFeaturesList'))),
    renderer: withJsonFormsControlProps(LinkedFeaturesListRenderer),
};
