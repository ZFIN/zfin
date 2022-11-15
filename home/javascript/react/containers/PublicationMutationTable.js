import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink, EntityList} from '../components/entity';

const PublicationMarkerTable = ({url, title, navigationCounter}) => {
    const columns = [
        {
            label: 'Allele',
            content: row => <EntityLink key={row.zdbID} entity={row} />,
            width: '200px',
        },
        {
            label: 'Construct',
            content: row => (<EntityList entities={row.tgConstructs}/>),
            width: '250px',
        },
        {
            label: 'Type',
            content: row => row.type.display,
        },
        {
            label: 'Affected Genomic Region',
            content: row => (<EntityList entities={row.affectedGenes}/>),
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

PublicationMarkerTable.propTypes = {
    url: PropTypes.string,
};

export default PublicationMarkerTable;
