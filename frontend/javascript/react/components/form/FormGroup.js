import React from 'react';
import PropTypes from 'prop-types';
import InputField from './InputField';

const FormGroup = ({
    label,
    id,
    labelClassName = 'col-md-2 col-form-label',
    inputClassName = 'col-md-4',
    ...rest
}) => {
    return (
        <div className='form-group row'>
            <label htmlFor={id} className={labelClassName}>{label}</label>
            <div className={inputClassName}>
                <InputField id={id} {...rest} />
            </div>
        </div>
    )
};

FormGroup.propTypes = {
    label: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    labelClassName: PropTypes.string,
    inputClassName: PropTypes.string,
};

export default FormGroup;
