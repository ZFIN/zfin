import React from 'react';
import PropTypes from 'prop-types';
import DataProvider from './DataProvider';
import List from './List';
import {downloadOptionType} from '../../utils/types';

const DataList = ({
    dataUrl,
    downloadOptions,
    rowFormat,
    rowKey,
    sortOptions,
}) => {
    return (
        <div className='data-list-container'>
            <DataProvider
                dataUrl={dataUrl}
                downloadOptions={downloadOptions}
                renderData={data => <List data={data} rowFormat={rowFormat} rowKey={rowKey}/>}
                sortOptions={sortOptions}
            />
        </div>
    )
};

DataList.propTypes = {
    dataUrl: PropTypes.string,
    downloadOptions: PropTypes.arrayOf(downloadOptionType),
    rowFormat: PropTypes.func,
    rowKey: PropTypes.func,
    sortOptions: PropTypes.arrayOf(downloadOptionType),
};

export default DataList;
