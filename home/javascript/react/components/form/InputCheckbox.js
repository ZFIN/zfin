import React from 'react';
import { splitFormProps, useField } from 'react-form';

const InputCheckbox = React.forwardRef((props, ref) => {
    const [field, fieldOptions, rest] = splitFormProps(props);
    const {
        meta: { error, isTouched },
        getInputProps,
        setValue,
        value
    } = useField(field, fieldOptions);
    const invalid = isTouched && error;

    const onChange = (event) => {
        setValue(event.target.checked);
    }

    return (
        <>
            <input
                {...getInputProps({ ref, ...rest })}
                checked={value}
                className={`form-check-input ${invalid ? 'is-invalid' : ''}`}
                onChange={onChange}
                type='checkbox'
            />
            {invalid && <div className='text-danger small'>{error}</div>}
        </>
    );
});

export default InputCheckbox;
