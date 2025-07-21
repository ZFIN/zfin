import React, {useEffect, useState} from 'react';
import CurateConstructForm from './CurateConstructForm';
import {backendBaseUrl} from './DomainInfo';
import {EditConstructFormDTO, MarkerLabelAndZdbId} from './ConstructTypes';

const calculatedDomain = backendBaseUrl();

interface CurateConstructEditProps {
    publicationId: string;
}

type DisplayMode = 'new' | 'edit' | 'list' | 'none';

const CurateConstructEdit = ({publicationId}: CurateConstructEditProps) => {
    const [selectedConstruct, setSelectedConstruct] = useState<string>('');
    const [constructList, setConstructList] = useState<MarkerLabelAndZdbId[]>([]);
    const [errorMessage, setErrorMessage] = useState<string>('');
    const [successMessage, setSuccessMessage] = useState<string>('');
    const [displayMode, setDisplayMode] = useState<DisplayMode>('none');

    function toggleDisplayMode(newMode: DisplayMode) {
        setDisplayMode(newMode);
    }

    async function loadConstructList() {
        const response = await fetch(`${calculatedDomain}/action/api/publication/${publicationId}/constructs`);
        const data = await response.json();
        const constructIdNameList = data.map((row) : MarkerLabelAndZdbId => {
            return {
                zdbID: row.zdbID,
                label: row.name
            }
        });
        const uniqueConstructIdNameList = constructIdNameList.filter((v, i, a) => a.findIndex(t => (t.zdbID === v.zdbID)) === i);
        setConstructList(uniqueConstructIdNameList);
    }

    async function submitForm(submissionObject : EditConstructFormDTO) {
        setSuccessMessage('');
        setErrorMessage('');
        try {
            let url = `${calculatedDomain}/action/construct/create-and-update`;
            if (selectedConstruct) {
                url = `${calculatedDomain}/action/construct/update/${selectedConstruct}`;
            }
            const result = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(submissionObject),
            });
            loadConstructList();
            const bodyJson = await result.json();
            if (bodyJson.success === false) {
                setErrorMessage('Error updating construct: ' + bodyJson.message);
            } else {
                setSuccessMessage(bodyJson.message);
                setSelectedConstruct(null);
            }
        } catch (error) {
            setErrorMessage('Error updating construct');
        }
    }

    async function handleConstructSelected(constructId: string) {
        setSuccessMessage('');
        setErrorMessage('');
        setSelectedConstruct(constructId);
        toggleDisplayMode('edit');
    }

    function cancelEdit() {
        toggleDisplayMode('none');
        setSelectedConstruct('');
    }

    useEffect(() => {
        loadConstructList();
    }, []);

    return <>
        <div className={`mb-3 pub-${publicationId}`}>
            {displayMode === 'none' && <>
                <span className='bold'>NEW CONSTRUCT: </span>
                <a onClick={() => toggleDisplayMode('new')} style={{textDecoration: 'underline'}}>Show</a>
                <br/>
                <span className='bold'>EDIT CONSTRUCT: </span>
                <a onClick={() => toggleDisplayMode('list')} style={{textDecoration: 'underline'}}>Show</a>
            </>}

            {displayMode === 'list' && <div className='mt-2'>
                <span className='bold'>EDIT CONSTRUCT: <a href='#' onClick={(e) => {e.preventDefault(); cancelEdit()}}>Back</a> </span>
                <div>
                    <select onChange={(e) => handleConstructSelected(e.target.value)} value={selectedConstruct}>
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
                <span className='bold'>EDIT CONSTRUCT: <a href='#' onClick={(e) => {e.preventDefault(); cancelEdit()}}>Back</a>  </span>
                <CurateConstructForm
                    key={selectedConstruct}
                    publicationId={publicationId}
                    constructId={selectedConstruct}
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
                <span className='bold'>NEW CONSTRUCT: <a href='#' onClick={(e) => {e.preventDefault(); cancelEdit()}}>Back</a>  </span>
                <CurateConstructForm
                    key={selectedConstruct}
                    publicationId={publicationId}
                    constructId={selectedConstruct}
                    submitButtonLabel='Save'
                    onCancel={cancelEdit}
                    onSubmit={submitForm}
                />
                <div className='mt-2'>
                    {successMessage && <div className='alert alert-success' dangerouslySetInnerHTML={{__html: successMessage}}/>}
                    {errorMessage && <div className='alert alert-danger'>{errorMessage}</div>}
                </div>
            </div>}
        </div>
    </>;
}

export default CurateConstructEdit;