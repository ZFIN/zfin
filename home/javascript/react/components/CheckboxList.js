import React from 'react';
import PropTypes from 'prop-types';
import { makeId } from '../utils';

const identityFunc = a => a;

const CheckboxList = ({getItemDisplay, getItemKey, items, value, onChange}) => {

    const handleChange = (checked, item) => {
        let newValue;
        const itemKey = getItemKey(item);
        if (checked) {
            newValue = value.concat(itemKey);
        } else {
            newValue = value.filter(val => val !== itemKey);
        }
        onChange(newValue);
    };

    return (
        <div>
            {items && items.map(item => (
                <div key={getItemKey(item)} className='form-check'>
                    <input
                        id={makeId(getItemKey(item))}
                        className='form-check-input'
                        type='checkbox'
                        checked={value && value.indexOf(getItemKey(item)) >= 0}
                        onChange={e => handleChange(e.target.checked, item)}
                    />
                    <label className='form-check-label' htmlFor={makeId(getItemKey(item))}>
                        {getItemDisplay(item)}
                    </label>
                </div>
            ))}
        </div>
    );
};

CheckboxList.propTypes = {
    getItemKey: PropTypes.func,
    getItemDisplay: PropTypes.func,
    items: PropTypes.array,
    value: PropTypes.array,
    onChange: PropTypes.func,
};

CheckboxList.defaultProps = {
    getItemKey: identityFunc,
    getItemDisplay: identityFunc,
};

export default CheckboxList;