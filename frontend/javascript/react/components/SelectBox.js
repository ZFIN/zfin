import React from 'react';
import PropTypes from 'prop-types';

const SelectBox = ({options, value, onSelect, showAny}) => {
    return (
        <select className='form-control' onChange={event => onSelect(event.target.value)} value={value}>
            {showAny && <option value=''>Any</option>}
            {options.map(option => (
                <option key={option.value} value={option.value}>
                    {option.display}
                </option>
            ))}
        </select>
    );
};

SelectBox.propTypes = {
    options: PropTypes.array,
    value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    onSelect: PropTypes.func,
    showAny: PropTypes.bool,
};

export default SelectBox;