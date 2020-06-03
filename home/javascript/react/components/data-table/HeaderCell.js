import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import {columnDefinitionType} from '../../utils/types';
import {useDebouncedValue} from '../../utils/effects';
import TextBoxFilter from './TextBoxFilter';
import CheckboxListFilter from './CheckboxListFilter';

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

    return (
        <>
            {column.label} {column.filterName && (
                <button className='btn text-muted bg-transparent border-0 p-0' onClick={toggleFilter} role='button'>
                    <i className='fas fa-filter' />
                </button>
            )}
            {filterOpen && (
                column.filterOptions ?
                    <CheckboxListFilter options={column.filterOptions} value={filterValue} onChange={setFilterValue} /> :
                    <TextBoxFilter value={filterValue} onChange={setFilterValue} />
            )}
        </>
    );
};

HeaderCell.propTypes = {
    column: columnDefinitionType,
    defaultFilterValue: PropTypes.string,
    onChange: PropTypes.func,
};

export default HeaderCell;
