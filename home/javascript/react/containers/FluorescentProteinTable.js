import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityList} from '../components/entity';

const sortOptions = [
    {
        value: 'alleleUp',
        label: 'Allele (Default), A to Z',
    },
    {
        value: 'typeUp',
        label: 'Type, A to Z',
    },
    {
        value: 'typeDown',
        label: 'Type, Z to A',
    },
    {
        value: 'consequenceUp',
        label: 'Consequence, A to Z',
    },
    {
        value: 'consequenceDown',
        label: 'Consequence, Z to A',
    },
    {
        value: 'supplierUp',
        label: 'Supplier, A to Z',
    },
];

const FluorescentProteinTable = () => {
    const columns = [
        {
            label: 'FPbase Protein',
            content: ({fpId, name}) => (<a href={`https://www.fpbase.org/protein/${fpId}`}>{name}</a>),
            width: '100px',
        },
        {
            label: 'Excitation Length',
            content: ({excitationLength}) => excitationLength,
            width: '50px',
        },
        {
            label: 'Emission Length',
            content: ({emissionLength}) => emissionLength,
            width: '50px',
        },
        {
            label: 'EFG',
            content: ({efgs}) => (efgs && <EntityList entities={efgs}/>
            ),
            width: '90px',
        },
    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={'/action/api/marker/fpbase-proteins'}
            rowKey='zdbID'
            sortOptions={sortOptions}
        />
    );
};

FluorescentProteinTable.propTypes = {
    geneId: PropTypes.string,
};

export default FluorescentProteinTable;
