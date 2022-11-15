import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import EntityGroupList from '../components/entity/EntityGroupList';
import TermLink from '../components/entity/TermLink';

const PublicationDiseaseTable = ({url, title, navigationCounter}) => {
    const columns = [
        {
            label: 'Human Disease',
            content: row => <TermLink key={row.disease} entity={row.disease}/>,
        },
        {
            label: 'Fish',
            content: row => (<EntityGroupList entities={row.fishList} showLink={true} stringOnly={false}/>),
            width: '300px',
        },
        {
            label: 'Environment',
            content: row => (<EntityGroupList entities={row.environmentList} showLink={false} stringOnly={false}/>),
            width: '350px',
        },
        {
            label: 'Evidence',
            content: row => (<EntityGroupList entities={row.evidenceCodeList} showLink={false} stringOnly={true}/>),
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
};

export default PublicationDiseaseTable;
