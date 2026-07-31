import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import PubFileRow from '../components/pub-edit/PubFileRow';
import http from '../utils/http';
import PubFileUpload from '../components/pub-edit/PubFileUpload';
import LoadingSpinner from '../components/LoadingSpinner';
import ProcessorApproval from './ProcessorApproval';
import PubFileEditModal from '../components/pub-edit/PubFileEditModal';
import useFetch from '../hooks/useFetch';
import PubFileCheck from './PubFileCheck';

const PubEditFiles = ({ pubId }) => {
    const [editFile, setEditFile] = useState(null);
    const [fileIDs, setFileIDs] = useState([]);

    const {
        pending,
        value,
        setValue,
    } = useFetch(`/action/publication/${pubId}/files`);

    useEffect(() => {
        if (!value || !value.results) {
            return;
        }
        const ids = value.results.map(file => file.id).sort();
        if (ids.join(',') !== fileIDs.join(',')) {
            setFileIDs(ids);
        }
    }, [value]);

    const handleDelete = async (file) => {
        await http.delete(`/action/publication/files/${file.id}`);
        setValue({
            ...value,
            results: value.results.filter(keepFile => keepFile.id !== file.id)
        });
    };

    const handleSave = async (file) => {
        const updatedValue = await http.post(`/action/publication/files/${file.id}`, file);
        setValue(updatedValue);
    }

    if (pending || !value) {
        return <LoadingSpinner />
    }

    const pubHasOriginalArticle = value.results.some(file => file.type.name === 'Original Article');

    return (
        <>
            <PubFileCheck pubId={pubId} initialFiles={fileIDs} uniqueId={'file-editor'} />
            <table className='table table-hover'>
                <thead>
                    <tr>
                        <th width='175px'>Type</th>
                        <th>File</th>
                        <th width='75px' />
                    </tr>
                </thead>
                <tbody>
                    {!value.results.length === 0 &&
                        <tr>
                            <td className='text-muted text-center' colSpan='3'>No files yet.</td>
                        </tr>
                    }
                    {value.results.map(file => (
                        <PubFileRow
                            key={file.originalFileName + ' ' + file.id}
                            file={file}
                            onDelete={handleDelete}
                            onEdit={setEditFile}
                        />
                    ))}
                </tbody>
            </table>

            <h4>Upload New File</h4>
            <PubFileUpload
                pubId={pubId}
                pubHasOriginalArticle={pubHasOriginalArticle}
                fileTypeOptions={value.supplementalData.fileTypes}
                onSuccess={setValue}
            />

            <PubFileEditModal
                file={editFile}
                fileTypeOptions={value.supplementalData.fileTypes}
                pubHasOriginalArticle={pubHasOriginalArticle}
                onClose={() => setEditFile(null)}
                onDelete={handleDelete}
                onSave={handleSave}
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