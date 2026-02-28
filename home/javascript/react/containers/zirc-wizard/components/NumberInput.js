import React from 'react';
import PropTypes from 'prop-types';

const NumberInput = ({ label, value, onChange, min = 0, max = 100, id }) => {
    const handleChange = (e) => {
        const val = parseInt(e.target.value, 10);
        if (!isNaN(val) && val >= min && val <= max) {
            onChange(val);
        } else if (e.target.value === '') {
            onChange('');
        }
    };

    return (
        <div className='form-group row'>
            <label htmlFor={id} className='col-md-6 col-form-label'>{label}</label>
            <div className='col-md-2'>
                <input
                    type='number'
                    id={id}
                    className='form-control'
                    value={value}
                    onChange={handleChange}
                    min={min}
                    max={max}
                />
            </div>
        </div>
    );
};

NumberInput.propTypes = {
    label: PropTypes.string.isRequired,
    value: PropTypes.oneOfType([PropTypes.number, PropTypes.string]).isRequired,
    onChange: PropTypes.func.isRequired,
    min: PropTypes.number,
    max: PropTypes.number,
    id: PropTypes.string,
};

export default NumberInput;
