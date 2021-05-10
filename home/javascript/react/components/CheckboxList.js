import React from 'react';
import PropTypes from 'prop-types';
import { makeId } from '../utils';

const identityFunc = a => a;

const CheckboxList = ({getItemDisplay, getItemKey, itemIdPrefix = '', items, onChange, value}) => {

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
            {items && items.map(item => {
                const id = itemIdPrefix + makeId(getItemKey(item));
                return (
                    <div key={getItemKey(item)} className='form-check'>
                        <input
                            id={id}
                            className='form-check-input'
                            type='checkbox'
                            checked={value && value.indexOf(getItemKey(item)) >= 0}
                            onChange={e => handleChange(e.target.checked, item)}
                        />
                        <label className='form-check-label' htmlFor={id}>
                            {getItemDisplay(item)}
                        </label>
                    </div>
                )
            })}
        </div>
    );
};

CheckboxList.propTypes = {
    getItemDisplay: PropTypes.func,
    getItemKey: PropTypes.func,
    itemIdPrefix: PropTypes.string,
    items: PropTypes.array,
    onChange: PropTypes.func,
    value: PropTypes.array,
};

CheckboxList.defaultProps = {
    getItemKey: identityFunc,
    getItemDisplay: identityFunc,
};

export default CheckboxList;