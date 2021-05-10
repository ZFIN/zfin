import React from 'react';
import PropTypes from 'prop-types';
import {stringToFunction} from '../utils';

const ObjectSelectBox = ({getDisplay, getValue, options, onChange, value, ...rest}) => {
    getDisplay = stringToFunction(getDisplay);
    const valueOrEmptyString = (value) => (value && stringToFunction(getValue)(value)) || '';

    const handleChange = (event) => {
        const selectedOption = options.find(option => valueOrEmptyString(option).toString() === event.target.value);
        onChange(selectedOption);
    };

    return (
        <select {...rest} onChange={handleChange} value={valueOrEmptyString(value)}>
            {options.map(option => (
                <option key={valueOrEmptyString(option)} value={valueOrEmptyString(option)}>
                    {getDisplay(option)}
                </option>
            ))}
        </select>
    );
};

ObjectSelectBox.propTypes = {
    getDisplay: PropTypes.oneOfType([PropTypes.string, PropTypes.func]),
    getValue: PropTypes.oneOfType([PropTypes.string, PropTypes.func]),
    options: PropTypes.array,
    onChange: PropTypes.func,
    value: PropTypes.object,
};

export default ObjectSelectBox;
