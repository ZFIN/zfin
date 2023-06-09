import React, {useState, useEffect} from 'react';
import PropTypes from 'prop-types';
import Modal from './Modal';
const EditNomenclatureModal = ({nomenclature, reasons, onEdit, onAttributionDeleted, onAttributionAdded}) => {

    const emptyNomenclature = {zdbID: '', reason: '', comments: '', attributions: [], attribution: ''};
    const [showEditNomenclatureModal, setShowEditNomenclatureModal] = useState(!!nomenclature);
    const [validationErrors, setValidationErrors] = useState({});
    const [nomenclatureModalData, setNomenclatureModalData] = useState(emptyNomenclature);
    const [errorSavingMessage, setErrorSavingMessage] = useState('');

    useEffect(() => {
        setShowEditNomenclatureModal(!!nomenclature);
    }, [nomenclature]);

    useEffect(() => {
        if (nomenclature) {
            setNomenclatureModalData({...nomenclature, attribution: ''});
        }
    }, [nomenclature]);

    function closeModal() {
        setShowEditNomenclatureModal(false);
        setValidationErrors({});
        setNomenclatureModalData(emptyNomenclature);
    }

    function handleReasonChanged(e) {
        setNomenclatureModalData({...nomenclatureModalData, reason: e.target.value});
    }

    function handleCommentChanged(e) {
        setNomenclatureModalData({...nomenclatureModalData, comments: e.target.value});
    }

    function handleAttributionChange(e) {
        setNomenclatureModalData({...nomenclatureModalData, attribution: e.target.value});
    }

    async function saveNomenclatureToServer() {
        const url = '/action/nomenclature/update/' + nomenclatureModalData.zdbID;
        const data = {comments: nomenclatureModalData.comments, reason: nomenclatureModalData.reason};
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            setErrorSavingMessage('Error saving information');
        } else {
            closeModal();
            onEdit(nomenclatureModalData);
        }
    }

    function handleUpdateClicked() {
        const validationErrors = validateNomenclature(nomenclatureModalData);
        setValidationErrors(validationErrors);
        if (Object.keys(validationErrors).length > 0) {
            return;
        }

        saveNomenclatureToServer();
    }

    function validateNomenclature(nomenclature) {
        const errors = {};
        if (!nomenclature.reason) {
            errors.reason = 'Reason is required';
        }
        return errors;
    }

    async function handleDeleteAttributionClick(ref) {
        const url = '/action/nomenclature/deleteAttribution/' + nomenclatureModalData.zdbID + '/' + ref;
        const response = await fetch(url, {
            method: 'DELETE'
        });
        if (!response.ok) {
            setErrorSavingMessage('Error saving attribution');
        } else {
            const attributions = nomenclatureModalData.attributions.filter((attribution) => {
                return attribution !== ref;
            });
            const newData = {...nomenclatureModalData, attribution: '', attributions};
            setNomenclatureModalData(newData);
            onAttributionDeleted(newData);
        }
    }

    async function handleAddAttributionClick() {
        if (!nomenclatureModalData.attribution) {
            return;
        }

        const url = '/action/nomenclature/addAttribution/' + nomenclatureModalData.zdbID;
        const data = nomenclatureModalData.attribution;
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: data
        });

        if (!response.ok) {
            setErrorSavingMessage('Error saving attribution');
        } else {
            const attributions = [...nomenclatureModalData.attributions, nomenclatureModalData.attribution];
            const newData = {...nomenclatureModalData, attribution: '', attributions};
            setNomenclatureModalData(newData);
            onAttributionAdded(newData);
        }
    }

    return (
        <Modal open={showEditNomenclatureModal} onClose={closeModal} config={{escapeClose: true, clickClose: true, showClose: true}}>

            <h3>
                Nomenclature Edit
            </h3>
            {errorSavingMessage && <div className='error'>{errorSavingMessage}</div>}
            <table>
                <tbody>
                    <tr>
                        <td>ID:</td>
                        <td>{nomenclatureModalData.zdbID}</td>
                    </tr>
                    {validationErrors.reason && <tr><td colSpan='2'><span className='error'>{validationErrors.reason}</span></td></tr>}
                    <tr>
                        <td>Reason:&nbsp;</td>
                        <td>
                            <select value={nomenclatureModalData.reason} onChange={handleReasonChanged}>
                                <option value=''>Select a reason</option>
                                {reasons.map((reason) => {
                                    return <option key={reason} value={reason}>{reason}</option>
                                })}
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td>Comments:&nbsp;</td>
                        <td>
                            <textarea value={nomenclatureModalData.comments} onChange={handleCommentChanged}/>
                        </td>
                    </tr>
                    <tr>
                        <td colSpan='2'>
                            <button onClick={handleUpdateClicked} className='zfin-button approve'>Update</button>
                        </td>
                    </tr>
                </tbody>
            </table>
            <h4>Attributions</h4>
            <table>
                <tbody>
                    { nomenclatureModalData.attributions && nomenclatureModalData.attributions.map((ref) => {
                        return <tr key={ref}>
                            <td>
                                <a
                                    target='_blank'
                                    rel='noreferrer'
                                    href={'/' + ref}
                                >{ref}</a>
                            </td>
                            <td>
                                {nomenclatureModalData.attributions.length > 1 &&
                                    <a
                                        onClick={() => {handleDeleteAttributionClick(ref)}}
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
                            <input value={nomenclatureModalData.attribution} onChange={handleAttributionChange}/>
                        </td>
                        {validationErrors.attribution && <td><span className='error'>{validationErrors.attribution}</span></td>}
                    </tr>
                    <tr>
                        <td colSpan='2'>
                            <button onClick={closeModal} className='zfin-button cancel'>Close</button>{' '}
                            <button onClick={handleAddAttributionClick} className={'zfin-button ' + (nomenclatureModalData.attribution ? 'approve' : 'disabled')}>Add</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </Modal>
    );

}

EditNomenclatureModal.propTypes = {
    nomenclature: PropTypes.object,
    reasons: PropTypes.array,
    onAttributionAdded: PropTypes.func,
    onAttributionDeleted: PropTypes.func,
    onEdit: PropTypes.func,
    onClose: PropTypes.func,
}

export default EditNomenclatureModal;