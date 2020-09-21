import React from 'react';
import PropTypes from 'prop-types';
import useMutableFetch from '../hooks/useMutableFetch';
import PubFileRow from '../components/pub-edit/PubFileRow';
import http from '../utils/http';
import PubFileUpload from '../components/pub-edit/PubFileUpload';
import LoadingSpinner from '../components/LoadingSpinner';
import ProcessorApproval from './ProcessorApproval';

const PubEditFiles = ({ pubId }) => {
    const {
        pending,
        value,
        setValue,
    } = useMutableFetch(`/action/publication/${pubId}/files`);

    const handleDelete = async (file) => {
        await http.delete('/action/publication/files/' + file.id);
        setValue({
            ...value,
            results: value.results.filter(keepFile => keepFile.id !== file.id)
        });
    };

    if (pending || !value) {
        return <LoadingSpinner />
    }

    return (
        <>
            <table className='table'>
                <thead>
                    <tr>
                        <th width='175px'>Type</th>
                        <th>File</th>
                        <th width='75px'>Remove</th>
                    </tr>
                </thead>
                <tbody>
                    {!value.results.length === 0 &&
                        <tr>
                            <td className='text-muted text-center' colSpan='3'>No files yet.</td>
                        </tr>
                    }
                    {value.results.map(file => (
                        <PubFileRow key={file.originalFileName} file={file} onDelete={handleDelete} />
                    ))}
                </tbody>
            </table>
            <h4>Upload New File</h4>
            <PubFileUpload
                pubId={pubId}
                pubHasOriginalArticle={value.results.some(file => file.type.name === 'Original Article')}
                fileTypeOptions={value.supplementalData.fileTypes}
                onSuccess={setValue}
            />
            <ProcessorApproval
                pubId={pubId}
                task='ADD_PDF'
            />
        </>
    );
};

PubEditFiles.propTypes = {
    pubId: PropTypes.string,
}

export default PubEditFiles;