import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import useDebouncedValue from '../../hooks/useDebouncedValue';

const UPDATE_TIMEOUT = 200;

const TextBoxFilter = ({value, onChange, placeholder}) => {
    const [inputValue, setInputValue] = useState(value);
    const debouncedValue = useDebouncedValue(inputValue, UPDATE_TIMEOUT);
    useEffect(() => onChange(debouncedValue), [debouncedValue]);
    return (
        <div className='position-relative'>
            <input
                className='form-control form-control-sm'
                placeholder={placeholder}
                type='text'
                value={inputValue || ''}
                onChange={event => setInputValue(event.target.value)}
            />
        </div>
    )
};

TextBoxFilter.propTypes = {
    value: PropTypes.string,
    onChange: PropTypes.func,
    placeholder: PropTypes.string,
};

export default TextBoxFilter;
