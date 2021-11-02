import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityList} from '../components/entity';

const sortOptions = [
    {
        value: 'default',
        label: '(Default)',
    },
    {
        value: 'proteinUp',
        label: 'Protein, A to Z',
    },
    {
        value: 'proteinDown',
        label: 'Protein, Z to A',
    },
    {
        value: 'efgUp',
        label: 'EFG, A to Z',
    },
    {
        value: 'efgDown',
        label: 'EFG, Z to A',
    },
    {
        value: 'excitationUp',
        label: 'Excitation wave length, 0 to 100',
    },
    {
        value: 'excitationDown',
        label: 'Excitation wave length, 100 to 0',
    },
    {
        value: 'emissionUp',
        label: 'Emission wave length, 0 to 100',
    },
    {
        value: 'emissionDown',
        label: 'Emission wave length, 100 to 0',
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
            label: 'Excitation Color',
            content: ({excitationColor}) => excitationColor,
            width: '50px',
        },
        {
            label: 'Emission Length',
            content: ({emissionLength}) => emissionLength,
            width: '50px',
        },
        {
            label: 'Emission Color',
            content: ({emissionColor}) => emissionColor,
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
            rowKey='fpId'
            sortOptions={sortOptions}
        />
    );
};

FluorescentProteinTable.propTypes = {
    geneId: PropTypes.string,
};

export default FluorescentProteinTable;
