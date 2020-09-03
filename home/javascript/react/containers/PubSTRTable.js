import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';

/*const sortOptions = [
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
];*/

const PubSTRTable = ({pubId}) => {
    const columns = [
        {
            label: 'Name',
            content: row => <a href={`/${row.id}`}>{row.name}</a>,
            width: '150px',
        },
        {
            label: 'Phenotype Data',
            content: row => (row.phenoOnMarker.numFigures > 0 &&
                <a href={`/action/marker/${row.id}/phenotype-summary`}>{row.phenoOnMarker.numFigures} figures from {row.phenoOnMarker.numPublications} pubs</a>

            ),
            width: '120px',
        },



    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/publication/${pubId}/prioritization/strs`}
            rowKey={row => row.id}
            //sortOptions={sortOptions}
        />
    );
};

PubSTRTable.propTypes = {
    pubId: PropTypes.string,
};

export default PubSTRTable;
