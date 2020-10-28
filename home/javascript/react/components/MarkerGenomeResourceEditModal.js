import React, { useState } from 'react';
import PropTypes from 'prop-types';
import Modal from './Modal';
import { useForm } from 'react-form';
import http from '../utils/http';
import FormGroup from './form/FormGroup';
import InputField from './form/InputField';
import PublicationInput from './PublicationInput';
import LoadingButton from './LoadingButton';

const MarkerGenomeResourceEditModal = ({link, databaseOptions, markerId, isEdit, onAdd, onClose, onDelete, onEdit}) => {
    const [deleting, setDeleting] = useState(false);

    const {
        Form,
        meta: { isValid, isSubmitting, serverError, error },
        pushFieldValue,
        removeFieldValue,
        reset,
        setFieldMeta,
        setMeta,
        values,
    } = useForm({
        debugForm: false,
        defaultValues: link,
        validate: values => {
            if (values && values.references.length === 0) {
                return 'At least one reference is required';
            }
            return false;
        },
        onSubmit: async (values) => {
            try {
                if (isEdit) {
                    const updated = await http.post(`/action/marker/link/${link.dblinkZdbID}`, values);
                    onEdit(updated);
                } else {
                    const added = await http.post(`/action/marker/${markerId}/links`, values);
                    onAdd(added);
                }
                onClose();
            } catch (error) {
                if (error.responseJSON && error.responseJSON.fieldErrors && error.responseJSON.fieldErrors.length > 0) {
                    error.responseJSON.fieldErrors.forEach(fieldError => {
                        // react-form gets a little confused when we set a fieldMeta with the field 'references'. We
                        // should probably be using the nested field version (i.e. 'references.0.zdbID') but we can't do
                        // that now because it would break the existing STR editing interface. So for now, limit
                        // field-specific errors to just 'alias'
                        if (fieldError.field === 'alias') {
                            setFieldMeta(fieldError.field, { error: fieldError.message });
                        } else {
                            setMeta({ serverError: fieldError.message });
                        }
                    })
                } else {
                    setMeta({ serverError: 'Update not saved. Try again later.' });
                }
                throw error;
            }
        },
    });

    const handleCancel = () => {
        reset();
        onClose();
    }

    const handleDelete = async () => {
        setDeleting(true);
        try {
            await http.delete(`/action/marker/link/${link.dblinkZdbID}`);
            onDelete();
            onClose();
        } catch (error) {
            setMeta({ error: 'Could not delete alias. Try again later.' });
            throw error;
        }
        setDeleting(false);
    };

    return (
        <Modal open={link !== null}>
            <div className='popup-header'>{isEdit ? 'Edit' : 'Add'} Genome Resource</div>
            <div className='popup-body show-overflow'>
                {values &&
                <Form>
                    <FormGroup
                        inputClassName='col-md-10'
                        label='Database'
                        id='database'
                        field='referenceDatabaseZdbID'
                        tag='select'
                        validate={value => value ? false : 'A database is required'}
                    >
                        <option value='' />
                        {databaseOptions.map(database => (
                            <option value={database.zdbID} key={database.zdbID}>{database.name}</option>
                        ))}
                    </FormGroup>

                    <FormGroup
                        inputClassName='col-md-10'
                        label='Accession'
                        id='accession'
                        field='accession'
                        validate={value => value ? false : 'An accession is required'}
                    />

                    <div className='form-group row'>
                        <label className='col-md-2 col-form-label'>Citations</label>
                        <div className='col-md-10'>
                            {
                                values.references.map((reference, idx) => (
                                    <div key={idx} className={`d-flex align-items-baseline ${idx > 0 ? 'mt-2' : ''}`}>
                                        <div className='flex-grow-1'>
                                            <InputField
                                                tag={PublicationInput}
                                                field={`references.${idx}.zdbID`}
                                                validate={value => {
                                                    if (!value) {
                                                        return 'A publication ZDB ID is required';
                                                    }
                                                    return false
                                                }}
                                            />
                                        </div>
                                        <button
                                            type='button'
                                            onClick={() => removeFieldValue('references', idx)}
                                            className='btn btn-link'
                                        >
                                            <i className='fas fa-times' />
                                        </button>
                                    </div>
                                ))
                            }
                            <button
                                type='button'
                                className='btn btn-link px-0'
                                onClick={() => pushFieldValue('references', { zdbID: '' })}
                            >
                                Add Citation
                            </button>

                            {(error || serverError) &&
                            <div className='text-danger small'>{serverError} {error}</div>
                            }

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
                                onClick={handleCancel}
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
                </Form>
                }
            </div>
        </Modal>
    );
};

MarkerGenomeResourceEditModal.propTypes = {
    link: PropTypes.object,
    databaseOptions: PropTypes.array,
    markerId: PropTypes.string,
    isEdit: PropTypes.bool,
    onAdd: PropTypes.func,
    onClose: PropTypes.func,
    onDelete: PropTypes.func,
    onEdit: PropTypes.func,
};

export default MarkerGenomeResourceEditModal;
