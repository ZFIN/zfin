import React from 'react';
import type {MutationSummary} from '../../containers/LineSubmissionEdit';

interface MutationsSectionProps {
    submissionId: string;
    mutations: MutationSummary[];
    onRemove: (id: number, label: string) => void;
}

/** Mirrors LineSubmissionService.MAX_MUTATIONS_PER_SUBMISSION. */
const MAX_MUTATIONS = 5;

const MutationsSection = ({submissionId, mutations, onRemove}: MutationsSectionProps) => {
    // When the parent submission already exists, add under it. When the
    // curator is on /new with no id yet, hit the create-both-in-one
    // endpoint so they don't have to type anything in Overview first.
    const addUrl = submissionId
        ? `/action/zirc/line-submission/${encodeURIComponent(submissionId)}/mutation/new`
        : '/action/zirc/line-submission/new-with-mutation';
    const atCap = mutations.length >= MAX_MUTATIONS;
    const capReason = `Maximum ${MAX_MUTATIONS} mutations per submission.`;
    return (
        <div>
            {mutations.length === 0
                ? <p className='text-muted'>No mutations recorded for this submission.</p>
                : <table className='table table-striped'>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Allele Designation</th>
                            <th>Mutagenesis Protocol</th>
                            <th>Mutation Type</th>
                            <th>Discoverer</th>
                            <th className='text-right'>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {mutations.map(m => {
                            const label = m.alleleDesignation && m.alleleDesignation.trim()
                                ? m.alleleDesignation
                                : `#${m.sortOrder ?? '?'}`;
                            return (
                                <tr key={m.id}>
                                    <td>{m.sortOrder ?? '—'}</td>
                                    <td>{m.alleleDesignation ?? <span className='text-muted'>—</span>}</td>
                                    <td>{m.mutagenesisProtocol ?? <span className='text-muted'>—</span>}</td>
                                    <td>{m.mutationType ?? <span className='text-muted'>—</span>}</td>
                                    <td>{m.mutationDiscoverer ?? <span className='text-muted'>—</span>}</td>
                                    <td className='text-right'>
                                        <a
                                            className='btn btn-sm btn-outline-primary mr-2'
                                            href={`/action/zirc/mutation/${m.id}/edit`}
                                        >
                                            Edit
                                        </a>
                                        <button
                                            type='button'
                                            className='btn btn-sm btn-outline-danger'
                                            onClick={() => onRemove(m.id, label)}
                                        >
                                            Remove
                                        </button>
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            }
            {addUrl && !atCap
                ? <a href={addUrl} className='btn btn-sm btn-outline-secondary'>+ Add mutation</a>
                : (
                    <button
                        type='button'
                        className='btn btn-sm btn-outline-secondary'
                        disabled
                        title={atCap ? capReason : 'Save a field on this submission first'}
                    >
                        + Add mutation
                    </button>
                )}
        </div>
    );
};

export default MutationsSection;
