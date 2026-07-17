import React, {useEffect, useState} from 'react';
import TermRelationshipView from '../components/term-relationships/TermRelationshipView';

export interface TermRelationshipTypeStat {
    relationshipType: string;
    humanName: string;
    isForward: boolean;
    count: number;
}

interface TermRelationshipTableListProps {
    termId: string;
}

const TermRelationshipListView = ({termId}: TermRelationshipTableListProps) => {

    const [relationshipTypes, setRelationshipTypes] = useState<TermRelationshipTypeStat[]>([]);
    useEffect(() => {
        loadRelationships();
    }, [termId]);

    async function loadRelationships() {
        await fetch(`/action/api/ontology/${termId}/relationshipTypes`)
            .then(response => response.json())
            .then(data => setRelationshipTypes(data));
    }

    return (
        <div>
            <dl className='row'>
                {relationshipTypes && relationshipTypes.map(relationshipType => (
                    <>
                        <dt className='col-sm-2 mb-sm-2 attribute-list-item-dt'>{relationshipType.humanName}</dt>
                        <dd className='col-sm-10 mb-sm-10 attribute-list-item-dd'>
                            <TermRelationshipView relationshipType={relationshipType} termId={termId}/>
                        </dd>
                    </>
                ))}
            </dl>
        </div>
    );
};

export default TermRelationshipListView;
