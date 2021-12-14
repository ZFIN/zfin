import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import CommaSeparatedList from '../components/CommaSeparatedList';
import {EntityList} from '../components/entity';

const AntibodyTable = ({url}) => {
    const columns = [
        {
            label: 'Name',
            content: row => row.name,
            width: '200px',
        },
        {
            label: 'Type',
            content: row => row.clonalType,
        },
        {
            label: 'Antigen Genes',
            content: row => (<EntityList entities={row.antigenGenes}/>),
        },
        {
            label: 'Isotypes',
            content: row => row.heavyChainIsotype,
        },
        {
            label: 'Host Organism',
            content: row => row.hostSpecies,
        },
        {
            label: 'Assay',
            content: row => (
                <CommaSeparatedList>
                    {row.distinctAssayNames.map(name => <i key={name}>{name}</i>)}
                </CommaSeparatedList>
            ),
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

AntibodyTable.propTypes = {
    url: PropTypes.string,
};

export default AntibodyTable;
