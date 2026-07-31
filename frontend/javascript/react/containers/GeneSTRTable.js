import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import {EntityLink, EntityList} from '../components/entity';
import NoData from '../components/NoData';
import AttributionLink from '../components/AttributionLink';

const sortOptions = [
    {
        value: 'strUp',
        label: 'STR (Default), A to Z ',
    },
    {
        value: 'createdAlleleUp',
        label: 'Created Alleles, A to Z ',
    },
    {
        value: 'createdAlleleDown',
        label: 'Created Alleles, Z to A ',
    },
    {
        value: 'citationMost',
        label: 'Citation, Most ',
    },
    {
        value: 'citationLeast',
        label: 'Citation, Least ',
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
            content: row => (
                <AttributionLink
                    url={row.url}
                    publicationCount={row.numberOfPublications}
                    publication={row.singlePublication}
                    multiPubs={row.marker.zdbID}
                />
            ),
            width: '100px',
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
