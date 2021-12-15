import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';

const PublicationAttributionTable = ({url}) => {
    const columns = [
        {
            label: 'Entity ID',
            content: (row) => row,
            width: '200px',
            filterName: 'entityID',
        },
    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={url}
            rowKey={row => row.zdbID}
            pagination={true}
        />
    );
};

PublicationAttributionTable.propTypes = {
    url: PropTypes.string,
};

export default PublicationAttributionTable;
