import React from 'react';
import PropTypes from 'prop-types';

const WizardField = ({
    label,
    id,
    value,
    onChange,
    tag = 'input',
    type = 'text',
    labelClassName = 'col-md-4 col-form-label',
    inputClassName = 'col-md-6',
    error,
    children,
    ...rest
}) => {
    const Wrapper = tag;

    const handleChange = (e) => {
        onChange(e.target.value);
    };

    return (
        <div className='form-group row'>
            <label htmlFor={id} className={labelClassName}>{label}</label>
            <div className={inputClassName}>
                {tag === 'select' ? (
                    <select
                        id={id}
                        className={`form-control ${error ? 'is-invalid' : ''}`}
                        value={value || ''}
                        onChange={handleChange}
                        {...rest}
                    >
                        {children}
                    </select>
                ) : (
                    <Wrapper
                        id={id}
                        type={type}
                        className={`form-control ${error ? 'is-invalid' : ''}`}
                        value={value || ''}
                        onChange={handleChange}
                        {...rest}
                    />
                )}
                {error && <div className='text-danger small'>{error}</div>}
            </div>
        </div>
    );
};

WizardField.propTypes = {
    label: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired,
    value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    onChange: PropTypes.func.isRequired,
    tag: PropTypes.string,
    type: PropTypes.string,
    labelClassName: PropTypes.string,
    inputClassName: PropTypes.string,
    error: PropTypes.string,
    children: PropTypes.node,
};

export default WizardField;
