import React, { useCallback } from 'react';
import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';

const ImageUpload = ({ files, onFilesChange, permissionGranted, onPermissionChange }) => {
    const onDrop = useCallback((acceptedFiles) => {
        onFilesChange([...files, ...acceptedFiles]);
    }, [files, onFilesChange]);

    const removeFile = (index) => {
        onFilesChange(files.filter((_, i) => i !== index));
    };

    return (
        <div>
            <Dropzone onDrop={onDrop} accept={{ 'image/*': ['.png', '.jpg', '.jpeg', '.gif', '.tif', '.tiff'] }}>
                {({ getRootProps, getInputProps, isDragActive }) => (
                    <div
                        {...getRootProps()}
                        className={`border border-dashed rounded p-4 text-center mb-2 ${isDragActive ? 'bg-light border-primary' : ''}`}
                        style={{ cursor: 'pointer', borderStyle: 'dashed' }}
                    >
                        <input {...getInputProps()} />
                        <p className='mb-0 text-muted'>
                            {isDragActive
                                ? 'Drop image(s) here...'
                                : 'Drag & drop image(s) here, or click to select'}
                        </p>
                    </div>
                )}
            </Dropzone>

            {files.length > 0 && (
                <ul className='list-group mb-2'>
                    {files.map((file, i) => (
                        <li key={i} className='list-group-item d-flex justify-content-between align-items-center'>
                            <span>{file.name} ({(file.size / 1024).toFixed(1)} KB)</span>
                            <button
                                type='button'
                                className='btn btn-sm btn-outline-danger'
                                onClick={() => removeFile(i)}
                            >
                                Remove
                            </button>
                        </li>
                    ))}
                </ul>
            )}

            <div className='form-check mt-2'>
                <input
                    type='checkbox'
                    className='form-check-input'
                    id='imagePermission'
                    checked={permissionGranted}
                    onChange={(e) => onPermissionChange(e.target.checked)}
                />
                <label className='form-check-label' htmlFor='imagePermission'>
                    Does ZIRC have permission to publish these images on the ZIRC website?
                </label>
            </div>
        </div>
    );
};

ImageUpload.propTypes = {
    files: PropTypes.array.isRequired,
    onFilesChange: PropTypes.func.isRequired,
    permissionGranted: PropTypes.bool.isRequired,
    onPermissionChange: PropTypes.func.isRequired,
};

export default ImageUpload;
