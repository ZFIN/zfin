import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import {DataList} from '../components/data-table';
import Checkbox from '../components/Checkbox';

const CitationTable = ({markerId}) => {
    const [includeUnpublished, setIncludeUnpublished] = useState(false);

    const rowFormat = ({citation, zdbID, indexedOpenStatus}) => <>
        <a href={'/' + zdbID} dangerouslySetInnerHTML={{__html: citation}}/> {indexedOpenStatus}
    </>;

    const sortOptions = [
        {
            value: 'Year, Newest',
            label: 'Year, Newest',
        },
        {
            value: 'Year, Oldest',
            label: 'Year, Oldest',
        },
        {
            value: 'First Author, A to Z',
            label: 'First Author, A to Z',
        },
        {
            value: 'First Author, Z to A',
            label: 'First Author, Z to A',
        },
    ];

    const queryParams = qs.stringify({
        includeUnpublished
    }, {addQueryPrefix: true});

    const downloadOptions = [
        {
            format: 'TSV',
            url: `/action/api/marker/${markerId}/citations.tsv${queryParams}`,
        },
    ];

    return (
        <>
            <div className='mb-2'>
                <Checkbox
                    checked={includeUnpublished}
                    id='includeUnpublishedCheckbox'
                    onChange={e => setIncludeUnpublished(e.target.checked)}
                >
                    Include unpublished citations
                </Checkbox>
            </div>
            <DataList
                dataUrl={`/action/api/marker/${markerId}/citations${queryParams}`}
                downloadOptions={downloadOptions}
                filterable
                rowFormat={rowFormat}
                rowKey={row => row.zdbID}
                sortOptions={sortOptions}
            />
        </>
    );
};

CitationTable.propTypes = {
    markerId: PropTypes.string,
};

export default CitationTable;
