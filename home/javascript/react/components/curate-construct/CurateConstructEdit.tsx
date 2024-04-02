import React, {useEffect, useState} from 'react';
import CurateConstructForm from "./CurateConstructForm";
import {backendBaseUrl} from "./DomainInfo";
import {MarkerNameAndZdbId} from './ConstructTypes';

const calculatedDomain = backendBaseUrl();

interface CurateConstructEditProps {
    publicationId: string;
}

const CurateConstructEdit = ({publicationId}: CurateConstructEditProps) => {
    const [display, setDisplay] = useState<boolean>(false);
    const [displayEditForm, setDisplayEditForm] = useState<boolean>(false);
    const [selectedConstruct, setSelectedConstruct] = useState<any>(null);
    const [constructList, setConstructList] = useState<any[]>([]);

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

    // async function loadConstructDetails(constructId: string) {
    //     const response = await fetch(`${calculatedDomain}/action/construct/json/${constructId}`);
    //     const data = await response.json();
    //     return data;
    // }

    async function handleConstructSelected(constructId: string) {
        setSelectedConstruct(constructId);
        // await loadConstructDetails(constructId);
        setDisplayEditForm(true);
    }

    async function submitForm() {
        return loadConstructList();
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
                        <option value={null}>Select a construct</option>
                        {constructList.map((row: MarkerNameAndZdbId) => {
                            return <option key={row.zdbID} value={row.zdbID}>{row.label}</option>
                        })}
                    </select>
                </div>
            </div>}
            {displayEditForm && <div className='mt-2'>
                <CurateConstructForm publicationId={publicationId} constructId={selectedConstruct} submitButtonLabel='Update' onSubmit={submitForm}/>
            </div>
            }
        </div>
    </>;
}

export default CurateConstructEdit;