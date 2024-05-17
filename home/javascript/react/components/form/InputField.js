import React from 'react';
import { splitFormProps, useField } from 'react-form';

const InputField = React.forwardRef((props, ref) => {
    const {
        tag = 'input',
        onPasteTransform,
        ...propsWithoutTag
    } = props;
    const [field, fieldOptions, rest] = splitFormProps(propsWithoutTag);

    // Use the useField hook with a field and field options
    // to access field state
    const {
        meta: { error, isTouched },
        getInputProps,
        setValue,
    } = useField(field, fieldOptions);

    const handlePaste = e => {
        if (onPasteTransform) {
            e.preventDefault();
            const text = e.clipboardData.getData('text/plain');
            const transformedText = onPasteTransform(text);
            setValue(transformedText);
        }
    }

    const invalid = isTouched && error;
    const Wrapper = tag;

    // Build the field
    return (
        <>
            <Wrapper
                {...getInputProps({
                    ref,
                    ...rest,
                    onPaste: handlePaste,
                })}
                className={`form-control ${invalid ? 'is-invalid' : ''}`}
            />
            {invalid && <div className='text-danger small'>{error}</div>}
        </>
    );
});

export default InputField;
