import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import publicationStore from '../state/PublicationStore';

console.log("store state from PMT:", publicationStore.getState());
publicationStore.subscribe(
    () => {
        console.log("store state update from PMT:", publicationStore.getState());
    }
);

const PublicationMarkerTable = ({store, url}) => {
    const columns = [
        {
            label: 'Marker',
            content: row => <EntityLink key={row.zdbID} entity={row}/>,
            width: '250px',
        },
        {
            label: 'Marker Type',
            content: row => row.type,
            width: '200px',
        },
        {
            label: 'Name',
            content: row => row.name,
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

PublicationMarkerTable.propTypes = {
    url: PropTypes.string,
};

export default PublicationMarkerTable;
