import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/DataTable';

const MarkerCitationsTable = ({markerId}) => {
    const columns = [
        {
            label: 'Citation',
            content: ({citation, zdbID}) => <a href={'/' + zdbID} dangerouslySetInnerHTML={{__html: citation}} />,
        },
    ];

    const sortOptions = [
        'Year, Newest',
        'Year, Oldest',
        'First Author, A to Z',
        'First Author, Z to A',
    ];

    const downloadOptions = [
        {
            format: 'TSV',
            url: `/action/api/marker/${markerId}/citations.tsv`,
        },
    ];

    return (
        <DataTable
            columns={columns}
            downloadOptions={downloadOptions}
            rowKey='zdbID'
            sortOptions={sortOptions}
            url={`/action/api/marker/${markerId}/citations`}
        />
    );
};

MarkerCitationsTable.propTypes = {
    markerId: PropTypes.string,
};

export default MarkerCitationsTable;
