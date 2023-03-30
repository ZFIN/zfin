import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import TermLink from '../components/entity/TermLink';
import {EntityList} from '../components/entity';

const PublicationDiseaseTable = ({url, title, navigationCounter}) => {
    const columns = [
        {
            label: 'Human Disease',
            content: row => <TermLink key={row.disease} entity={row.disease}/>,
        },
        {
            label: 'Fish',
            content: row => (<EntityList entities={row.fishList}/>),
            width: '300px',
        },
        {
            label: 'Environment',
            content: row => (<EntityList entities={row.environmentList}/>),
            width: '350px',
        },
        {
            label: 'Evidence',
            content: row => row.evidenceCodeString,
            width: '80px',
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

PublicationDiseaseTable.propTypes = {
    url: PropTypes.string,
    title: PropTypes.string,
    navigationCounter: PropTypes.shape({
        setCounts: PropTypes.func
    }),
};

export default PublicationDiseaseTable;
