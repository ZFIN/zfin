import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import OrthologyTable from './OrthologyTable';

const PublicationOrthologyTable = ({url, title, navigationCounter}) => {
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

PublicationOrthologyTable.propTypes = {
    url: PropTypes.string,
    title: PropTypes.string,
    navigationCounter: PropTypes.shape({
        setCounts: PropTypes.func
    }),
};

export default PublicationOrthologyTable;
