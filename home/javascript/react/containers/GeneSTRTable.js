import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import CommaSeparatedList from '../components/CommaSeparatedList';
import MarkerAbbreviation from '../components/MarkerAbbreviation';
import MarkerLink from '../components/MarkerLink';

const MarkerList = ({focusMarkerId, markers, notApplicable}) => {
    if (!notApplicable) {
        return <CommaSeparatedList>
            {markers.map(marker => {
                if (marker.zdbID === focusMarkerId) {
                    return <MarkerAbbreviation marker={marker}/>;
                } else {
                    return <MarkerLink marker={marker}/>
                }
            })}
        </CommaSeparatedList>
    } else {
        return <i className='no-data-tag'>N/A</i>
    }
};

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
            content: ({marker}) => <MarkerLink marker={marker}/>,
            width: '150px',
        },
        {
            label: 'Created Alleles',
            content: ({genomicFeatures, marker}) => <MarkerList markers={genomicFeatures} notApplicable={marker.type === 'MRPHLNO'}/>,
            width: '120px',
        },
        {
            label: 'Citations',
            content: ({marker}) => <a href='/action/marker/citation-list/'>{marker.publications.length}</a>,
            width: '100px',
            align: 'right',
        },
    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/marker/${geneId}/strs`}
            rowKey='sdf'
            sortOptions={sortOptions}
        />
    );
};

GeneSTRTable.propTypes = {
    geneId: PropTypes.string,
};

export default GeneSTRTable;
