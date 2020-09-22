import React, { useState } from 'react';
import PropTypes from 'prop-types';
import Modal from '../Modal';
import LoadingButton from '../LoadingButton';

const PubFileEditModal = ({ file, onClose, onDelete }) => {
    const [deleting, setDeleting] = useState(false);
    const [error, setError] = useState('');

    const handleDelete = async () => {
        setDeleting(true);
        setError('');
        try {
            await onDelete(file);
            onClose();
        } catch (err) {
            setError('Unable to delete file. Try again later.')
        }
        setDeleting(false);
    }

    return (
        <Modal open={file !== null}>
            <div className='popup-header'>Edit File</div>
            <div className='popup-body'>
                <div className='d-flex justify-content-between'>
                    <LoadingButton
                        className='btn btn-danger'
                        loading={deleting}
                        onClick={handleDelete}
                    >
                        Delete
                    </LoadingButton>
                    <span className='horizontal-buttons'>
                        <button className='btn btn-outline-secondary' onClick={onClose}>Cancel</button>
                        <button className='btn btn-primary'>Save</button>
                    </span>
                    {error && <span className='text-danger'>{error}</span>}
                </div>
            </div>
        </Modal>
    )
};

PubFileEditModal.propTypes = {
    file: PropTypes.object,
    onClose: PropTypes.func,
    onDelete: PropTypes.func,
}

export default PubFileEditModal;