import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import CommaSeparatedList from '../components/CommaSeparatedList';
import {EntityLink, EntityList} from '../components/entity';

const AntibodyTable = ({url, title, navigationCounter}) => {
    const columns = [
        {
            label: 'Name',
            content: row => <EntityLink entity={row}/>,
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

    const handleDataLoadedCount = (data) => {
        if (navigationCounter && navigationCounter.setCounts && data.total) {
            navigationCounter.setCounts(title, data.total);
        }
    };

    return (
        <DataTable
            columns={columns}
            dataUrl={url}
            rowKey={row => row.zdbID}
            pagination={true}
            onDataLoaded={handleDataLoadedCount}
        />
    );
};

AntibodyTable.propTypes = {
    url: PropTypes.string,
    title: PropTypes.string,
    navigationCounter: PropTypes.node,
};

export default AntibodyTable;
