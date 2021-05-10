import React from 'react';
import PropTypes from 'prop-types';
import Modal from './Modal';
import LoadingButton from './LoadingButton';

const AddEditDeleteModal = ({children, header, Form, isEdit, isOpen, formMeta, deleting, onDelete, onCancel}) => {
    return (
        <Modal open={isOpen}>
            <div className='popup-header'>{isEdit ? 'Edit' : 'Add'} {header}</div>
            <div className='popup-body show-overflow'>
                <Form>
                    {children}

                    {(formMeta.error || formMeta.serverError) &&
                        <div className='text-danger'>{formMeta.serverError} {formMeta.error}</div>
                    }

                    <div className='d-flex justify-content-between'>
                        {isEdit ?
                            <LoadingButton
                                className='btn btn-danger'
                                loading={deleting}
                                onClick={onDelete}
                                type='button'
                            >
                                Delete
                            </LoadingButton> :
                            <span />
                        }
                        <span className='horizontal-buttons'>
                            <button
                                className='btn btn-outline-secondary'
                                onClick={onCancel}
                                type='button'
                            >
                                Cancel
                            </button>
                            <LoadingButton
                                className='btn btn-primary'
                                disabled={formMeta.isSubmitting || !formMeta.isValid}
                                type='submit'
                                loading={formMeta.isSubmitting}
                            >
                                Save
                            </LoadingButton>
                        </span>
                    </div>
                </Form>
            </div>
        </Modal>
    );
};

AddEditDeleteModal.propTypes = {
    children: PropTypes.node,
    Form: PropTypes.elementType,
    formMeta: PropTypes.shape({
        error: PropTypes.string,
        serverError: PropTypes.string,
        isSubmitting: PropTypes.bool,
        isValid: PropTypes.bool,
    }),
    isEdit: PropTypes.bool,
    isOpen: PropTypes.bool,
    header: PropTypes.string,
    deleting: PropTypes.bool,
    onDelete: PropTypes.func,
    onCancel: PropTypes.func,
};

export default AddEditDeleteModal;
