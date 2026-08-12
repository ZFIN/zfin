import React from 'react';
import CurateConstructEdit from '../components/curate-construct/CurateConstructEdit';
import CurateConstructRelationships from '../components/curate-construct/CurateConstructRelationships';

interface CurateConstructProps {
    publicationId: string;
}

const CurateConstruct = ({publicationId} : CurateConstructProps) => {
    return (
        <>
            <CurateConstructEdit publicationId={publicationId}/>
            <CurateConstructRelationships publicationId={publicationId}/>
        </>
    );
};

export default CurateConstruct;

