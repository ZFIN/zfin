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

const PubAlleleTable = ({pubId}) => {
    const columns = [
        {
            label: 'Name',
            content: row => <a href={`/${row.id}`}>{row.name}</a>,
            width: '150px',
        },
        {
            label: 'New with this paper',
            content: row =>row.newWithThisPaper ? <i className='text-muted'>Yes </i>: <i className='text-muted'>No</i>,
            width: '120px',
        },

        {
            label: 'Phenotype Data',
            content: row =>row.newWithThisPaper ? <i className='text-muted'>Yes </i>: <i className='text-muted'>No</i>,
            width: '100px',
        },

    ];
    return (
        <CollapseTable
            columns={columns}
            dataUrl={`/action/api/publication/${pubId}/prioritization/features`}
            rowKey={row => row.id}
            //sortOptions={sortOptions}
        />
    );
};

PubAlleleTable.propTypes = {
    pubId: PropTypes.string,
};

export default PubAlleleTable;
