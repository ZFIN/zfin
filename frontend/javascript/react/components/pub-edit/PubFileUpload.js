import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { useDropzone } from 'react-dropzone'
import LoadingButton from '../LoadingButton';
import http from '../../utils/http';

const PubFileUpload = ({ pubId, fileTypeOptions, pubHasOriginalArticle, onSuccess }) => {
    const [type, setType] = useState('');
    const [files, setFiles] = useState(null);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const {getRootProps, getInputProps, isDragActive} = useDropzone({
        onDrop: setFiles,
        multiple: false,
    });

    const onSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        const form = new FormData();
        form.append('fileType', type);
        form.append('file', files[0]);
        try {
            // special handling for binary form data
            const response = await http.post(`/action/publication/${pubId}/files`, null, {
                data: form,
                processData: false,
                contentType: false,
            });
            setType('');
            setFiles(null);
            setError('');
            onSuccess(response)
        } catch (error) {
            setError(error.responseJSON ? error.responseJSON.message : 'Something went wrong');
        }
        setSaving(false);
    }

    return (
        <form className='form-horizontal'>
            <div className='form-group row'>
                <label className='col-md-2 col-form-label'>Type</label>
                <div className='col-md-3'>
                    <select className='form-control' value={type} onChange={e => setType(e.target.value)}>
                        <option value='' />
                        {fileTypeOptions.map(option => (
                            <option key={option.id} value={option.id}>{option.name}</option>
                        ))}
                    </select>
                    {pubHasOriginalArticle && type === '1' &&
                        <p className='text-warning'>
                            Pub can have only one original article. The existing file will be replaced.
                        </p>
                    }
                </div>
            </div>
            <div className='form-group row'>
                <label className='col-md-2 col-form-label'>File</label>
                <div className='col-md-6'>
                    <div {...getRootProps({className: `file-drag-target ${isDragActive ? 'hover' : ''}`})}>
                        {files && files.length &&
                            <ul className='list-unstyled mb-3'>
                                {files.map(file => <li key={file.name}>{file.name}</li>)}
                            </ul>
                        }
                        <input {...getInputProps()} />
                        {
                            isDragActive ?
                                <p>Drop files here ...</p> :
                                <p>Drag and drop files here, or click to select files</p>
                        }
                    </div>
                </div>
            </div>
            <div className='form-group row'>
                <div className='offset-md-2 col-md-6'>
                    <LoadingButton
                        className='btn btn-primary'
                        disabled={type === '' || files === null || files.length === 0}
                        loading={saving}
                        onClick={onSubmit}
                    >
                        Save
                    </LoadingButton>
                    {error && <span className='text-danger'> {error}</span>}
                </div>
            </div>
        </form>
    );
};

PubFileUpload.propTypes = {
    pubId: PropTypes.string,
    fileTypeOptions: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.number,
        name: PropTypes.string,
    })),
    pubHasOriginalArticle: PropTypes.bool,
    onSuccess: PropTypes.func,
};

export default PubFileUpload;
