import React, {useState} from 'react';
import PropTypes from 'prop-types';
import {columnDefinitionType, downloadOptionType, sortOptionType, tableStateType} from '../../utils/types';
import DataProvider from './DataProvider';
import Table from './Table';
import HeaderCell from './HeaderCell';
import produce, {setAutoFreeze} from 'immer';
setAutoFreeze(false);

import useTableState from '../../hooks/useTableState';
import ShowDevInfo from '../ShowDevInfo';

/**
 * Checks if a column should be hidden based on its hideIfAllNull property
 * and whether all values in the dataset are null, undefined, or empty string
 */
const shouldHideColumn = (data, column) => {
    if (!column.hideIfAllNull) {
        return false;
    }
    
    return data.every(row => {
        const value = row[column.accessor];
        return value === null || value === undefined || value === '';
    });
};

const DataTable = ({
    columns,
    dataUrl,
    downloadOptions,
    onDataLoaded,
    onDataLoadedCount,
    pagination = true,
    rowKey,
    setTableState,
    sortOptions,
    tableState,
    tableFixed = true,
}) => {
    [tableState, setTableState] = useTableState(tableState, setTableState);
    const [processedColumns, setProcessedColumns] = useState(columns);

    const handleFilterChange = (field, value) => {
        setTableState(produce(state => {
            if (!state.filter) {
                state.filter = {};
            }
            state.page = 1;
            state.filter[field] = value;
        }));
    };

    const columnHeaderFormat = (column, supplementalData) => (
        <HeaderCell
            column={column}
            filterValue={tableState.filter && tableState.filter[column.filterName]}
            onFilterChange={handleFilterChange}
            supplementalData={supplementalData}
        />
    );

    const renderData = response => {
        // Process columns to hide those marked with hideIfAllNull when all values are null
        const updatedColumns = columns.map(column => ({
            ...column,
            hidden: column.hidden || shouldHideColumn(response.results, column)
        }));
        
        // Update state if columns have changed
        if (JSON.stringify(updatedColumns) !== JSON.stringify(processedColumns)) {
            setProcessedColumns(updatedColumns);
        }
        
        return (
            <Table
                columnHeaderFormat={columnHeaderFormat}
                columns={updatedColumns}
                data={response.results}
                rowKey={rowKey}
                supplementalData={response.supplementalData}
                total={response.total}
                tableFixed={tableFixed}
            />
        );
    };

    return (
        <div className='data-table-container'>
            <DataProvider
                dataUrl={dataUrl}
                downloadOptions={downloadOptions}
                onDataLoaded={onDataLoaded}
                onDataLoadedCount={onDataLoadedCount}
                pagination={pagination}
                renderData={renderData}
                setTableState={setTableState}
                sortOptions={sortOptions}
                tableState={tableState}
            />
            <ShowDevInfo show={false} url={dataUrl} />
        </div>
    )
};

DataTable.propTypes = {
    columns: PropTypes.arrayOf(columnDefinitionType).isRequired,
    dataUrl: PropTypes.string.isRequired,
    downloadOptions: PropTypes.arrayOf(downloadOptionType),
    onDataLoaded: PropTypes.func,
    onDataLoadedCount: PropTypes.func,
    pagination: PropTypes.bool,
    rowKey: PropTypes.oneOfType([PropTypes.string, PropTypes.func]).isRequired,
    setTableState: PropTypes.func,
    sortOptions: PropTypes.arrayOf(sortOptionType),
    tableState: tableStateType,
    tableFixed: PropTypes.bool,
};

export default DataTable;
