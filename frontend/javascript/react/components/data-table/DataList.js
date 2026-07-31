import React from 'react';
import PropTypes from 'prop-types';
import DataProvider from './DataProvider';
import List from './List';
import {downloadOptionType} from '../../utils/types';
import produce, {setAutoFreeze} from 'immer';
setAutoFreeze(false);

import useTableState from '../../hooks/useTableState';
import TextBoxFilter from './TextBoxFilter';

const DataList = ({
    dataUrl,
    downloadOptions,
    filterable = false,
    rowFormat,
    rowKey,
    sortOptions,
}) => {
    const [tableState, setTableState] = useTableState();

    const handleFilterChange = (value) => {
        setTableState(produce(state => {
            state.page = 1;
            state.filter = value;
        }));
    };

    const renderData = data => (
        <List
            items={data.results}
            rowFormat={rowFormat}
            rowKey={rowKey}
            total={data.total}
        />
    );

    return (
        <div className='data-list-container'>
            <DataProvider
                additionalControls={filterable ?
                    <TextBoxFilter value={tableState.filter} placeholder='Filter' onChange={handleFilterChange} /> :
                    null
                }
                dataUrl={dataUrl}
                downloadOptions={downloadOptions}
                renderData={renderData}
                sortOptions={sortOptions}
                tableState={tableState}
                setTableState={setTableState}
            />
        </div>
    )
};

DataList.propTypes = {
    dataUrl: PropTypes.string,
    downloadOptions: PropTypes.arrayOf(downloadOptionType),
    filterable: PropTypes.bool,
    rowFormat: PropTypes.func,
    rowKey: PropTypes.func,
    sortOptions: PropTypes.arrayOf(downloadOptionType),
};

export default DataList;
