import React, {StrictMode, useEffect, useState} from 'react';
import PropTypes from 'prop-types';

const PubFileCheck = ({ pubId, initialFiles}) => {
    const [files, setFiles] = useState([]);
    const [emptyFiles, setEmptyFiles] = useState([]);
    const url = `/action/publication/${pubId}/files`;

    async function fetchFiles() {
        const response = await fetch(url);
        const data = await response.json();
        const tempFiles = [...data.results];
        const tempEmptyFiles = tempFiles.filter((file) => file.size === 0);
        setFiles(tempFiles);
        setEmptyFiles(tempEmptyFiles);
    }

    useEffect(() => {
        setTimeout(() => {
            fetchFiles();
        });
        // fetchFiles();
    }, [pubId, initialFiles]);

    if (files.length === 0) {
        return null;
    }

    if (emptyFiles.length === 0) {
        return null;
    }

    return (
        <StrictMode>
            <>
                <div className={'alert alert-danger'}>
                    <h5><i className='fa fa-warning'/>Warning: Empty Files Found</h5>
                    <ul>
                        {emptyFiles.map((file) => (
                            <li key={file.id}>
                                {file.type.name} - {file.originalFileName}
                            </li>
                        ))}
                    </ul>
                </div>
            </>
        </StrictMode>
    );
};

PubFileCheck.propTypes = {
    pubId: PropTypes.string,
    initialFiles: PropTypes.array,
}

export default PubFileCheck;