import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
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
    navigationCounter: PropTypes.shape({
        setCounts: PropTypes.func
    }),
};

export default AntibodyTable;
