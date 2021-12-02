import React, { useState } from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import AttributionLink from '../components/AttributionLink';
import BlastDropDown from '../components/BlastDropDown';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';
import SequenceType from '../components/SequenceType';

const MarkerSequencesTable = ({markerId, showSummary}) => {
    const [summary, setSummary] = useState(showSummary === 'true');
    const [hasData, setHasData] = useState(false);

    const columns = [
        {
            label: 'Type',
            content: row => (
                <SequenceType
                    type={row.type}
                    showPopup={summary}
                    markerID={markerId}
                />
            ),

            width: '80px',
            filterName: 'type',
            filterOptions: ['Genomic', 'RNA', 'Polypeptide'],
        },
        {
            label: 'Accession #',
            content: row => (
                <AttributionLink
                    url={row.url}
                    accession={row.displayName}
                    publicationCount={row.publicationCount}
                    publication={row.singlePublication}
                    multiPubs={row.publicationIds}
                    multiPubAccessionID={markerId}
                />
            ),
            width: '150px',
            filterName: 'accession',
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

    const params = {};
    if (summary) {
        params.summary = true;
    }

    return (
        <>
            {showSummary && hasData && (
                <DataTableSummaryToggle detailLabel='All Sequences' showPopup={summary} onChange={setSummary} />
            )}
            <DataTable
                columns={columns}
                dataUrl={`/action/api/marker/${markerId}/sequences?${qs.stringify(params)}`}
                onDataLoaded={() => setHasData(true)}
                pagination={!summary}
                rowKey={row => row.zdbID}
            />
        </>
    );
};

MarkerSequencesTable.propTypes = {
    markerId: PropTypes.string,
    showSummary: PropTypes.string,
};

export default MarkerSequencesTable;
