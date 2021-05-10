import React from 'react';
import { splitFormProps, useField } from 'react-form';

const InputField = React.forwardRef((props, ref) => {
    const {
        tag = 'input',
        ...propsWithoutTag
    } = props;
    const [field, fieldOptions, rest] = splitFormProps(propsWithoutTag);

    // Use the useField hook with a field and field options
    // to access field state
    const {
        meta: { error, isTouched },
        getInputProps
    } = useField(field, fieldOptions);

    const invalid = isTouched && error;
    const Wrapper = tag;

    // Build the field
    return (
        <>
            <Wrapper {...getInputProps({ ref, ...rest })} className={`form-control ${invalid ? 'is-invalid' : ''}`} />
            {invalid && <div className='text-danger small'>{error}</div>}
        </>
    );
});

export default InputField;
