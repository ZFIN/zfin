import React from 'react';
import PropTypes from 'prop-types';
import TimeAgo from 'react-timeago'

const formatter = (value, unit) => `${value} ${unit}${value !== 1 ? 's': ''}`;

class DashboardPubList extends React.Component {
    render() {
        const { pubs, statusCounts } = this.props;
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
                {Object.entries(pubMap).map(([status, pubs]) => (
                    <div key={status}>
                        <h4>{status} ({statusCounts[status]})</h4>
                        <table className="table">
                            <thead>
                            <tr>
                                <th width="150px">ZDB-ID</th>
                                <th>Title</th>
                                <th width="100px">Last Status Change</th>
                                {status === 'Waiting for Author' && <th width="130px">Last Correspondence</th>}
                                <th width="150px">Status</th>
                                <th width="100px">Owner</th>
                                <th width="40px" />
                            </tr>
                            </thead>
                            <tbody>
                            {pubs.map(pub => (
                                <tr key={pub.zdbId}>
                                    <td>
                                        <a href={`/${pub.zdbId}`} target="_blank">{pub.zdbId}</a>
                                    </td>
                                    <td>
                                        <p><b dangerouslySetInnerHTML={{__html: pub.title}} /></p>
                                    </td>
                                    <td><TimeAgo date={pub.status.updateDate} formatter={formatter} /></td>
                                    {status === 'Waiting for Author' && <td><TimeAgo date={pub.lastCorrespondenceDate} formatter={formatter} /></td>}
                                    <td>{pub.status.status.name}</td>
                                    <td>{pub.status.owner.name}</td>
                                    <td>
                                        <div className="dropdown">
                                            <button className="btn btn-link dropdown-toggle" type="button" data-toggle="dropdown">
                                                <i className="fas fa-ellipsis-v" />
                                            </button>
                                            <ul className="dropdown-menu dropdown-menu-right">
                                                <li><a href={`/action/curation/${pub.zdbId}`} target="_blank">Curate</a></li>
                                                <li><a href={`/action/publication/${pub.zdbId}/track`} target="_blank">Track</a></li>
                                                <li><a href={`/action/publication/${pub.zdbId}/link`} target="_blank">Link</a></li>
                                                <li><a href={`/action/publication/${pub.zdbId}/edit`} target="_blank">Edit</a></li>
                                            </ul>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                ))}
            </React.Fragment>
        );
    }
}

DashboardPubList.propTypes = {
    pubs: PropTypes.array,
    statusCounts: PropTypes.object,
};

export default DashboardPubList;
