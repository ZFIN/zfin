import React from 'react';
import PropTypes from 'prop-types';
import { CollapseTable } from '../../components/data-table';

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
            content: row => (row.phenotypeFigures > 0 &&
                <a href={`/action/marker/${row.id}/phenotype-summary`}>{row.phenotypeFigures} figures from {row.phenotypePublication} pubs</a>
            ),
            width: '120px',
        },



    ];
    return (
        <CollapseTable
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
