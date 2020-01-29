import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/DataTable';
import AttributionLink from '../components/AttributionLink';
import BlastDropDown from '../components/BlastDropDown';

const AccessionLink = ({accession, url}) => (
    <AccessionLink>
        return <a href={url}>{accession}</a>;
    </AccessionLink>
);

const GeneSequencesTable = ({geneId, type}) => {
    const columns = [
        {
            label: 'Accession #',
            content: row => (
                <AttributionLink url={row.url} accession={row.displayName} publicationCount={row.publicationCount}
                                 publication={row.singlePublication} multiPubAccessionID={row.zdbID}/>
            ),
            width: '150px',
        },
        {
            label: 'Length',
            content: row => (<span>{row.length}</span>),
            width: '100px',
            align: 'right',
        },
        {
            label: '[nt/aa]',
            content: row => (<span>{row.units}</span>),
            width: '20px',
        },
        {
            label: 'Analysis',
            content: row => (<BlastDropDown dbLink={row}/>),
            width: '150px',
        }
    ];
    return (
        <DataTable
            columns={columns}
            url={`/action/api/marker/${geneId}/sequences?filter.type=${type}`}
            rowKey={row => row.zdbID}
        />
    );
};

GeneSequencesTable.propTypes = {
    geneId: PropTypes.string,
};

export default GeneSequencesTable;
