import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import DisplayLocation from '../components/DisplayLocation';

const PublicationMappingTable = ({url, title, navigationCounter}) => {
    const columns = [
        {
            label: 'Entity Type',
            content: row => row.entity.entityType,
            width: '250px',
        },
        {
            label: 'Entity Symbol',
            content: row => <EntityLink key={row.entity} entity={row.entity}/>,
            width: '200px',
        },
        {
            label: 'Location',
            content: row => <DisplayLocation entity={row}/>,
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

PublicationMappingTable.propTypes = {
    url: PropTypes.string,
    title: PropTypes.string,
    navigationCounter: PropTypes.shape({
        setCounts: PropTypes.func
    }),
};

export default PublicationMappingTable;
