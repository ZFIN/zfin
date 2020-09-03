import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';

const sortOptions = [
    {
        value: 'strUp',
        label: 'STR (Default), A to Z ',
    },
    {
        value: 'createdAlleleUp',
        label: 'Created Alleles, A to Z ',
    },
    {
        value: 'createdAlleleDown',
        label: 'Created Alleles, Z to A ',
    },
    {
        value: 'citationMost',
        label: 'Citation, Most ',
    },
    {
        value: 'citationLeast',
        label: 'Citation, Least ',
    },
];

const PubSTRTable = ({pubId}) => {
    const columns = [
        {
            label: 'Name',
            //content: ({marker}) => <EntityLink entity={marker}/>,
            width: '150px',
        },
        {
            label: 'Phenotype Data',
            /* content: ({genomicFeatures, marker}) => {
                if (marker.type === 'MRPHLNO') {
                    return <NoData placeholder='N/A' />
                }
                return <EntityList entities={genomicFeatures} />
            },*/
            width: '120px',
        },



    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/publication/${pubId}/prioritization/strs`}
            rowKey={row => row.marker.zdbID}
            sortOptions={sortOptions}
        />
    );
};

PubSTRTable.propTypes = {
    pubId: PropTypes.string,
};

export default PubSTRTable;
