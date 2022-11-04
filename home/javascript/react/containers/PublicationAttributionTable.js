import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';

const PublicationAttributionTable = ({url, title, navigationCounter}) => {
    const columns = [
        {
            label: 'Entity ID',
            content: (row) => row,
            width: '200px',
            filterName: 'entityID',
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
            rowKey={row => row}
            pagination={true}
            onLoadedCount={handleDataLoadedCount}
        />
    );
};

PublicationAttributionTable.propTypes = {
    url: PropTypes.string,
};

export default PublicationAttributionTable;
