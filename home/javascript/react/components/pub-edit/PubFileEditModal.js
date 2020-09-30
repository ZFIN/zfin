import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import Modal from '../Modal';
import LoadingButton from '../LoadingButton';
import ObjectSelectBox from '../ObjectSelectBox';

const PubFileEditModal = ({ file, fileTypeOptions, pubHasOriginalArticle, onClose, onDelete, onSave }) => {
    const [deleting, setDeleting] = useState(false);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const [formType, setFormType] = useState(null);
    const [valid, setValid] = useState(true);

    useEffect(() => {
        if (!file) {
            return;
        }
        setFormType(file.type);
        setError('');
        setValid(true);
    }, [file]);

    const handleChange = (value) => {
        setFormType(value);
        if (pubHasOriginalArticle && value.name === 'Original Article') {
            setError('Publication can only have one Original Article. Delete or change type of existing Original Article before continuing.');
            setValid(false);
            return;
        }
        setValid(true);
    }

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

    const handleSave = async () => {
        setSaving(true);
        setError('');
        try {
            await onSave({
                ...file,
                type: formType,
            });
            onClose();
        } catch (err) {
            setError('Unable to save update. Try again later.')
        }
        setSaving(false);
    }

    return (
        <Modal open={file !== null}>
            <div className='popup-header'>Edit File</div>
            <div className='popup-body'>

                <form>
                    <div className='form-group row'>
                        <label className='col-md-2 col-form-label'>Type</label>
                        <div className='col-md-10'>
                            <ObjectSelectBox
                                className='form-control'
                                options={fileTypeOptions}
                                value={formType}
                                onChange={handleChange}
                                getDisplay='name'
                                getValue='id'
                            />
                        </div>
                    </div>

                    <div className='d-flex justify-content-between'>
                        <LoadingButton
                            className='btn btn-danger'
                            loading={deleting}
                            onClick={handleDelete}
                            type='button'
                        >
                            Delete
                        </LoadingButton>
                        <span className='horizontal-buttons'>
                            <button className='btn btn-outline-secondary' onClick={onClose} type='button'>Cancel</button>
                            <LoadingButton
                                className='btn btn-primary'
                                disabled={!valid}
                                type='button'
                                loading={saving}
                                onClick={handleSave}
                            >
                                Save
                            </LoadingButton>
                        </span>
                    </div>
                </form>

                {error && <span className='text-danger'>{error}</span>}
            </div>
        </Modal>
    )
};

PubFileEditModal.propTypes = {
    file: PropTypes.object,
    fileTypeOptions: PropTypes.array,
    pubHasOriginalArticle: PropTypes.bool,
    onClose: PropTypes.func,
    onDelete: PropTypes.func,
    onSave: PropTypes.func,
}

export default PubFileEditModal;