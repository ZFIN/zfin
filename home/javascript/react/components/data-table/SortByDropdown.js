import React from 'react';
import PropTypes from 'prop-types';
import { sortOptionType } from '../../utils/types';

const SortByDropdown = ({
    onChange,
    options,
    value,
}) => {
    if (!options || !options.length) {
        return null;
    }
    return (
        <div className='btn-group'>
            <button className='btn btn-sm dropdown-toggle' type='button' data-toggle='dropdown'>
                Sort by
            </button>
            <div className='dropdown-menu dropdown-menu-right'>
                {options.map((sort, idx) => {
                    const isActive = (value === null && idx === 0) || value === sort.value;
                    return (
                        <button key={sort.value} className='dropdown-item' type='button' onClick={() => onChange(sort.value)}>
                            <i className={`fas fa-fw mr-1 ${isActive ? 'fa-check' : ''}`}/>
                            {sort.label}
                        </button>
                    );
                })}
            </div>
        </div>
    )
};

SortByDropdown.propTypes = {
    onChange: PropTypes.func,
    options: PropTypes.arrayOf(sortOptionType),
    value: PropTypes.string,
};

export default SortByDropdown;
