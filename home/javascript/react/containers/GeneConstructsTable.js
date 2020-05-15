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
                return <MarkerAbbreviation marker={marker} />;
            } else {
                return <MarkerLink marker={marker} />
            }
        })}
    </CommaSeparatedList>
);

const GeneConstructsTable = ({geneId}) => {
    const columns = [
        {
            label: 'Construct',
            content: ({construct}) => <MarkerLink marker={construct} />,
            width: '150px',
        },
        {
            label: 'Regulatory Regions',
            content: ({regulatoryRegions}) => <MarkerList focusMarkerId={geneId} markers={regulatoryRegions} />,
            width: '120px',
        },
        {
            label: 'Coding Sequences',
            content: ({codingSequences}) => <MarkerList focusMarkerId={geneId} markers={codingSequences} />,
            width: '120px',
        },
        {
            label: 'Species',
            content: ({species}) => (
                <CommaSeparatedList>
                    {species.map(s => <i key={s.cvNameDefinition}>{s.cvNameDefinition}</i>)}
                </CommaSeparatedList>
            ),
            width: '130px',
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
            url={`/action/api/marker/${geneId}/constructs`}
            rowKey={row => row.construct.zdbID}
        />
    );
};

GeneConstructsTable.propTypes = {
    geneId: PropTypes.string,
};

export default GeneConstructsTable;
