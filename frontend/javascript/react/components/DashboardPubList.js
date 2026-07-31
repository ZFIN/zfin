import React from 'react';
import PropTypes from 'prop-types';
import RelativeDate from './RelativeDate';
import ProcessingTaskList from './ProcessingTaskList';

const DashboardPubList = ({ pubs, statusCounts }) => {
    if (!pubs || !pubs.length) {
        return null;
    }
    const pubMap = {};
    pubs.forEach(pub => {
        const key = pub.status.status.name;
        if (!(key in pubMap)) {
            pubMap[key] = [];
        }
        pubMap[key].push(pub);
    });

    return (
        <React.Fragment>
            {Object.entries(pubMap).map(([status, pubs]) => {
                const hasCorrespondenceColumn = status === 'Waiting for Author' || status === 'Manual PDF Acquisition Needed';
                const hasPriorityColumn = status === 'Manual PDF Acquisition Needed';
                const hasCurateAction = status !== 'Processing' && status !== 'Manual PDF Acquisition Needed';
                return (
                    <div key={status}>
                        <h4>{status} ({statusCounts[status]})</h4>
                        <table className='table'>
                            <thead>
                                <tr>
                                    <th width='150px'>ZDB-ID</th>
                                    <th>Title</th>
                                    <th width='100px'>Last Status Change</th>
                                    {hasCorrespondenceColumn && <th width='130px'>Last Correspondence</th>}
                                    {hasCorrespondenceColumn && <th width='130px'># Correspondences</th>}
                                    <th width='150px'>Status</th>
                                    {hasPriorityColumn && <th width='80px'>Priority</th>}
                                    <th width='100px'>Owner</th>
                                    <th width='40px' />
                                </tr>
                            </thead>
                            <tbody>
                                {pubs.map(pub => (
                                    <tr key={pub.zdbId}>
                                        <td>
                                            <a href={`/${pub.zdbId}`} target='_blank' rel='noopener noreferrer'>{pub.zdbId}</a>
                                        </td>
                                        <td>
                                            <p><b dangerouslySetInnerHTML={{__html: pub.title}} /></p>
                                            {status === 'Processing' && <ProcessingTaskList pub={pub} />}
                                        </td>
                                        <td><RelativeDate date={pub.status.updateDate} /></td>
                                        {hasCorrespondenceColumn && <td><RelativeDate date={pub.lastCorrespondenceDate} /></td>}
                                        {hasCorrespondenceColumn && <td>{pub.numberOfCorrespondences}</td>}
                                        <td>{pub.status.status.name}</td>
                                        {hasPriorityColumn && <td>{pub.status.location ? pub.status.location.name : 'Not Set'}</td>}
                                        <td>{pub.status.owner.name}</td>
                                        <td>
                                            <div className='dropdown'>
                                                <button className='btn btn-link' type='button' data-toggle='dropdown'>
                                                    <i className='fas fa-ellipsis-v' />
                                                </button>
                                                <div className='dropdown-menu dropdown-menu-right'>
                                                    {hasCurateAction && <a className='dropdown-item' href={`/action/curation/${pub.zdbId}`} target='_blank' rel='noopener noreferrer'>Curate</a>}
                                                    <a className='dropdown-item' href={`/action/publication/${pub.zdbId}/track`} target='_blank' rel='noopener noreferrer'>Track</a>
                                                    <a className='dropdown-item' href={`/action/publication/${pub.zdbId}/link`} target='_blank' rel='noopener noreferrer'>Link</a>
                                                    <a className='dropdown-item' href={`/action/publication/${pub.zdbId}/edit`} target='_blank' rel='noopener noreferrer'>Edit</a>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )
            })}
        </React.Fragment>
    );
};

DashboardPubList.propTypes = {
    pubs: PropTypes.array,
    statusCounts: PropTypes.object,
};

export default DashboardPubList;
