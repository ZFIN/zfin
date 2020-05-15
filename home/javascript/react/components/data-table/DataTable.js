import React, {useState} from 'react';
import PropTypes from 'prop-types';
import produce from 'immer';
import LoadingSpinner from '../LoadingSpinner';
import {useTableDataFetch} from '../../utils/effects';
import {stringToFunction} from '../../utils';
import NoData from '../NoData';
import GenericErrorMessage from '../GenericErrorMessage';
import {columnDefinitionType} from '../../utils/types';
import HeaderCell from './HeaderCell';
import {DEFAULT_TABLE_STATE} from './index';

const DataTable = ({
    columns,
    downloadOptions,
    onTableStateChange,
    pagination = true,
    rowKey,
    tableState,
    url,
    sortOptions
}) => {
    if ((tableState && !onTableStateChange) || (!tableState && onTableStateChange)) {
        if (process.env.NODE_ENV === 'development') {
            console.warn('DataTable must either be controlled (by setting tableState and onTableStateChange) or uncontrolled (by setting neither)');
        }
    }

    const [controlledTableState, setControlledTableState] = useState(DEFAULT_TABLE_STATE);
    tableState = tableState || controlledTableState;
    const setTableState = onTableStateChange || setControlledTableState;

    const data = useTableDataFetch(url, tableState);

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

    const {results, returnedRecords, supplementalData, total} = data.value;

    if (total === 0 && Object.keys(tableState.filter).length === 0) {
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
            state.sortBy = sortBy;
        }))
    }

    const handleFilterChange = (field, value) => {
        setTableState(produce(state => {
            state.page = 1;
            state.filter[field] = value;
        }));
    }

    return (
        <div className='data-table-container'>
            <div className='d-flex justify-content-end'>
                {downloadOptions && downloadOptions.length > 0 &&
                    <div className='dropdown'>
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
                    <div className='dropdown'>
                        <button className='btn btn-sm dropdown-toggle' type='button' data-toggle='dropdown'>
                            Sort by
                        </button>
                        <div className='dropdown-menu dropdown-menu-right'>
                            {sortOptions.map((option, idx) => {
                                const isActive = (tableState.sortBy === null && idx === 0) || tableState.sortBy === option;
                                return (
                                    <button key={option} className='dropdown-item' type='button' onClick={() => handleSortChange(option)}>
                                        <i className={`fas fa-fw mr-1 ${isActive ? 'fa-check' : ''}`} />
                                        {option}
                                    </button>
                                );
                            })}
                        </div>
                    </div>
                }
            </div>

            <div className='horizontal-scroll-container'>
                <table className='data-table table-fixed'>
                    <thead>
                        <tr>
                            {columns.map(column => (
                                <HeaderCell
                                    column={column}
                                    defaultFilterValue={tableState.filter[column.filterName]}
                                    key={column.key || column.label}
                                    onChange={handleFilterChange}
                                />
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {results.map(row => (
                            <tr key={stringToFunction(rowKey)(row)}>
                                {columns.map(column => {
                                    if (column.hidden) {
                                        return null;
                                    }
                                    const valueGetter = stringToFunction(column.content);
                                    const value = valueGetter(row, supplementalData);
                                    return (
                                        <td key={column.key || column.label} style={{textAlign: column.align}}>
                                            {value}
                                        </td>
                                    );
                                })}
                            </tr>
                        ))
                        }
                        {total === 0 && (
                            <tr>
                                <td className='text-center' colSpan={columns.length}>
                                    <NoData placeholder='No rows match filters' />
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
            <div className='data-table-pagination'>
                {pagination && total > 0 && <React.Fragment>
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
                </React.Fragment>}
            </div>
        </div>
    );
};

DataTable.propTypes = {
    columns: PropTypes.arrayOf(columnDefinitionType).isRequired,
    downloadOptions: PropTypes.arrayOf(PropTypes.shape({
        format: PropTypes.string,
        url: PropTypes.string,
    })),
    onTableStateChange: PropTypes.func,
    pagination: PropTypes.bool,
    rowKey: PropTypes.oneOfType([PropTypes.string, PropTypes.func]).isRequired,
    sortOptions: PropTypes.arrayOf(PropTypes.string),
    tableState: PropTypes.shape({
        limit: PropTypes.number,
        page: PropTypes.number,
    }),
    url: PropTypes.string.isRequired,
};

export default DataTable;
