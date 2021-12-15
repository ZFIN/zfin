import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';

const StrTable = ({url}) => {
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
    return (
        <DataTable
            columns={columns}
            dataUrl={url}
            rowKey={row => row.zdbID}
            pagination={true}
        />
    );
};

StrTable.propTypes = {
    url: PropTypes.string,
};

export default StrTable;
