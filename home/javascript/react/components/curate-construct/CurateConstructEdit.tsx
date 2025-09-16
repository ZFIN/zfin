import React, {useEffect, useState} from 'react';
import CurateConstructForm from './CurateConstructForm';
import {backendBaseUrl} from './DomainInfo';
import {EditConstructFormDTO, MarkerLabelAndZdbId, MarkerNameAndZdbId} from './ConstructTypes';
import useCurationTabLoadEvent from '../../hooks/useCurationTabLoadEvent';

const calculatedDomain = backendBaseUrl();

interface CurateConstructEditProps {
    publicationId: string;
}

type DisplayMode = 'new' | 'edit' | 'list' | 'none';

const CurateConstructEdit = ({publicationId}: CurateConstructEditProps) => {
    const [selectedConstructID, setSelectedConstructID] = useState<string>('');
    const [constructList, setConstructList] = useState<MarkerLabelAndZdbId[]>([]);
    const [errorMessage, setErrorMessage] = useState<string>('');
    const [successMessage, setSuccessMessage] = useState<string>('');
    const [displayMode, setDisplayMode] = useState<DisplayMode>('none');
    const [lastCreatedConstructID, setLastCreatedConstructID] = useState<string>('');
    const [pending, setPending] = useState<boolean>(true);

    useCurationTabLoadEvent('CONSTRUCT', pending);

    function toggleDisplayMode(newMode: DisplayMode) {
        setErrorMessage('');
        setSuccessMessage('');
        if (newMode === 'new') {
            setSelectedConstructID('');
            resetSelectedConstruct();
        }
        // Clear the lastCreatedConstructID when changing modes
        if (newMode !== 'new') {
            setLastCreatedConstructID('');
        }
        setDisplayMode(newMode);
    }

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

    async function submitForm(submissionObject : EditConstructFormDTO) {
        setSuccessMessage('');
        setErrorMessage('');
        try {
            let url = `${calculatedDomain}/action/construct/create-and-update`;
            if (selectedConstructID) {
                url = `${calculatedDomain}/action/construct/update/${selectedConstructID}`;
            }
            const result = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(submissionObject),
            });
            const bodyJson = await result.json();
            if (bodyJson.success === false) {
                setErrorMessage('Error updating construct: ' + bodyJson.message);
            } else {
                loadConstructList();
                setSuccessMessage(bodyJson.message);
                setErrorMessage('');

                // Store the newly created construct ID for the "Edit" link
                if (bodyJson.zdbID) {
                    setLastCreatedConstructID(bodyJson.zdbID);
                }

                resetSelectedConstruct();
            }
        } catch (error) {
            setErrorMessage('Error updating construct');
        }
    }

    async function handleConstructSelected(constructId: string) {
        setSuccessMessage('');
        setErrorMessage('');
        setSelectedConstructID(constructId);
        toggleDisplayMode('edit');
    }

    function cancelEdit() {
        toggleDisplayMode('none');
        setSelectedConstructID('');
        resetSelectedConstruct();
    }

    function activateEditMode() {
        if (lastCreatedConstructID) {
            setSelectedConstructID(lastCreatedConstructID);
            setDisplayMode('edit');
            setSuccessMessage(''); // Clear success message when transitioning to edit mode
        }
    }

    function resetSelectedConstruct() {
        //force re-render of form by setting selectedConstruct to null (or empty string if already null)
        if (selectedConstructID === '') {
            setSelectedConstructID(null);
        } else if (selectedConstructID === null) {
            setSelectedConstructID('');
        } else {
            //this means we are updating an existing construct, so we should reset it to the same construct after re-rendering
            const currentSelectedConstructID = selectedConstructID;
            setSelectedConstructID('');
            setTimeout(() => {
                if (displayMode === 'edit') {
                    setSelectedConstructID(currentSelectedConstructID);
                }
            }, 0);
        }

    }

    useEffect(() => {
        loadConstructList();
    }, []);

    return <>
        <div className={`mb-3 pub-${publicationId} construct-mode-${displayMode}`}>
            {displayMode === 'none' && <>
                <span className='bold'>NEW CONSTRUCT: </span>
                <a onClick={() => toggleDisplayMode('new')} style={{textDecoration: 'underline'}}>Show</a>
                <br/>
                <span className='bold'>EDIT CONSTRUCT: </span>
                <a onClick={() => toggleDisplayMode('list')} style={{textDecoration: 'underline'}}>Show</a>
            </>}

            {displayMode === 'list' && <div className='mt-2'>
                <span className='bold'>EDIT CONSTRUCT: </span>
                <div>
                    <select onChange={(e) => handleConstructSelected(e.target.value)} value={selectedConstructID}>
                        <option>Select a construct</option>
                        {constructList.map((row: MarkerLabelAndZdbId) => {
                            return <option key={row.zdbID} value={row.zdbID} >{row.label}</option>
                        })}
                    </select>
                </div>
                <div>
                    <button onClick={cancelEdit}>Cancel</button>
                </div>
            </div>}

            {displayMode === 'edit' && <div className='mt-2'>
                <span className='bold'>EDIT CONSTRUCT: </span>
                <CurateConstructForm
                    key={selectedConstructID}
                    publicationId={publicationId}
                    constructId={selectedConstructID}
                    submitButtonLabel='Update'
                    onCancel={cancelEdit}
                    onSubmit={submitForm}
                />
                <div className='mt-2'>
                    {successMessage && <div className='alert alert-success' dangerouslySetInnerHTML={{__html: successMessage}}/>}
                    {errorMessage && <div className='alert alert-danger'>{errorMessage}</div>}
                </div>
            </div>}

            {displayMode === 'new' && <div className='mt-2'>
                <span className='bold'>NEW CONSTRUCT: </span>
                <CurateConstructForm
                    key={selectedConstructID}
                    publicationId={publicationId}
                    constructId={selectedConstructID}
                    submitButtonLabel='Save'
                    onCancel={cancelEdit}
                    onSubmit={submitForm}
                />
                <div className='mt-2'>
                    {successMessage && <div className='alert alert-success' dangerouslySetInnerHTML={{__html: successMessage}}/>}
                    {successMessage && lastCreatedConstructID && <div><a onClick={() => activateEditMode()} style={{textDecoration: 'underline'}}>Edit this construct</a></div>}
                    {errorMessage && <div className='alert alert-danger'>{errorMessage}</div>}
                </div>
            </div>}
        </div>
    </>;
}

export default CurateConstructEdit;
