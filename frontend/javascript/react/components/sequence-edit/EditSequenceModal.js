import React, {useState, useEffect} from 'react';
import PropTypes from 'prop-types';
import Modal from '../Modal';
import {validateUpdateSequenceInfo} from './SequenceValidation';
const EditSequenceModal = ({markerId, sequence, onEdit, onClose}) => {

    const emptyUpdateSequenceInfo = {referenceDatabaseDisplay: '', accession: '', length: '', reference: '', references: [{zdbID: '', title: ''}], modalTitle: '', modalLink: ''};
    const [errorUpdatingSequenceInfo, setErrorUpdatingSequenceInfo] = useState('');
    const [showUpdateSequenceInformationModal, setShowUpdateSequenceInformationModal] = useState(false);
    const [updateSequenceInfo, setUpdateSequenceInfo] = useState(emptyUpdateSequenceInfo);
    const [validationErrors, setValidationErrors] = useState({});

    useEffect(() => {
        setShowUpdateSequenceInformationModal(!!sequence);
    }, [sequence]);

    useEffect(() => {
        if (sequence) {
            setUpdateSequenceInfo({
                ...sequence,
                length: sequence.length ? sequence.length : '',
                reference: sequence.reference ? sequence.reference : '',
            });
        }
    }, [sequence]);

    const closeUpdateSequenceInfoModal = () => {
        setUpdateSequenceInfo(emptyUpdateSequenceInfo);
        setShowUpdateSequenceInformationModal(false);
        setValidationErrors({});
        setErrorUpdatingSequenceInfo('');
        onClose();
    };


    const saveUpdateSequenceInfoModal = async () => {
        const validationErrors = validateUpdateSequenceInfo(updateSequenceInfo);
        setValidationErrors(validationErrors);
        if (Object.keys(validationErrors).length > 0) {
            return;
        }

        const deleteResults = await removeLink(updateSequenceInfo);
        if (!deleteResults.ok) {
            setErrorUpdatingSequenceInfo('Error deleting sequence information');
            return;
        }

        const url = '/action/marker/' + markerId + '/links';
        const data = {
            'referenceDatabaseZdbID': updateSequenceInfo.referenceDatabaseZdbID,
            'accession': updateSequenceInfo.accession,
            'references': updateSequenceInfo.references.map(ref => {return {zdbID: ref.zdbID}}),
            'length': updateSequenceInfo.length
        };

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            setErrorUpdatingSequenceInfo('Error updating sequence information');
        } else {
            closeUpdateSequenceInfoModal();
            onEdit(updateSequenceInfo);
        }
    };

    const removeLink = async (link) => {
        //use fetch to send a delete request to the server
        const url = '/action/marker/link/' + link.dblinkZdbID;
        return await fetch(url, {method: 'DELETE'});
    };

    const addAttributionToSequence = async () => {
        const url = '/action/marker/link/' + updateSequenceInfo.dblinkZdbID + '/references';
        const data = {
            'zdbID': updateSequenceInfo.reference,
        };

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            setErrorUpdatingSequenceInfo('Error updating sequence information attribution');
        } else {
            setUpdateSequenceInfo({...updateSequenceInfo,
                reference: '',
                references: [...updateSequenceInfo.references, {zdbID: updateSequenceInfo.reference}]});
            onEdit(updateSequenceInfo);
        }
    };

    const deleteAttributionFromSequence = async (reference) => {
        const url = '/action/marker/link/' + updateSequenceInfo.dblinkZdbID + '/references/' + reference.zdbID;

        const response = await fetch(url, {method: 'DELETE'});

        if (!response.ok) {
            setErrorUpdatingSequenceInfo('Error deleting sequence information attribution');
        } else {
            const filteredReferences = updateSequenceInfo.references.filter(ref => ref.zdbID !== reference.zdbID);
            setUpdateSequenceInfo({...updateSequenceInfo, references: filteredReferences});
            onEdit(updateSequenceInfo);
        }
    };

    return (
        <Modal open={showUpdateSequenceInformationModal} onClose={closeUpdateSequenceInfoModal} config={{escapeClose: true, clickClose: true, showClose: true}}>

            <h3>
                Update <a
                    style={{fontWeight: 'bold'}}
                    target='_blank'
                    rel='noreferrer'
                    href={updateSequenceInfo.modalLink}
                >{updateSequenceInfo.modalTitle}
                </a>
            </h3>
            {errorUpdatingSequenceInfo && <div className='error'>{errorUpdatingSequenceInfo}</div>}
            <table>
                <tbody>
                    <tr>
                        <td>Database:&nbsp;&nbsp;{updateSequenceInfo.referenceDatabaseDisplay}</td>
                    </tr>
                    <tr>
                        <td>Accession:&nbsp;
                            <input
                                value={updateSequenceInfo.accession}
                                onChange={(e) => {setUpdateSequenceInfo({...updateSequenceInfo, accession: e.target.value})}}
                            />
                        </td>
                        {validationErrors.accession && <td><span className='error'>{validationErrors.accession}</span></td>}
                    </tr>
                    <tr>
                        <td>Length:&nbsp;
                            <input
                                value={updateSequenceInfo.length}
                                onChange={(e) => {setUpdateSequenceInfo({...updateSequenceInfo, length: e.target.value})}}
                            />
                        </td>
                        {validationErrors.length && <td><span className='error'>{validationErrors.length}</span></td>}
                    </tr>
                    <tr>
                        <td colSpan='2'>
                            <button type='button' className='zfin-button cancel' onClick={closeUpdateSequenceInfoModal}>Cancel</button>{' '}
                            <button type='button' className='zfin-button approve type-button' onClick={saveUpdateSequenceInfoModal}>Update</button>
                        </td>
                    </tr>
                </tbody>
            </table>
            <table>
                <tbody>
                    { updateSequenceInfo.references && updateSequenceInfo.references.map((ref) => {
                        return <tr key={ref.zdbID}>
                            <td>
                                <a
                                    target='_blank'
                                    rel='noreferrer'
                                    href={'/' + ref.zdbID}
                                >{ref.zdbID}</a>
                            </td>
                            <td>
                                {updateSequenceInfo.references.length > 1 &&
                                    <a
                                        onClick={() => {deleteAttributionFromSequence(ref)}}
                                        href='#'
                                    >
                                        <img alt='Delete' src='/images/delete-button.png'/>
                                    </a>
                                }
                            </td>
                        </tr>
                    })}
                    <tr>
                        <td>Reference:&nbsp;
                            <input
                                value={updateSequenceInfo.reference}
                                onChange={(e) => {setUpdateSequenceInfo({...updateSequenceInfo, reference: e.target.value})}}
                            /></td>
                        {validationErrors.reference && <td><span className='error'>{validationErrors.reference}</span></td>}
                    </tr>
                    <tr>
                        <td colSpan='2'>
                            <button onClick={closeUpdateSequenceInfoModal} className='zfin-button cancel'>Close</button>{' '}
                            <button onClick={addAttributionToSequence} className='zfin-button approve'>Add</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </Modal>
    );

}

EditSequenceModal.propTypes = {
    markerId: PropTypes.string,
    sequence: PropTypes.object,
    onEdit: PropTypes.func,
    onClose: PropTypes.func,
}

export default EditSequenceModal;