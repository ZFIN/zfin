import React, {useEffect, useState} from 'react';
import CurateConstructForm from "./CurateConstructForm";
import {backendBaseUrl} from "./DomainInfo";
import {ConstructFormDTO, MarkerNameAndZdbId} from './ConstructTypes';

const calculatedDomain = backendBaseUrl();

interface CurateConstructEditProps {
    publicationId: string;
}

const CurateConstructEdit = ({publicationId}: CurateConstructEditProps) => {
    const [display, setDisplay] = useState<boolean>(false);
    const [displayEditForm, setDisplayEditForm] = useState<boolean>(false);
    const [selectedConstruct, setSelectedConstruct] = useState<any>(null);
    const [constructList, setConstructList] = useState<any[]>([]);
    const [errorMessage, setErrorMessage] = useState<string>('');
    const [successMessage, setSuccessMessage] = useState<string>('');

    function toggleDisplay() {
        setDisplay(!display);
    }

    async function loadConstructList() {
        const response = await fetch(`${calculatedDomain}/action/api/publication/${publicationId}/constructs`);
        const data = await response.json();
        const constructIdNameList = data.map((row: any) : MarkerNameAndZdbId => {
            return {
                zdbID: row.constructDTO.zdbID,
                label: row.constructDTO.name
            }
        });
        const uniqueConstructIdNameList = constructIdNameList.filter((v, i, a) => a.findIndex(t => (t.zdbID === v.zdbID)) === i);
        setConstructList(uniqueConstructIdNameList);
        return data;
    }

    async function submitForm(submissionObject : ConstructFormDTO) {
        setSuccessMessage('');
        setErrorMessage('');
        try {
            //post with fetch to `/action/construct/create`
            const result = await fetch(`${calculatedDomain}/action/construct/update/${selectedConstruct}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(submissionObject),
            });
            loadConstructList();
            const bodyJson = await result.json();
            setSuccessMessage(bodyJson.message);
            setSelectedConstruct(null);
        } catch (error) {
            setErrorMessage('Error updating construct');
        }
    }

    async function handleConstructSelected(constructId: string) {
        setSuccessMessage('');
        setErrorMessage('');
        setSelectedConstruct(constructId);
        setDisplayEditForm(true);
    }

    useEffect(() => {
        loadConstructList();
    }, []);

    return <>
        <div className={`mb-3 pub-${publicationId}`}>
            <span className='bold'>EDIT CONSTRUCT: </span>
            <a onClick={toggleDisplay} style={{textDecoration: 'underline'}}>{display ? 'Hide' : 'Show'}</a>
            {display && <div className='mt-2'>
                <div>
                    <select onChange={(e) => handleConstructSelected(e.target.value)}>
                        <option value={''} selected={!selectedConstruct}>Select a construct</option>
                        {constructList.map((row: MarkerNameAndZdbId) => {
                            return <option key={row.zdbID} value={row.zdbID} selected={row.zdbID === selectedConstruct}>{row.label}</option>
                        })}
                    </select>
                </div>
            </div>}
            {displayEditForm && <div className='mt-2'>
                <CurateConstructForm publicationId={publicationId} constructId={selectedConstruct} submitButtonLabel='Update' onSubmit={submitForm}/>
                <div className='mt-2'>
                    {successMessage && <div className='alert alert-success'>{successMessage}</div>}
                    {errorMessage && <div className='alert alert-danger'>{errorMessage}</div>}
                </div>
            </div>}
        </div>
    </>;
}

export default CurateConstructEdit;