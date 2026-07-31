import React, {useState, useEffect} from 'react';
import PropTypes from 'prop-types';
import Modal from '../Modal';

const DeleteSequenceModal = ({sequence, onDelete, onClose}) => {

    const [showDeleteSequenceInformationModal, setShowDeleteSequenceInformationModal] = useState(false);
    const [deleteSequenceInfo, setDeleteSequenceInfo] = useState(false);
    const [errorDeletingSequenceInfo, setErrorDeletingSequenceInfo] = useState('');

    useEffect(() => {
        setShowDeleteSequenceInformationModal(!!sequence);
    }, [sequence]);

    useEffect(() => {
        setDeleteSequenceInfo(sequence);
    }, [sequence]);

    const closeDeleteSequenceInfoModal = () => {
        setShowDeleteSequenceInformationModal(false);
        setDeleteSequenceInfo(false);
        setErrorDeletingSequenceInfo('');
        onClose();
    }

    const deleteSequenceInfoOnServer = async () => {
        const url = '/action/marker/link/' + deleteSequenceInfo.dblinkZdbID
        const response = await fetch(url, {method: 'DELETE'});

        if (!response.ok) {
            setErrorDeletingSequenceInfo('Error deleting sequence information');
        } else {
            setDeleteSequenceInfo(false);
            onDelete(deleteSequenceInfo);
            closeDeleteSequenceInfoModal();
        }
    }

    return <Modal open={showDeleteSequenceInformationModal} onClose={closeDeleteSequenceInfoModal} config={{escapeClose: true, clickClose: true, showClose: true}}>
        <h3>
            Delete Sequence Information
        </h3>
        <table>
            <tbody>
                <tr>
                    <td>
                        <a
                            style={{fontWeight: 'bold'}}
                            target='_blank'
                            rel='noreferrer'
                            href={deleteSequenceInfo.modalLink}
                        >
                            {deleteSequenceInfo.referenceDatabaseName}:{deleteSequenceInfo.accession}
                        </a>
                    </td>
                </tr>

                { deleteSequenceInfo.references && deleteSequenceInfo.references.map((ref) => {
                    return <tr key={ref.zdbID}>
                        <td>
                            {ref.zdbID}<br/>
                            {ref.title.length < 80 ?
                                <span><a target='_blank' rel='noreferrer' href={'/' + ref.zdbID}>{ref.title}</a></span> :
                                <span><a target='_blank' rel='noreferrer' href={'/' + ref.zdbID}>{ref.title.substring(0,55) + '...'}</a></span>
                            }
                        </td>
                    </tr>;
                })}

                <tr>
                    <td>
                        <div className='popup-actions'>
                            <button className='zfin-button cancel' onClick={closeDeleteSequenceInfoModal}>Cancel</button>{' '}
                            <button className='zfin-button reject' onClick={deleteSequenceInfoOnServer}>Delete</button>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>
                        {errorDeletingSequenceInfo && <span className='error'>{errorDeletingSequenceInfo}</span>}
                    </td>
                </tr>
            </tbody>
        </table>
    </Modal>

}

DeleteSequenceModal.propTypes = {
    markerId: PropTypes.string,
    sequence: PropTypes.object,
    onDelete: PropTypes.func,
    onClose: PropTypes.func,
}

export default DeleteSequenceModal;