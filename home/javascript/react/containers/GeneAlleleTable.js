import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import NoDataDisplay from '../components/NoDataDisplay';
import Supplier from '../components/Supplier';

const sortOptions = [
    {
        value: 'alleleUp',
        label: <span>Allele (Default) <i className='fas fa-sort-alpha-down' /></span>,
    },
    {
        value: 'typeUp',
        label: <span>Type <i className='fas fa-sort-alpha-down' /></span>,
    },
    {
        value: 'typeDown',
        label: <span>Type <i className='fas fa-sort-alpha-up' /></span>,
    },
    {
        value: 'consequenceUp',
        label: <span>Consequence <i className='fas fa-sort-alpha-down' /></span>,
    },
    {
        value: 'consequenceDown',
        label: <span>Consequence <i className='fas fa-sort-alpha-up' /></span>,
    },
    {
        value: 'supplierUp',
        label: <span>Supplier <i className='fas fa-sort-alpha-up' /></span>,
    },
];

const GeneAlleleTable = ({geneId}) => {
    const columns = [
        {
            label: 'Allele',
            content: ({name}) => <a>{name}</a>,
            width: '10%',
        },
        {
            label: 'Type',
            content: ({type}) => type.display,
            width: '13%',
        },
        {
            label: 'Localization',
            content: ({geneLocalizationStatement}) => <NoDataDisplay data={geneLocalizationStatement} noDataString='Unknown'/>,
            width: '15%',
        },
        {
            label: 'Consequence',
            content: ({transcriptConsequenceStatement}) => <NoDataDisplay data={transcriptConsequenceStatement} noDataString='Unknown'/>,
            width: '20%',
        },
        {
            label: 'Mutagen',
            content: ({featureAssay}) => <NoDataDisplay data={featureAssay.mutagen.value} noDataString='Unknown'/>,
            width: '10%',
        },
        {
            label: 'Supplier',
            content: ({suppliers}) => <Supplier suppliers={suppliers}  />,
            width: '40%',
        },
    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/marker/${geneId}/mutations`}
            rowKey='sdf'
            sortOptions={sortOptions}
        />
    );
};

GeneAlleleTable.propTypes = {
    geneId: PropTypes.string,
};

export default GeneAlleleTable;
