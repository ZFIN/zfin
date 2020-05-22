import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import CommaSeparatedList from '../components/CommaSeparatedList';
import MarkerAbbreviation from '../components/MarkerAbbreviation';
import MarkerLink from '../components/MarkerLink';

const MarkerList = ({focusMarkerId, markers}) => (
    <CommaSeparatedList>
        {markers.map(marker => {
            if (marker.zdbID === focusMarkerId) {
                return <MarkerAbbreviation key={marker.zdbID} marker={marker} />;
            } else {
                return <MarkerLink key={marker.zdbID} marker={marker} />
            }
        })}
    </CommaSeparatedList>
);

const sortOptions = [
    {
        value: 'constructNameUp',
        label: <span>Construct (Default) <i className='fas fa-sort-alpha-down' /></span>,
    },
    {
        value: 'regulatoryRegionUp',
        label: <span>Regulatory Region  <i className='fas fa-sort-alpha-down'/></span>,
    },
    {
        value: 'regulatoryRegionDown',
        label: <span>Regulatory Region <i className='fas fa-sort-alpha-up'/></span>,
    },
    {
        value: 'codingSequenceUp',
        label: <span>Coding Sequence  <i className='fas fa-sort-alpha-down'/></span>,
    },
    {
        value: 'codingSequenceDown',
        label: <span>Coding Sequence  <i className='fas fa-sort-alpha-up'/></span>,
    },
    {
        value: 'speciesUp',
        label: <span>Species  <i className='fas fa-sort-alpha-down'/></span>,
    },
    {
        value: 'speciesDown',
        label: <span>Species  <i className='fas fa-sort-alpha-up'/></span>,
    },
    {
        value: 'citationMost',
        label: <span>Citation <i className='fas fa-sort-numeric-up'/></span>,
    },
    {
        value: 'citationLeast',
        label: <span>Citation <i className='fas fa-sort-numeric-down'/></span>,
    },
];

const GeneConstructsTable = ({geneId}) => {
    const columns = [
        {
            label: 'Construct',
            content: ({construct}) => <MarkerLink marker={construct}/>,
            width: '150px',
            filterName: 'name',
        },
        {
            label: 'Regulatory Region',
            content: ({regulatoryRegions}) => <MarkerList focusMarkerId={geneId} markers={regulatoryRegions}/>,
            width: '120px',
            filterName: 'regulatoryRegion',
        },
        {
            label: 'Coding Sequence',
            content: ({codingSequences}) => <MarkerList focusMarkerId={geneId} markers={codingSequences}/>,
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
            align: 'right',
        },
        {
            label: 'Citations',
            content: row => <a href={`/action/marker/citation-list/${row.construct.zdbID}`}>{row.numberOfPublications}</a>,
            width: '100px',
            align: 'right',
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
