import React from 'react';
import PropTypes from 'prop-types';

const TextBoxFilter = ({value, onChange}) => {
    return (
        <div className='position-relative'>
            <input
                className='form-control form-control-sm'
                type='text'
                value={value || ''}
                onChange={event => onChange(event.target.value)}
            />
            <button
                className='input-overlay-button p-1'
                onClick={() => onChange('')}
            >
                <i className='fas fa-times' />
            </button>
        </div>
    )
};

TextBoxFilter.propTypes = {
    value: PropTypes.string,
    onChange: PropTypes.func,
};

export default TextBoxFilter;
