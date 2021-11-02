import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import CommaSeparatedList from '../components/CommaSeparatedList';

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

const EfgTable = () => {
    const columns = [
        {
            label: 'EFG',
            content: ({efg}) => (<EntityLink entity={efg}/>
            ),
            width: '90px',
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
            label: 'FPbase Protein',
            content: ({proteins}) => (
                <CommaSeparatedList>
                    {proteins.map(protein => {
                        return (<a key={protein.fpId} href={`https://www.fpbase.org/protein/${protein.fpId}`}>
                            {protein.name} </a>)
                    })}
                </CommaSeparatedList>),
            width: '100px',
        },
    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={'/action/api/marker/efg-proteins'}
            rowKey='fpId'
            sortOptions={sortOptions}
        />
    );
};

EfgTable.propTypes = {
    type: PropTypes.string,
};

export default EfgTable;
