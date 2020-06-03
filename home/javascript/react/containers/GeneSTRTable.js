import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink, EntityList} from '../components/entity';
import NoData from '../components/NoData';

const sortOptions = [
    {
        value: 'strUp',
        label: <span>STR (Default) <i className='fas fa-sort-alpha-down' /></span>,
    },
    {
        value: 'createdAlleleUp',
        label: <span>Created Alleles <i className='fas fa-sort-alpha-down' /></span>,
    },
    {
        value: 'createdAlleleDown',
        label: <span>Created Alleles <i className='fas fa-sort-alpha-up' /></span>,
    },
    {
        value: 'citationMost',
        label: <span>Citation <i className='fas fa-sort-numeric-up' /></span>,
    },
    {
        value: 'citationLeast',
        label: <span>Citation <i className='fas fa-sort-numeric-down' /></span>,
    },
];

const GeneSTRTable = ({geneId}) => {
    const columns = [
        {
            label: 'Targeting Reagent',
            content: ({marker}) => <EntityLink entity={marker}/>,
            width: '150px',
        },
        {
            label: 'Created Alleles',
            content: ({genomicFeatures, marker}) => {
                if (marker.type === 'MRPHLNO') {
                    return <NoData placeholder='N/A' />
                }
                return <EntityList entities={genomicFeatures} />
            },
            width: '120px',
        },
        {
            label: 'Citations',
            content: ({marker}) => <a href={`/action/marker/citation-list/${marker.zdbID}`}>{marker.publications.length}</a>,
            width: '100px',
            align: 'right',
        },
    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/marker/${geneId}/strs`}
            rowKey={row => row.marker.zdbID}
            sortOptions={sortOptions}
        />
    );
};

GeneSTRTable.propTypes = {
    geneId: PropTypes.string,
};

export default GeneSTRTable;
