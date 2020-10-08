import React, { useState } from 'react';
import PropTypes from 'prop-types';
import Modal from './Modal';
import LoadingButton from './LoadingButton';
import { useForm } from 'react-form';
import FormGroup from './form/FormGroup';
import InputField from './form/InputField';
import PublicationInput from './PublicationInput';
import http from '../utils/http';

const MarkerAliasEditModal = ({ alias, markerId, onClose, onDelete, onSave }) => {
    const [deleting, setDeleting] = useState(false);

    const isEdit = alias && alias.zdbID;

    const {
        Form,
        pushFieldValue,
        setMeta,
        setFieldMeta,
        values,
        meta: { isValid, isSubmitting, error }
    } = useForm({
        debugForm: true,
        defaultValues: alias,
        onSubmit: async (values) => {
            try {
                const updated = await http.post(`/action/marker/${markerId}/aliases`, values);
                onSave(updated);
                onClose();
            } catch (error) {
                if (error.responseJSON && error.responseJSON.fieldErrors && error.responseJSON.fieldErrors.length > 0) {
                    error.responseJSON.fieldErrors.forEach(fieldError => {
                        setFieldMeta(fieldError.field, { error: fieldError.message });
                    })
                } else {
                    setMeta({ error: 'Update not saved. Try again later.' });
                }
                throw error;
            }
        },
    });

    const handleDelete = async () => {
        setDeleting(true);
        try {
            await http.delete(`/action/marker/alias/${alias.zdbID}`);
            onDelete();
            onClose();
        } catch (error) {
            setMeta({ error: 'Could not delete alias. Try again later.' });
            throw error;
        }
        setDeleting(false);
    };

    return (
        <Modal open={values !== null}>
            <div className='popup-header'>{isEdit ? 'Edit' : 'Add'} Alias</div>
            <div className='popup-body'>
                {values &&
                <Form>
                    <FormGroup
                        inputClassName='col-md-10'
                        label='Name'
                        id='alias'
                        field='alias'
                        validate={value => value ? false : 'An alias is required'}
                    />

                    <div className='form-group row'>
                        <label className='col-md-2 col-form-label'>References</label>
                        <div className='col-md-10'>
                            {
                                values.references.map((reference, idx) => (
                                    <InputField
                                        tag={PublicationInput}
                                        key={idx}
                                        field={`references.${idx}.zdbID`}
                                    />
                                ))
                            }
                            <button
                                type='button'
                                className='btn btn-link px-0'
                                onClick={() => pushFieldValue('references', { zdbID: '' })}
                            >
                                Add Reference
                            </button>
                        </div>
                    </div>

                    <div className='d-flex justify-content-between'>
                        {isEdit ?
                            <LoadingButton
                                className='btn btn-danger'
                                loading={deleting}
                                onClick={handleDelete}
                                type='button'
                            >
                                Delete
                            </LoadingButton> :
                            <span />
                        }
                        <span className='horizontal-buttons'>
                            <button
                                className='btn btn-outline-secondary'
                                onClick={onClose}
                                type='button'
                            >
                                Cancel
                            </button>
                            <LoadingButton
                                className='btn btn-primary'
                                disabled={isSubmitting || !isValid}
                                type='submit'
                                loading={isSubmitting}
                            >
                                Save
                            </LoadingButton>
                        </span>
                    </div>

                    {error && <span className='text-danger'>{error}</span>}
                </Form>
                }
            </div>
        </Modal>
    );
};

MarkerAliasEditModal.propTypes = {
    alias: PropTypes.object,
    markerId: PropTypes.string,
    onClose: PropTypes.func,
    onDelete: PropTypes.func,
    onSave: PropTypes.func,
};

export default MarkerAliasEditModal;
