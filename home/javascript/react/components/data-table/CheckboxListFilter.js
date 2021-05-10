import React from 'react';
import PropTypes from 'prop-types';
import CheckboxList from '../CheckboxList';

const DELIMITER = '|';

const CheckboxListFilter = ({value, onChange, options}) => {
    return (
        <CheckboxList
            items={options}
            value={value ? value.split(DELIMITER) : []}
            onChange={(values) => onChange(values.join(DELIMITER))}
        />
    );
}

CheckboxListFilter.propTypes = {
    onChange: PropTypes.func,
    options: PropTypes.array,
    value: PropTypes.string,
};

export default CheckboxListFilter;
