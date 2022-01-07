import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { DEFAULT_TABLE_STATE } from './index';
import { columnDefinitionType, sortOptionType } from '../../utils/types';
import useTableDataFetch from '../../hooks/useTableDataFetch';
import LoadingSpinner from '../LoadingSpinner';
import Table from './Table';
import SortByDropdown from './SortByDropdown';
import produce, {setAutoFreeze} from 'immer';
setAutoFreeze(false);

import NoData from '../NoData';

const CollapseTable = ({
    dataUrl,
    columns,
    collapsedSize = 2,
    sortOptions,
    rowKey,
}) => {
    const [tableState, setTableState] = useState({
        ...DEFAULT_TABLE_STATE,
        limit: null,
        page: null,
    });
    const [isCollapsed, setCollapsed] = useState(true);
    const { value, pending } = useTableDataFetch(dataUrl, tableState);

    const handleSortChange = (sortBy) => {
        setTableState(produce(state => {
            state.sortBy = sortBy;
        }));
    };

    const handleToggleCollapsed = (event) => {
        event.preventDefault();
        setCollapsed(prev => !prev);
    }

    if (!value) {
        if (pending) {
            return <LoadingSpinner/>;
        } else {
            return null;
        }
    }

    if (value.results.length === 0) {
        return <NoData />;
    }

    const fullLength = value.results.length;
    const canCollapse = collapsedSize < fullLength;
    const end = isCollapsed ? collapsedSize : fullLength;
    const data = value.results.slice(0, end);
    const displayedLength = data.length;

    return (
        <div className='data-table-container'>
            <div className='d-flex justify-content-end align-items-start'>
                <SortByDropdown options={sortOptions} value={tableState.sortBy} onChange={handleSortChange} />
            </div>

            <Table
                columns={columns}
                data={data}
                rowKey={rowKey}
            />

            <div className='data-pagination-container'>
                <span>1 - {displayedLength} of {fullLength}</span>
                {canCollapse && (
                    <a href='#' className='text-dark' onClick={handleToggleCollapsed}>
                        <span className={`fa-animation-container ${isCollapsed ? '' : 'fa-rotate-180'}`}>
                            <i className='fas fa-angle-down' />
                        </span>{' '}
                        Show {isCollapsed ? 'all' : 'fewer'}
                    </a>
                )}
            </div>
        </div>
    )
};

CollapseTable.propTypes = {
    columns: PropTypes.arrayOf(columnDefinitionType).isRequired,
    dataUrl: PropTypes.string,
    collapsedSize: PropTypes.number,
    sortOptions: PropTypes.arrayOf(sortOptionType),
    rowKey: PropTypes.oneOfType([PropTypes.string, PropTypes.func]).isRequired,
}

export default CollapseTable;
