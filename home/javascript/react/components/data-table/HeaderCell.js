import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import {columnDefinitionType} from '../../utils/types';
import {useDebouncedValue} from '../../utils/effects';

const UPDATE_TIMEOUT = 200;

const HeaderCell = ({column, defaultFilterValue, onChange}) => {
    const [filterOpen, setFilterOpen] = useState(false);
    const [filterValue, setFilterValue] = useState(defaultFilterValue);
    const debouncedValue = useDebouncedValue(filterValue, UPDATE_TIMEOUT);
    useEffect(() => {
        if (!column.filterName) {
            return;
        }
        onChange(column.filterName, debouncedValue);
    }, [debouncedValue]);
    useEffect(() => {
        setFilterValue(defaultFilterValue);
        if (typeof defaultFilterValue === 'undefined') {
            setFilterOpen(false);
        }
    }, [defaultFilterValue]);

    const toggleFilter = () => setFilterOpen(prev => !prev);
    const handleClear = () => {
        setFilterValue('');
    }

    if (!column || column.hidden) {
        return null;
    }

    return (
        <th style={{width: column.width, textAlign: column.align}}>
            {column.label} {column.filterName && (
                <button className='btn text-muted bg-transparent border-0 p-0' onClick={toggleFilter} role='button'>
                    <i className='fas fa-filter' />
                </button>
            )}
            {filterOpen && (
                <div className='position-relative'>
                    <input
                        className='form-control form-control-sm'
                        type='text'
                        value={filterValue}
                        onChange={event => setFilterValue(event.target.value)}
                    />
                    <button
                        className='input-overlay-button p-1'
                        onClick={handleClear}
                    >
                        <i className='fas fa-times' />
                    </button>
                </div>
            )}
        </th>
    );
};

HeaderCell.propTypes = {
    column: columnDefinitionType,
    defaultFilterValue: PropTypes.string,
    onChange: PropTypes.func,
};

export default HeaderCell;
