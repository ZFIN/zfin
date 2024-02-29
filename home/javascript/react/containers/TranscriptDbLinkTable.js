import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';
import SequenceType from '../components/SequenceType';
import CommaSeparatedList from '../components/CommaSeparatedList';

const TranscriptDbLinkTable = ({markerId, showSummary}) => {
    const [summary, setSummary] = useState(showSummary === 'true');
    const [showSequence, setShowSequence] = useState(false);
    const [count, setCount] = useState({'countDirect': 0, 'countIncludingChildren': 0});

    const showSeq = () => {
        setShowSequence(true)
    };

    const hideSeq = () => {
        setShowSequence(false)
    };

    const spanStyle = {
        overflowWrap: 'break-word'
    };

    const columns = [
        {
            label: 'ZFIN ID',
            content: row => row.zdbID,
            width: '100px',
            filterName: 'dblinkId',
        },
        {
            label: 'Foreign DB',
            content: row =>
                <span title={row.referenceDatabase.zdbID}>
                    {row.referenceDatabase.foreignDB.dbName}
                </span>,
            width: '80px',
            filterName: 'foreignDB',
            filterOptionFromSupplementalData: 'foreignDB',
        },
        {
            label: 'Accession',
            content: row => row.accessionNumber,
            width: '100px',
            filterName: 'accession',
            align: 'left',
        },
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
            filterOptionFromSupplementalData: 'type',
        },
        {
            label: 'Super Type',
            content: row => (row.referenceDatabase.foreignDBDataType.superType),
            width: '100px',
            filterName: 'superType',
            filterOptionFromSupplementalData: 'superTypes',
        },
        {
            label: 'Sequence',
            content: row => (
                <>
                    {row.sequence && !showSequence && (
                        <>
                            [
                            <button type='button' className='btn btn-link px-0' onClick={showSeq}>Show</button>
                            ]
                        </>
                    )
                    }
                    {row.sequence && showSequence && (
                        <>
                            <span style={spanStyle}>{row.sequence.data} </span>
                            [
                            <button type='button' className='btn btn-link px-0' onClick={hideSeq}>Hide</button>
                            ]
                        </>
                    )}
                </>
            )
            ,
            width: '100px',
            align: 'right',
        },
        {
            label: 'Display Groups',
            //content: row => row.referenceDatabase.displayGroupMembers.map(obj => obj.displayGroup.groupName) ,
            content: row => <CommaSeparatedList>
                {row.referenceDatabase.displayGroupMembers.map(obj => obj.displayGroup.groupName)}
            </CommaSeparatedList>,
            width: '100px',
            filterOptionFromSupplementalData: 'displayGroup',
            filterName: 'displayGroup',
            align: 'right',
        },
        {
            label: 'Length (nt/aa)',
            content: row => row.length && `${row.length} ${row.units}`,
            width: '60px',
            align: 'right',
        },
        {
            label: 'Db_Link Info',
            content: row => row.linkInfo,
            filterName: 'dbInfo',
            width: '100px',
        }
    ];

    const params = {};
    if (summary) {
        params.summary = true;
    }

    return (
        <>
            {showSummary && (
                <DataTableSummaryToggle
                    directLabel='Overview'
                    childrenLabel='All Sequences'
                    directCount={count.countDirect}
                    childrenCount={count.countIncludingChildren}
                    showPopup={summary}
                    onChange={setSummary}
                />
            )}
            <DataTable
                columns={columns}
                dataUrl={`/action/api/marker/${markerId}/dblinks?${qs.stringify(params)}`}
                onDataLoadedCount={(count) => setCount(count)}
                pagination={!summary}
                rowKey={row => row.zdbID}
            />
        </>
    );
};

TranscriptDbLinkTable.propTypes = {
    markerId: PropTypes.string,
    showSummary: PropTypes.string,
};

export default TranscriptDbLinkTable;
