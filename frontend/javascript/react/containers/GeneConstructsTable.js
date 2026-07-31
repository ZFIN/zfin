import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import CommaSeparatedList from '../components/CommaSeparatedList';
import { EntityLink, EntityList } from '../components/entity';
import AttributionLink from '../components/AttributionLink';

const sortOptions = [
    {
        value: 'constructNameUp',
        label: 'Construct (Default), A to Z ',
    },
    {
        value: 'regulatoryRegionUp',
        label: 'Regulatory Region, A to Z ',
    },
    {
        value: 'regulatoryRegionDown',
        label: 'Regulatory Region, Z to A ',
    },
    {
        value: 'codingSequenceUp',
        label: 'Coding Sequence, A to Z ',
    },
    {
        value: 'codingSequenceDown',
        label: 'Coding Sequence, Z to A ',
    },
    {
        value: 'speciesUp',
        label: 'Species, A to Z ',
    },
    {
        value: 'speciesDown',
        label: 'Species, Z to A ',
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

const GeneConstructsTable = ({geneId}) => {
    const columns = [
        {
            label: 'Construct',
            content: ({construct}) => <span className='text-break'><EntityLink entity={construct}/></span>,
            width: '200px',
            filterName: 'name',
        },
        {
            label: 'Regulatory Region',
            content: ({regulatoryRegions}) => <EntityList focusEntityId={geneId} entities={regulatoryRegions}/>,
            width: '120px',
            filterName: 'regulatoryRegion',
        },
        {
            label: 'Coding Sequence',
            content: ({codingSequences}) => <EntityList focusEntityId={geneId} entities={codingSequences}/>,
            width: '120px',
            filterName: 'codingSequence',
        },
        {
            label: 'Species',
            content: ({species}) => (
                <CommaSeparatedList>
                    {species.map(s => <i key={s.cvNameDefinition}>{s.cvNameDefinition}</i>)}
                </CommaSeparatedList>
            ),
            width: '130px',
            filterName: 'species',
        },
        {
            label: 'Tg Lines',
            content: row => (row.numberOfTransgeniclines > 0 &&
                <a href={`/search?category=Mutation+/+Tg&q=&fq=xref:${row.construct.zdbID}`}>{row.numberOfTransgeniclines}</a>
            ),
            width: '60px',
        },
        {
            label: 'Citations',
            content: row => (
                <AttributionLink
                    url={row.url}
                    publicationCount={row.numberOfPublications}
                    publication={row.singlePublication}
                    multiPubs={row.construct.zdbID}
                />
            ),
            width: '100px',
        },
    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/marker/${geneId}/constructs`}
            rowKey={row => row.construct.zdbID}
            sortOptions={sortOptions}
        />
    );
};

GeneConstructsTable.propTypes = {
    geneId: PropTypes.string,
};

export default GeneConstructsTable;
