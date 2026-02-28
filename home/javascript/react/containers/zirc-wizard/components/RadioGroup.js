import React from 'react';
import PropTypes from 'prop-types';

const RadioGroup = ({ name, options, value, onChange, inline = false }) => {
    return (
        <div className={inline ? 'd-flex flex-wrap' : ''}>
            {options.map(option => (
                <div className={`form-check ${inline ? 'form-check-inline' : ''}`} key={option.value}>
                    <input
                        type='radio'
                        className='form-check-input'
                        name={name}
                        id={`${name}-${option.value}`}
                        value={option.value}
                        checked={value === option.value}
                        onChange={() => onChange(option.value)}
                    />
                    <label className='form-check-label' htmlFor={`${name}-${option.value}`}>
                        {option.label}
                    </label>
                </div>
            ))}
        </div>
    );
};

RadioGroup.propTypes = {
    name: PropTypes.string.isRequired,
    options: PropTypes.arrayOf(PropTypes.shape({
        value: PropTypes.string.isRequired,
        label: PropTypes.string.isRequired,
    })).isRequired,
    value: PropTypes.string,
    onChange: PropTypes.func.isRequired,
    inline: PropTypes.bool,
};

export default RadioGroup;
