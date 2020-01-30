import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/DataTable';
import AttributionLink from '../components/AttributionLink';
import BlastDropDown from '../components/BlastDropDown';

const GeneSequencesTable = ({geneId}) => {
    const columns = [
        {
            label: 'Type',
            content: row => row.type,
            width: '50px',
        },
        {
            label: 'Accession #',
            content: row => (
                <AttributionLink
                    url={row.url}
                    accession={row.displayName}
                    publicationCount={row.publicationCount}
                    publication={row.singlePublication}
                    multiPubAccessionID={row.zdbID}
                />
            ),
            width: '150px',
        },
        {
            label: 'Length (nt/aa)',
            content: row => row.length && `${row.length} ${row.units}`,
            width: '100px',
            align: 'right',
        },
        {
            label: 'Analysis',
            content: row => <BlastDropDown dbLink={row}/>,
            width: '100px',
        }
    ];
    return (
        <DataTable
            columns={columns}
            url={`/action/api/marker/${geneId}/sequences`}
            rowKey={row => row.zdbID}
        />
    );
};

GeneSequencesTable.propTypes = {
    geneId: PropTypes.string,
};

export default GeneSequencesTable;
