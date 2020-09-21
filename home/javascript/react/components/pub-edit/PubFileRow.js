import React, { useState } from 'react';
import PropTypes from 'prop-types';

const PubFileRow = ({ file, onDelete }) => {
    const [deleting, setDeleting] = useState(false);

    const handleDelete = async () => {
        setDeleting(true);
        try {
            await onDelete(file);
        } catch (e) {
            setDeleting(false);
            throw e;
        }
    }

    return (
        <tr key={file.originalFileName}>
            <td>{file.type.name}</td>
            <td><a href={file.fullPath}>{file.originalFileName}</a></td>
            <td>
                <div className='figure-delete-button float-right'>
                    <button
                        className='btn btn-dense btn-link'
                        title='Remove file'
                        onClick={handleDelete}
                        disabled={deleting}
                    >
                        <i className='fas fa-trash' />
                    </button>
                </div>
            </td>
        </tr>
    )
};

PubFileRow.propTypes = {
    file: PropTypes.shape({
        fullPath: PropTypes.string,
        originalFileName: PropTypes.string,
        type: PropTypes.shape({
            id: PropTypes.number,
            name: PropTypes.string,
        }),
    }),
    onDelete: PropTypes.func,
};

export default PubFileRow;
