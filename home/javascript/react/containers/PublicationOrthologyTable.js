import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import OrthologyTable from './OrthologyTable';

const PublicationOrthologyTable = ({url}) => {
    const columns = [
        {
            label: 'Gene',
            content: row => <EntityLink key={row.marker.zdbID} entity={row.marker}/>,
            width: '80px',
        },
        {
            label: 'Orthology',
            content: row => <OrthologyTable geneId={row.marker.zdbID} showDownload={false}/>,
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

PublicationOrthologyTable.propTypes = {
    url: PropTypes.string,
};

export default PublicationOrthologyTable;
