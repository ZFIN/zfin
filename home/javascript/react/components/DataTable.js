import React, { useState } from 'react';
import PropTypes from 'prop-types';
import produce from 'immer';
import LoadingSpinner from './LoadingSpinner';
import { useTableDataFetch } from '../utils/effects';
import {stringToFunction} from '../utils';

const DataTable = ({columns, pagination = true, rowKey, url}) => {
    const [tableState, setTableState] = useState({
        limit: 10,
        page: 1,
    });
    const data = useTableDataFetch(url, tableState);

    if (data.rejected) {
        return <span className='text-danger'>Something went wrong fetching data. Try again later or <a href='mailto:@ZFIN_ADMIN@'>contact us</a>.</span>
    }

    if (!data.value) {
        if (data.pending) {
            return <LoadingSpinner />
        } else {
            return null;
        }
    }

    const { results, returnedRecords, total } = data.value;

    if (total === 0) {
        return <i className='text-muted'>No data available</i>
    }

    const start = (tableState.page - 1) * tableState.limit + 1;
    const end = start + returnedRecords - 1;
    const totalPages = Math.ceil(total / tableState.limit);

    const handleLimitChange = (event) => {
        const limit = event.target.value;
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

    return (
        <div className='data-table-container'>
            <table className='data-table table-fixed'>
                <thead>
                    <tr>
                        {columns.map(column => (
                            <th key={column.label} style={{width: column.width, textAlign: column.align}}>
                                {column.label}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {results.map((row, idx, rows) => (
                        <tr key={stringToFunction(rowKey)(row)}>
                            {columns.map(column => {
                                let isRepeat = false;
                                const valueGetter = stringToFunction(column.content);
                                const value = valueGetter(row);
                                if (column.grouped && idx > 0) {
                                    const lastValue = valueGetter(rows[idx - 1]);
                                    isRepeat = value === lastValue;
                                }
                                return (
                                    <td key={column.label} className={isRepeat ? 'border-0' : ''} style={{textAlign: column.align}}>
                                        {!isRepeat && value}
                                    </td>
                                );
                            })}
                        </tr>
                    ))}
                </tbody>
            </table>
            <div className='data-table-pagination'>
                {pagination && <React.Fragment>
                    <span>{start} - {end} of {total}</span>
                    <div>
                        <span className='mr-1'>Show</span>
                        <select className='form-control-sm mr-2' onChange={handleLimitChange} value={tableState.limit}>
                            <option value={10}>10</option>
                            <option value={25}>25</option>
                            <option value={100}>100</option>
                        </select>
                        <button disabled={tableState.page === 1} className='btn btn-sm btn-outline-secondary border-0' onClick={() => handlePageChange(1)}>
                            <i className='fas fa-angle-double-left' />
                        </button>
                        <button disabled={tableState.page === 1} className='btn btn-sm btn-outline-secondary border-0' onClick={() => handlePageChange(tableState.page - 1)}>
                            <i className='fas fa-angle-left' />
                        </button>
                        <button disabled={tableState.page === totalPages} className='btn btn-sm btn-outline-secondary border-0' onClick={() => handlePageChange(tableState.page + 1)}>
                            <i className='fas fa-angle-right' />
                        </button>
                        <button disabled={tableState.page === totalPages} className='btn btn-sm btn-outline-secondary border-0' onClick={() => handlePageChange(totalPages)}>
                            <i className='fas fa-angle-double-right' />
                        </button>
                    </div>
                </React.Fragment>}
            </div>
        </div>
    );
};

DataTable.propTypes = {
    columns: PropTypes.arrayOf(PropTypes.shape({
        label: PropTypes.string.isRequired,
        content: PropTypes.oneOfType([PropTypes.string, PropTypes.func]).isRequired,
        grouped: PropTypes.bool,
    })).isRequired,
    pagination: PropTypes.bool,
    rowKey: PropTypes.oneOfType([PropTypes.string, PropTypes.func]).isRequired,
    url: PropTypes.string.isRequired,
};

export default DataTable;
