import React from 'react';
import PropTypes from 'prop-types';

const PubFileRow = ({ file, onEdit }) => {

    const handleEditClick = (e) => {
        e.preventDefault();
        onEdit(file);
    }

    return (
        <tr key={file.originalFileName}>
            <td>{file.type.name}</td>
            <td><a href={file.fullPath}>{file.originalFileName}</a></td>
            <td><a className='show-on-hover' href='#' onClick={handleEditClick}>Edit</a></td>
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
    onEdit: PropTypes.func,
};

export default PubFileRow;
