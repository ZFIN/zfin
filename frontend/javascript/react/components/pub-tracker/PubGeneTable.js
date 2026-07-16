import React from 'react';
import PropTypes from 'prop-types';
import { CollapseTable } from '../../components/data-table';


const sortOptions = [
    {
        value: 'symbolUp',
        label: 'Gene, A to Z ',
    },
    {
        value: 'expDataUp',
        label: 'Expression Date, least to most ',
    },
    {
        value: 'phenoDataUp',
        label: 'Phenotype Data, least to most ',
    },

];

const PubGeneTable = ({pubId}) => {
    const columns = [
        {
            label: 'Symbol',
            content: row => <a href={`/${row.id}`}>{row.name}</a>,
            width: '100px',
        },
        {
            label: 'New with this paper',
            content: row =>row.newWithThisPaper ? <i className='text-muted'>Yes </i>: <i className='text-muted'>No</i>,

            width: '100px',
        },

        {
            label: 'Expression data',
            content: row => (row.expressionFigures > 0 &&
                <a href={`/action/marker/${row.id}/expression`}>{row.expressionFigures} figures  (<a href={`/action/expression/results?geneField=${row.name}&assayName=mRNA+in+situ+hybridization`}>{row.expressionInSitu} in situ</a>)  from {row.expressionPublication} pubs</a>
            ),

            width: '150px',
        },
        {
            label: 'Phenotype Data',

            content: row => (row.phenotypeFigures > 0 &&
                <a href={`/action/marker/${row.id}/phenotype-summary`}>{row.phenotypeFigures} figures from {row.phenotypePublication} pubs</a>
            ),
            width: '150px',
        },
        {
            label: 'Associated Disease',
            content:
                row => row.associatedDiseases,

            width: '100px',
        },
        {
            label: 'Has Orthology',
            content: row => row.hasOrthology ? <i className='text-muted'>Yes </i>: <i className='text-muted'>No</i>,
            width: '100px',
        },


    ];
    return (
        <CollapseTable
            columns={columns}
            dataUrl={`/action/api/publication/${pubId}/prioritization/genes`}
            rowKey={row => row.id}
            sortOptions={sortOptions}
        />
    );
};

PubGeneTable.propTypes = {
    pubId: PropTypes.string,
};

export default PubGeneTable;
