import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import SupplierList from '../components/SupplierList';
import {EntityLink} from '../components/entity';
import NoData from '../components/NoData';

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
            content: (allele) => <EntityLink entity={allele} />,
            width: '100px',
        },
        {
            label: 'Type',
            content: ({type}) => type.display,
            width: '90px',
        },
        {
            label: 'Localization',
            content: ({geneLocalizationStatement}) => (geneLocalizationStatement || <NoData placeholder='Unknown' />),
            width: '100px',
        },
        {
            label: 'Consequence',
            content: ({transcriptConsequenceStatement}) => (transcriptConsequenceStatement || <NoData placeholder='Unknown' />),
            width: '110px',
        },
        {
            label: 'Mutagen',
            content: ({featureAssay}) => (featureAssay.mutagen.value || <NoData placeholder='Unknown' />),
            width: '80px',
        },
        {
            label: 'Supplier',
            content: ({suppliers}) => <SupplierList suppliers={suppliers} />,
            width: '200px',
        },
    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/marker/${geneId}/mutations`}
            rowKey='zdbID'
            sortOptions={sortOptions}
        />
    );
};

GeneAlleleTable.propTypes = {
    geneId: PropTypes.string,
};

export default GeneAlleleTable;
