import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';

const StrTable = ({url, title, navigationCounter}) => {
    const columns = [
        {
            label: 'Target',
            content: row => <EntityLink key={row.target.zdbID} entity={row.target}/>,
            width: '200px',
            filterName: 'targetName',
        },
        {
            label: 'Reagent',
            content: row => <EntityLink key={row.str.zdbID} entity={row.str}/>,
            width: '300px',
            filterName: 'strName',
        },
        {
            label: 'Reagent Type',
            content: row => row.str.type,
            filterName: 'strName',
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
            rowKey={row => row.target.zdbID + row.str.zdbID}
            pagination={true}
            onDataLoaded={handleDataLoadedCount}
        />
    );
};

StrTable.propTypes = {
    url: PropTypes.string,
    title: PropTypes.string,
    navigationCounter: PropTypes.shape({
        setCounts: PropTypes.func
    }),
};

export default StrTable;
