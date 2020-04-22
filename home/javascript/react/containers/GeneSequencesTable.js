import React, { useState } from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/DataTable';
import AttributionLink from '../components/AttributionLink';
import BlastDropDown from '../components/BlastDropDown';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';

const GeneSequencesTable = ({geneId}) => {
    const [summary, setSummary] = useState(true);
    const columns = [
        {
            label: 'Type',
            content: row => row.type,
            width: '80px',
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
        <React.Fragment>
            <DataTableSummaryToggle detailLabel='All Sequences' value={summary} onChange={setSummary} />
            <DataTable
                columns={columns}
                url={`/action/api/marker/${geneId}/sequences?summary=${summary}`}
                pagination={!summary}
                rowKey={row => row.zdbID}
            />
        </React.Fragment>
    );
};

GeneSequencesTable.propTypes = {
    geneId: PropTypes.string,
};

export default GeneSequencesTable;
