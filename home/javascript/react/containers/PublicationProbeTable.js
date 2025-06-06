import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';

const PublicationProbeTable = ({url, publicationId, navigationCounter, title}) => {
    const columns = [
        {
            label: 'Probe',
            content: row => <EntityLink key={row.zdbID} entity={row}/>,
            filterName: 'symbol',
            width: '250px',
        },
        {
            label: 'Type',
            content: row => row.cloneType,
            filterName: 'type',
            filterOptionFromSupplementalData: 'probeTypes',
            width: '100px',
        },
        {
            label: 'All Figures',
            content: row => <a href={`/action/publication/${publicationId}/all-figures?probeZdbID=${row.zdbID}`}>Show all Figures</a>,
        },
    ];

    const handleDataLoadedCount = (data) => {
        if (navigationCounter && navigationCounter.setCounts && data.total) {
            navigationCounter.setCounts(title, data.total);
        }
    };

    return (
        <DataTable
            columns={columns}
            dataUrl={url}
            rowKey={row => row.zdbID}
            pagination={true}
            onDataLoaded={handleDataLoadedCount}
        />
    );
};

PublicationProbeTable.propTypes = {
    url: PropTypes.string,
    publicationId: PropTypes.string,
    title: PropTypes.string,
    navigationCounter: PropTypes.shape({
        setCounts: PropTypes.func
    }),
};

export default PublicationProbeTable;
