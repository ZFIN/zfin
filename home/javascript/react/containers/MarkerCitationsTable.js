import React from 'react';
import PropTypes from 'prop-types';
import { DataList } from '../components/data-table';

const MarkerCitationsTable = ({markerId}) => {
    const rowFormat = ({citation, zdbID}) => <a href={'/' + zdbID} dangerouslySetInnerHTML={{__html: citation}} />;

    const sortOptions = [
        {
            value: 'Year, Newest',
            label: 'Year, Newest',
        },
        {
            value: 'Year, Oldest',
            label: 'Year, Oldest',
        },
        {
            value: 'First Author, A to Z',
            label: 'First Author, A to Z',
        },
        {
            value: 'First Author, Z to A',
            label: 'First Author, Z to A',
        },
    ];

    const downloadOptions = [
        {
            format: 'TSV',
            url: `/action/api/marker/${markerId}/citations.tsv`,
        },
    ];

    return (
        <DataList
            dataUrl={`/action/api/marker/${markerId}/citations`}
            downloadOptions={downloadOptions}
            rowFormat={rowFormat}
            rowKey={row => row.zdbID}
            sortOptions={sortOptions}
        />
    );
};

MarkerCitationsTable.propTypes = {
    markerId: PropTypes.string,
};

export default MarkerCitationsTable;
