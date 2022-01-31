import React from 'react';
import PropTypes from 'prop-types';
import {columnDefinitionType, downloadOptionType, sortOptionType, tableStateType} from '../../utils/types';
import DataProvider from './DataProvider';
import StatisticTable from './StatisticTable';
/*
import HeaderCell from './HeaderCell';
import produce from 'immer';
*/
import useTableState from '../../hooks/useTableState';
import produce from 'immer';

const StatisticDataTable = ({
    dataUrl,
    downloadOptions,
    onDataLoaded,
    pagination = true,
    setTableState,
    rowKey,
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
            state
        }));
    };

    const handleCardinalitySortChange = (field, value) => {
        setTableState(produce(state => {
            if (!state.cardinalitySort) {
                state.cardinalitySort = {};
            }
            state.page = 1;
            // reset all other sorting: single sort only, no nested sort
            state.cardinalitySort = {};
            state.cardinalitySort[field] = value;
        }));
    };

    const renderData = response => (
        <StatisticTable
            supplementalData={response.supplementalData}
            tableState={tableState}
            handleFilterChange={handleFilterChange}
            handleCardinalitySortChange={handleCardinalitySortChange}
            data={response.results}
            rowKey={rowKey}
        />
    );

    return (
        <div className='data-table-container'>
            {
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
            }
        </div>
    )
};

StatisticDataTable.propTypes = {
    columns: PropTypes.arrayOf(columnDefinitionType),
    dataUrl: PropTypes.string.isRequired,
    downloadOptions: PropTypes.arrayOf(downloadOptionType),
    onDataLoaded: PropTypes.func,
    pagination: PropTypes.bool,
    rowKey: PropTypes.oneOfType([PropTypes.string, PropTypes.func]).isRequired,
    setTableState: PropTypes.func,
    sortOptions: PropTypes.arrayOf(sortOptionType),
    tableState: tableStateType,
};

export default StatisticDataTable;
