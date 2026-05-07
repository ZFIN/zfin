import React, {useEffect, useState} from 'react';
import CurateConstructForm from './CurateConstructForm';
import {backendBaseUrl} from './DomainInfo';
import {EditConstructFormDTO, MarkerLabelAndZdbId, MarkerNameAndZdbId} from './ConstructTypes';
import useCurationTabLoadEvent from '../../hooks/useCurationTabLoadEvent';

const calculatedDomain = backendBaseUrl();

interface CurateConstructEditProps {
    publicationId: string;
}

const CurateConstructEdit = ({publicationId}: CurateConstructEditProps) => {
    const [selectedConstructID, setSelectedConstructID] = useState<string>('');
    const [constructList, setConstructList] = useState<MarkerLabelAndZdbId[]>([]);
    const [showNew, setShowNew] = useState<boolean>(false);
    const [showEdit, setShowEdit] = useState<boolean>(false);
    const [newErrorMessage, setNewErrorMessage] = useState<string>('');
    const [newSuccessMessage, setNewSuccessMessage] = useState<string>('');
    const [editErrorMessage, setEditErrorMessage] = useState<string>('');
    const [editSuccessMessage, setEditSuccessMessage] = useState<string>('');
    const [lastCreatedConstructID, setLastCreatedConstructID] = useState<string>('');
    const [newFormKey, setNewFormKey] = useState<number>(0);
    const [pending, setPending] = useState<boolean>(true);

    useCurationTabLoadEvent('CONSTRUCT', pending);

    async function loadConstructList() {
        const response = await fetch(`${calculatedDomain}/action/api/publication/${publicationId}/constructs`);
        const data = await response.json();
        const constructIdLabelList : MarkerLabelAndZdbId[] = data.map((row : MarkerNameAndZdbId) : MarkerLabelAndZdbId => {
            return {
                zdbID: row.zdbID,
                label: row.name
            }
        });
        const uniqueConstructIdLabelList = uniqueByZdbID(constructIdLabelList);
        setConstructList(uniqueConstructIdLabelList);
        setPending(false);
    }

    function uniqueByZdbID(constructIdLabelList: MarkerLabelAndZdbId[]) {
        const seen = new Set<string>();
        const uniqueConstructIdLabelList = constructIdLabelList.filter(row => {
            if (seen.has(row.zdbID)) {
                return false;
            }
            seen.add(row.zdbID);
            return true;
        });
        return uniqueConstructIdLabelList;
    }

    async function submitNewForm(submissionObject : EditConstructFormDTO) {
        setNewSuccessMessage('');
        setNewErrorMessage('');
        try {
            const url = `${calculatedDomain}/action/construct/create-and-update`;
            const result = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(submissionObject),
            });
            const bodyJson = await result.json();
            if (bodyJson.success === false) {
                setNewErrorMessage('Error creating construct: ' + bodyJson.message);
            } else {
                loadConstructList();
                setNewSuccessMessage(bodyJson.message);
                setNewErrorMessage('');
                if (bodyJson.zdbID) {
                    setLastCreatedConstructID(bodyJson.zdbID);
                }
                setNewFormKey(prev => prev + 1);
            }
        } catch {
            setNewErrorMessage('Error creating construct');
        }
    }

    async function submitEditForm(submissionObject : EditConstructFormDTO) {
        setEditSuccessMessage('');
        setEditErrorMessage('');
        try {
            const url = `${calculatedDomain}/action/construct/update/${selectedConstructID}`;
            const result = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(submissionObject),
            });
            const bodyJson = await result.json();
            if (bodyJson.success === false) {
                setEditErrorMessage('Error updating construct: ' + bodyJson.message);
            } else {
                loadConstructList();
                setEditSuccessMessage(bodyJson.message);
                setEditErrorMessage('');
                // Force re-render of the edit form to reload updated data
                const currentId = selectedConstructID;
                setSelectedConstructID('');
                setTimeout(() => setSelectedConstructID(currentId), 0);
            }
        } catch {
            setEditErrorMessage('Error updating construct');
        }
    }

    function handleConstructSelected(constructId: string) {
        setEditSuccessMessage('');
        setEditErrorMessage('');
        setSelectedConstructID(constructId);
    }

    function cancelNew() {
        setShowNew(false);
        setNewErrorMessage('');
        setNewSuccessMessage('');
        setLastCreatedConstructID('');
    }

    function cancelEdit() {
        setShowEdit(false);
        setSelectedConstructID('');
        setEditErrorMessage('');
        setEditSuccessMessage('');
    }

    function activateEditMode() {
        if (lastCreatedConstructID) {
            setSelectedConstructID(lastCreatedConstructID);
            setShowEdit(true);
            setLastCreatedConstructID('');
            setNewSuccessMessage('');
        }
    }

    useEffect(() => {
        loadConstructList();
    }, []);

    return <>
        <div className={`mb-3 pub-${publicationId}`}>
            {/* NEW CONSTRUCT section */}
            <div>
                <span className='bold'>NEW CONSTRUCT: </span>
                <a
                    onClick={() => {
                        setShowNew(!showNew);
                        if (showNew) { setNewErrorMessage(''); setNewSuccessMessage(''); setLastCreatedConstructID(''); }
                    }}
                    style={{textDecoration: 'underline'}}
                >
                    {showNew ? 'Hide' : 'Show'}
                </a>
            </div>
            {showNew && <div className='mt-2'>
                <CurateConstructForm
                    key={`new-${newFormKey}`}
                    publicationId={publicationId}
                    constructId=''
                    submitButtonLabel='Save'
                    onCancel={cancelNew}
                    onSubmit={submitNewForm}
                />
                <div className='mt-2'>
                    {newSuccessMessage && <div className='alert alert-success' dangerouslySetInnerHTML={{__html: newSuccessMessage}}/>}
                    {newSuccessMessage && lastCreatedConstructID && <div><a onClick={() => activateEditMode()} style={{textDecoration: 'underline'}}>Edit this construct</a></div>}
                    {newErrorMessage && <div className='alert alert-danger'>{newErrorMessage}</div>}
                </div>
            </div>}

            {/* EDIT CONSTRUCT section */}
            <div className='mt-2'>
                <span className='bold'>EDIT CONSTRUCT: </span>
                <a
                    onClick={() => {
                        setShowEdit(!showEdit);
                        if (showEdit) { setSelectedConstructID(''); setEditErrorMessage(''); setEditSuccessMessage(''); }
                    }}
                    style={{textDecoration: 'underline'}}
                >
                    {showEdit ? 'Hide' : 'Show'}
                </a>
            </div>
            {showEdit && <div className='mt-2'>
                <div className='mb-2'>
                    <select onChange={(e) => handleConstructSelected(e.target.value)} value={selectedConstructID}>
                        <option value=''>Select a construct</option>
                        {constructList.map((row: MarkerLabelAndZdbId) => {
                            return <option key={row.zdbID} value={row.zdbID}>{row.label}</option>
                        })}
                    </select>
                </div>
                {selectedConstructID && <>
                    <CurateConstructForm
                        key={selectedConstructID}
                        publicationId={publicationId}
                        constructId={selectedConstructID}
                        submitButtonLabel='Update'
                        onCancel={cancelEdit}
                        onSubmit={submitEditForm}
                    />
                    <div className='mt-2'>
                        {editSuccessMessage && <div className='alert alert-success' dangerouslySetInnerHTML={{__html: editSuccessMessage}}/>}
                        {editErrorMessage && <div className='alert alert-danger'>{editErrorMessage}</div>}
                    </div>
                </>}
                {!selectedConstructID && constructList.length === 0 && <div>No constructs available for this publication.</div>}
            </div>}
        </div>
    </>;
}

export default CurateConstructEdit;
