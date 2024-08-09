import React, {useState} from 'react';
import CurateConstructNew from '../components/curate-construct/CurateConstructNew';
import CurateConstructEdit from '../components/curate-construct/CurateConstructEdit';
import CurateConstructRelationships from '../components/curate-construct/CurateConstructRelationships';
import {EditConstructFormDTO} from "../components/curate-construct/ConstructTypes";

interface CurateConstructProps {
    publicationId: string;
}

const CurateConstruct = ({publicationId} : CurateConstructProps) => {
    const [createdConstructs, setCreatedConstructs] = useState<EditConstructFormDTO[]>([]);

    function onCreationSuccess(construct) {
        setCreatedConstructs([...createdConstructs, construct]);
    }

    return (
        <>
            <CurateConstructNew publicationId={publicationId} onSuccess={onCreationSuccess}/>
            <CurateConstructEdit publicationId={publicationId} createdConstructs={createdConstructs}/>
            <CurateConstructRelationships publicationId={publicationId}/>
        </>
    );
};

export default CurateConstruct;

