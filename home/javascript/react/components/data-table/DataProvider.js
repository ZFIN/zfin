import React, {useEffect} from 'react';
import {useTableDataFetch, useTableState} from '../../utils/effects';
import GenericErrorMessage from '../GenericErrorMessage';
import LoadingSpinner from '../LoadingSpinner';
import NoData from '../NoData';
import produce from 'immer';
import PropTypes from 'prop-types';
import {downloadOptionType, sortOptionType, tableStateType} from '../../utils/types';
import {isEmptyObject} from '../../utils';

const DataProvider = ({
    additionalControls,
    dataUrl,
    downloadOptions,
    onDataLoaded,
    pagination = true,
    renderData,
    setTableState,
    sortOptions,
    tableState,
}) => {
    [tableState, setTableState] = useTableState(tableState, setTableState);
    const data = useTableDataFetch(dataUrl, tableState);
    useEffect(() => {
        if (typeof onDataLoaded === 'function' && !data.loading && !data.rejected && data.value && data.value.total > 0) {
            onDataLoaded();
        }
    }, [data]);

    if (data.rejected) {
        return <GenericErrorMessage/>;
    }

    if (!data.value) {
        if (data.pending) {
            return <LoadingSpinner/>;
        } else {
            return null;
        }
    }

    const {returnedRecords, total} = data.value;

    if (total === 0 && isEmptyObject(tableState.filter)) {
        return <NoData/>
    }

    const start = (tableState.page - 1) * tableState.limit + 1;
    const end = start + returnedRecords - 1;
    const totalPages = Math.ceil(total / tableState.limit);

    const handleLimitChange = (event) => {
        const limit = parseInt(event.target.value, 10);
        const totalPages = Math.ceil(total / limit);
        setTableState(produce(state => {
            state.limit = limit;
            state.page = Math.min(state.page, totalPages);
        }));
    };

    const handlePageChange = (page) => {
        setTableState(produce(state => {
            state.page = Math.max(1, Math.min(totalPages, page));
        }))
    };

    const handleSortChange = (sortBy) => {
        setTableState(produce(state => {
            state.page = 1;
            state.sortBy = sortBy;
        }));
    };

    return (
        <>
            <div className='d-flex justify-content-end align-items-start'>
                {additionalControls}

                {downloadOptions && downloadOptions.length > 0 &&
                <div className='btn-group'>
                    <button className='btn btn-sm dropdown-toggle' type='button' data-toggle='dropdown'>
                        Download
                    </button>
                    <div className='dropdown-menu dropdown-menu-right'>
                        {downloadOptions.map(option => (
                            <a key={option.format} className='dropdown-item' href={option.url}>{option.format}</a>
                        ))}
                    </div>
                </div>
                }

                {sortOptions && sortOptions.length > 0 &&
                <div className='btn-group'>
                    <button className='btn btn-sm dropdown-toggle' type='button' data-toggle='dropdown'>
                        Sort by
                    </button>
                    <div className='dropdown-menu dropdown-menu-right'>
                        {sortOptions.map((sort, idx) => {
                            const isActive = (tableState.sortBy === null && idx === 0) || tableState.sortBy === sort.value;
                            return (
                                <button key={sort.value} className='dropdown-item' type='button' onClick={() => handleSortChange(sort.value)}>
                                    <i className={`fas fa-fw mr-1 ${isActive ? 'fa-check' : ''}`}/>
                                    {sort.label}
                                </button>
                            );
                        })}
                    </div>
                </div>
                }
            </div>

            {renderData(data.value)}

            <div className='data-pagination-container'>
                {pagination && total > 0 && <>
                    {data.pending ? <LoadingSpinner/> : <span>{start} - {end} of {total}</span>}
                    <div>
                        <span className='mr-1'>Show</span>
                        <select className='form-control-sm mr-2' onChange={handleLimitChange} value={tableState.limit}>
                            <option value={10}>10</option>
                            <option value={25}>25</option>
                            <option value={100}>100</option>
                        </select>
                        <button
                            disabled={tableState.page === 1}
                            className='btn btn-sm btn-outline-secondary border-0'
                            onClick={() => handlePageChange(1)}
                        >
                            <i className='fas fa-angle-double-left'/>
                        </button>
                        <button
                            disabled={tableState.page === 1}
                            className='btn btn-sm btn-outline-secondary border-0'
                            onClick={() => handlePageChange(tableState.page - 1)}
                        >
                            <i className='fas fa-angle-left'/>
                        </button>
                        <button
                            disabled={tableState.page === totalPages}
                            className='btn btn-sm btn-outline-secondary border-0'
                            onClick={() => handlePageChange(tableState.page + 1)}
                        >
                            <i className='fas fa-angle-right'/>
                        </button>
                        <button
                            disabled={tableState.page === totalPages}
                            className='btn btn-sm btn-outline-secondary border-0'
                            onClick={() => handlePageChange(totalPages)}
                        >
                            <i className='fas fa-angle-double-right'/>
                        </button>
                    </div>
                </>}
            </div>
        </>
    );
}

DataProvider.propTypes = {
    additionalControls: PropTypes.node,
    dataUrl: PropTypes.string.isRequired,
    downloadOptions: PropTypes.arrayOf(downloadOptionType),
    onDataLoaded: PropTypes.func,
    pagination: PropTypes.bool,
    renderData: PropTypes.func,
    setTableState: PropTypes.func,
    sortOptions: PropTypes.arrayOf(sortOptionType),
    tableState: tableStateType,
};

export default DataProvider;
