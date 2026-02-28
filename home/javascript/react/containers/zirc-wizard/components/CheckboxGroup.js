import React from 'react';
import PropTypes from 'prop-types';

const CheckboxGroup = ({ name, options, values, onChange }) => {
    const handleToggle = (optionValue) => {
        if (values.includes(optionValue)) {
            onChange(values.filter(v => v !== optionValue));
        } else {
            onChange([...values, optionValue]);
        }
    };

    return (
        <div>
            {options.map(option => (
                <div className='form-check' key={option.value}>
                    <input
                        type='checkbox'
                        className='form-check-input'
                        id={`${name}-${option.value}`}
                        checked={values.includes(option.value)}
                        onChange={() => handleToggle(option.value)}
                    />
                    <label className='form-check-label' htmlFor={`${name}-${option.value}`}>
                        {option.label}
                    </label>
                </div>
            ))}
        </div>
    );
};

CheckboxGroup.propTypes = {
    name: PropTypes.string.isRequired,
    options: PropTypes.arrayOf(PropTypes.shape({
        value: PropTypes.string.isRequired,
        label: PropTypes.string.isRequired,
    })).isRequired,
    values: PropTypes.arrayOf(PropTypes.string).isRequired,
    onChange: PropTypes.func.isRequired,
};

export default CheckboxGroup;
