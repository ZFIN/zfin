import { useForm } from 'react-form';
import http from '../utils/http';
import { useState } from 'react';

export default function useAddEditDeleteForm({
    addUrl,
    editUrl,
    deleteUrl,
    items,
    setItems,
    itemKeyProp = 'zdbID',
    onSuccess,
    defaultValues,
    ...rest
}) {
    const [deleting, setDeleting] = useState(false);
    const isEdit = !!editUrl;

    const formInstance = useForm({
        defaultValues,
        onSubmit: async (values) => {
            try {
                if (isEdit) {
                    const updated = await http.post(editUrl, values);
                    const updatedIdx = items.findIndex(item => item[itemKeyProp] === defaultValues[itemKeyProp]);
                    setItems([
                        ...items.slice(0, updatedIdx),
                        updated,
                        ...items.slice(updatedIdx + 1)
                    ]);
                } else {
                    const added = await http.post(addUrl, values);
                    setItems([...items, added]);
                }
                onSuccess();
            } catch (error) {
                if (error.responseJSON && error.responseJSON.fieldErrors && error.responseJSON.fieldErrors.length > 0) {
                    error.responseJSON.fieldErrors.forEach(fieldError => {
                        // react-form gets a little confused when we set a fieldMeta with the field 'references'. We
                        // should probably be using the nested field version (i.e. 'references.0.zdbID') but we can't do
                        // that now because it would break the existing STR editing interface. So for now, limit
                        // field-specific errors to just simple fields
                        const fieldValue = formInstance.getFieldValue(fieldError.field);
                        if (fieldValue !== undefined && !Array.isArray(fieldValue)) {
                            formInstance.setFieldMeta(fieldError.field, { error: fieldError.message });
                        } else {
                            formInstance.setMeta({ serverError: fieldError.message });
                        }
                    })
                } else {
                    formInstance.setMeta({ serverError: 'Update not saved. Try again later.' });
                }
                throw error;
            }
        },
        ...rest
    });

    const handleCancel = () => {
        formInstance.reset();
        onSuccess();
    }

    const handleDelete = async () => {
        setDeleting(true);
        try {
            await http.delete(deleteUrl);
            setItems(items.filter(item => item[itemKeyProp] !== defaultValues[itemKeyProp]));
            onSuccess();
        } catch (error) {
            formInstance.setMeta({ error: 'Could not delete alias. Try again later.' });
            throw error;
        }
        setDeleting(false);
    };

    const modalProps = {
        Form: formInstance.Form,
        formMeta: formInstance.meta,
        isOpen: defaultValues !== null,
        isEdit,
        deleting,
        onDelete: handleDelete,
        onCancel: handleCancel,
    }

    return {
        ...formInstance,
        modalProps,
    };
}