import * as React from 'react';
import type { LineSubmissionDTO } from '../api/types';
import { AddPersonPicker } from './AddPersonPicker';

type Props = {
    submission: LineSubmissionDTO;
};

/**
 * Identity block under the page title — Line Designation, Submitter list,
 * PI list, Date Started. Editable Submitter / PI rows reuse
 * {@link AddPersonPicker} so curators can add people directly from the
 * header. Section-status cells live in the separate {@link StatusOverviewBar}
 * below.
 */
export function LineSubmissionHeader({ submission }: Props) {
    const labelStyle: React.CSSProperties = { fontWeight: 600, paddingRight: '0.75rem', whiteSpace: 'nowrap', verticalAlign: 'top' };
    const valueStyle: React.CSSProperties = { paddingBottom: '0.15rem' };
    const dash = <span className='text-muted'>&mdash;</span>;
    return (
        <table className='small mb-3' style={{ borderCollapse: 'collapse' }}>
            <tbody>
                <tr>
                    <td style={labelStyle}>ZDB ID</td>
                    <td style={valueStyle}>{submission.zdbID ?? dash}</td>
                </tr>
                <tr>
                    <td style={labelStyle}>Line Name</td>
                    <td style={valueStyle}>{submission.name ?? dash}</td>
                </tr>
                <tr>
                    <td style={labelStyle}>Submitter</td>
                    <td style={valueStyle}>
                        {submission.submitterNames ?? dash}
                        <AddPersonPicker
                            submissionZdbID={submission.zdbID}
                            endpoint='add-submitter'
                            roleLabel='submitter'
                        />
                    </td>
                </tr>
                <tr>
                    <td style={labelStyle}>PI</td>
                    <td style={valueStyle}>
                        {submission.piNames ?? dash}
                        <AddPersonPicker
                            submissionZdbID={submission.zdbID}
                            endpoint='add-pi'
                            roleLabel='PI'
                            autocompleteEndpoint='pis'
                        />
                    </td>
                </tr>
                <tr>
                    <td style={labelStyle}>Date Started</td>
                    <td style={valueStyle}>{submission.createdAt ?? dash}</td>
                </tr>
            </tbody>
        </table>
    );
}
