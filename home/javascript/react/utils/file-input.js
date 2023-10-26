import React, { useState, useRef, useEffect } from 'react';
import PropTypes from 'prop-types';

function FileInput({ multiple, accept = '', onChange = () => {}, reRenderKey = '' }) {
    const [files, setFiles] = useState([]);
    const [label, setLabel] = useState('');
    const [error, setError] = useState('');
    const [dragMsg, setDragMsg] = useState('');
    const inputRef = useRef(null);

    useEffect(() => {
        setLabel(multiple ? 'Choose files' : 'Choose a file');
        setDragMsg(multiple ? 'drag them here' : 'drag it here');
    }, [multiple, files]);

    useEffect(() => {
        if (reRenderKey) {
            setFiles([]);
        }
    }, [reRenderKey]);

    const handleFilesChange = (files) => {
        let validFiles = [];
        let invalidFiles = [];
        let errorMsg = '';
        const validType = accept.replace(/\*$/, '');

        files.forEach(file => {
            if (file.type.startsWith(validType)) {
                validFiles.push(file);
            } else {
                invalidFiles.push(file);
            }
        });

        if (invalidFiles.length > 0) {
            errorMsg = 'Invalid file type: ' + invalidFiles.map(f => f.name).join(', ');
        }

        if (!multiple) {
            validFiles = validFiles.slice(0, 1);
        }

        setFiles(validFiles);
        setError(errorMsg);
        onChange(validFiles);
    };

    const handleInputChange = (e) => {
        handleFilesChange(e.target.files);
    };

    const handleDrop = (e) => {
        e.preventDefault();
        handleFilesChange(e.dataTransfer.files);
    };

    const handleDragOver = (e) => {
        e.preventDefault();
        e.currentTarget.classList.add('hover');
    };

    const handleDragLeave = (e) => {
        e.preventDefault();
        e.currentTarget.classList.remove('hover');
    };

    return (
        <div
            className='file-drag-target'
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
        >
            <ul className='list-unstyled'>
                {files.map(file => <li key={file.name}>{file.name}</li>)}
            </ul>
            <input
                type='file'
                id='file-input'
                accept={accept}
                multiple={multiple}
                onChange={handleInputChange}
                ref={inputRef}
            />
            <label htmlFor='file-input'><strong>{label}</strong></label> or {dragMsg}.
            {error && <div className='text-danger'>{error}</div>}
        </div>
    );
}

FileInput.propTypes = {
    multiple: PropTypes.bool,
    accept: PropTypes.string,
    onChange: PropTypes.func,
    reRenderKey: PropTypes.string,
};

export default FileInput;
