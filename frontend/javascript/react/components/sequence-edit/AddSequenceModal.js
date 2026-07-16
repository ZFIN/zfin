import React, {useState, useEffect} from 'react';
import PropTypes from 'prop-types';
import Modal from '../Modal';
import {validateNewSequenceInfo} from './SequenceValidation';

const AddSequenceModal = ({markerId, linkDatabases, show, onAdd, onClose}) => {
    const emptyNewSequenceInfo = {database: '', accession: '', length: '', reference: ''};
    const [newSequenceInfo, setNewSequenceInfo] = useState(emptyNewSequenceInfo);
    const [errorAddingSequenceInfo, setErrorAddingSequenceInfo] = useState('');
    const [showNewSequenceInformationModal, setShowNewSequenceInformationModal] = useState(show);
    const [validationErrors, setValidationErrors] = useState({});

    useEffect(() => {
        setShowNewSequenceInformationModal(show);
    }, [show]);

    const clearNewSequenceInfo = () => {
        setNewSequenceInfo(emptyNewSequenceInfo);
    }

    const closeAddSequenceInfoModal = () => {
        setShowNewSequenceInformationModal(false);
        clearNewSequenceInfo();
        setErrorAddingSequenceInfo('');
        setValidationErrors({});
        onClose();
    }

    async function addSequenceInfo() {
        const validationErrors = validateNewSequenceInfo(newSequenceInfo);
        setValidationErrors(validationErrors);
        if (Object.keys(validationErrors).length > 0) {
            return;
        }

        const url = '/action/marker/' + markerId + '/links';
        const data = {
            'referenceDatabaseZdbID': newSequenceInfo.database,
            'accession': newSequenceInfo.accession,
            'length': newSequenceInfo.length,
            'references': [
                {
                    'zdbID': newSequenceInfo.reference
                }
            ],
        }

        const response = await fetch(url,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data),
            });

        if (!response.ok) {
            setErrorAddingSequenceInfo('Error adding sequence information');
        } else {
            closeAddSequenceInfoModal();
            onAdd();
        }
    }

    return (
        <Modal open={showNewSequenceInformationModal} onClose={closeAddSequenceInfoModal} config={{escapeClose: true, clickClose: true, showClose: true}}>
            <h3>
                Add New Sequence Information
            </h3>
            <table>
                <tbody>
                    <tr>
                        <td>Database:</td>
                        <td>
                            <select
                                className='form-control'
                                onChange={(e) => {setNewSequenceInfo({...newSequenceInfo, database: e.target.value})}}
                                value={newSequenceInfo.database}
                            >
                                <option value=''/>
                                {linkDatabases && linkDatabases.map((database, index) => {
                                    return <option
                                        key={index}
                                        value={database.zdbID}
                                    >{database.nameAndType}</option>
                                })}
                            </select>
                        </td>
                        {validationErrors.database && <td><span className='error'>{validationErrors.database}</span></td>}
                    </tr>
                    <tr>
                        <td>Accession:</td>
                        <td>
                            <input
                                onChange={(e) => {setNewSequenceInfo({...newSequenceInfo, accession: e.target.value})}}
                                value={newSequenceInfo.accession}
                            />
                        </td>
                        {validationErrors.accession && <td><span className='error'>{validationErrors.accession}</span></td>}
                    </tr>
                    <tr>
                        <td>Length:</td>
                        <td>
                            <input
                                onChange={(e) => {setNewSequenceInfo({...newSequenceInfo, length: e.target.value})}}
                                value={newSequenceInfo.length}
                            />
                        </td>
                        {validationErrors.length && <td><span className='error'>{validationErrors.length}</span></td>}
                    </tr>
                    <tr>
                        <td>Reference:</td>
                        <td>
                            <input
                                onChange={(e) => {setNewSequenceInfo({...newSequenceInfo, reference: e.target.value})}}
                                value={newSequenceInfo.reference}
                            />
                        </td>
                        {validationErrors.reference && <td><span className='error'>{validationErrors.reference}</span></td>}
                    </tr>
                    <tr>
                        <td colSpan='3'>
                            <button className='zfin-button cancel' onClick={closeAddSequenceInfoModal}>Cancel</button>{' '}
                            <button className='zfin-button approve' onClick={addSequenceInfo}>Add</button>
                        </td>
                    </tr>
                </tbody>
            </table>
            {errorAddingSequenceInfo && <span className='error' >{errorAddingSequenceInfo}</span>}
        </Modal>
    );
}

AddSequenceModal.propTypes = {
    markerId: PropTypes.string,
    linkDatabases: PropTypes.array,
    show: PropTypes.bool,
    onAdd: PropTypes.func,
    onClose: PropTypes.func,
}

export default AddSequenceModal;