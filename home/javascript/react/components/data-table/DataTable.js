import React  from 'react';
import PropTypes from 'prop-types';
import {columnDefinitionType, downloadOptionType, sortOptionType, tableStateType} from '../../utils/types';
import DataProvider from './DataProvider';
import Table from './Table';
import HeaderCell from './HeaderCell';
import produce, {setAutoFreeze} from 'immer';
setAutoFreeze(false);

import useTableState from '../../hooks/useTableState';

const DataTable = ({
    columns,
    dataUrl,
    downloadOptions,
    onDataLoaded,
    pagination = true,
    rowKey,
    setTableState,
    sortOptions,
    tableState,
}) => {
    [tableState, setTableState] = useTableState(tableState, setTableState);

    const handleFilterChange = (field, value) => {
        setTableState(produce(state => {
            if (!state.filter) {
                state.filter = {};
            }
            state.page = 1;
            state.filter[field] = value;
        }));
    };

    const columnHeaderFormat = column => (
        <HeaderCell
            column={column}
            filterValue={tableState.filter && tableState.filter[column.filterName]}
            onFilterChange={handleFilterChange}
        />
    );

    const renderData = response => (
        <Table
            columnHeaderFormat={columnHeaderFormat}
            columns={columns}
            data={response.results}
            rowKey={rowKey}
            supplementalData={response.supplementalData}
            total={response.total}
        />
    );

    return (
        <div className='data-table-container'>
            <DataProvider
                dataUrl={dataUrl}
                downloadOptions={downloadOptions}
                onDataLoaded={onDataLoaded}
                pagination={pagination}
                renderData={renderData}
                setTableState={setTableState}
                sortOptions={sortOptions}
                tableState={tableState}
            />
        </div>
    )
};

DataTable.propTypes = {
    columns: PropTypes.arrayOf(columnDefinitionType).isRequired,
    dataUrl: PropTypes.string.isRequired,
    downloadOptions: PropTypes.arrayOf(downloadOptionType),
    onDataLoaded: PropTypes.func,
    pagination: PropTypes.bool,
    rowKey: PropTypes.oneOfType([PropTypes.string, PropTypes.func]).isRequired,
    setTableState: PropTypes.func,
    sortOptions: PropTypes.arrayOf(sortOptionType),
    tableState: tableStateType,
};

export default DataTable;
