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

const StatisticDataTable = ({
    dataUrl,
    downloadOptions,
    onDataLoaded,
    pagination = true,
    setTableState,
    sortOptions,
    tableState,
}) => {
    [tableState, setTableState] = useTableState(tableState, setTableState);


    const renderData = response => (
        <StatisticTable
            supplementalData={response.supplementalData}

        />
    );

    return (
        <div className='data-table-container'> hello
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

export default StatisticDataTable;
