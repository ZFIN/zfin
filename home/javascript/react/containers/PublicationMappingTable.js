import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import DisplayLocation from '../components/DisplayLocation';

const PublicationMappingTable = ({url}) => {
    const columns = [
        {
            label: 'Entity Type',
            content: row => row.entity.type,
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
    return (
        <DataTable
            columns={columns}
            dataUrl={url}
            rowKey={row => row.zdbID}
            pagination={true}
        />
    );
};

PublicationMappingTable.propTypes = {
    url: PropTypes.string,
};

export default PublicationMappingTable;
