import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';


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

const PubAlleleTable = ({pubId}) => {
    const columns = [
        {
            label: 'Name',
            // content: ({marker}) => <EntityLink entity={marker}/>,
            width: '150px',
        },
        {
            label: 'New with this paper',
            /*  content: ({genomicFeatures, marker}) => {
                  if (marker.type === 'MRPHLNO') {
                      return <NoData placeholder='N/A' />
                  }
                  return <EntityList entities={genomicFeatures} />
              },*/
            width: '120px',
        },

        {
            label: 'Phenotype Data',
            /*      content: row => (
                      <AttributionLink
                          url={row.url}
                          publicationCount={row.numberOfPublications}
                          publication={row.singlePublication}
                          multiPubs={row.marker.zdbID}
                      />
                  ),*/
            width: '100px',
        },

    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/publication/${pubId}/prioritization/alleles`}
            rowKey={row => row.marker.zdbID}
            sortOptions={sortOptions}
        />
    );
};

PubAlleleTable.propTypes = {
    pubId: PropTypes.string,
};

export default PubAlleleTable;
