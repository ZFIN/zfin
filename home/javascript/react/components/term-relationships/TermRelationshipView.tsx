import React, {useState} from 'react';
import TermRelationshipInlineView from './TermRelationshipInlineView';
import TermRelationshipTable from './TermRelationshipTable';
import {TermRelationshipTypeStat} from '../../containers/TermRelationshipListView';

export interface RelatedTerm {
    zdbID: string;
    termName: string;
    oboID: string;
    abbreviation: string;
}

interface TermRelationshipViewProps {
    relationshipType: TermRelationshipTypeStat;
    termId: string;
}

export default function TermRelationshipView({relationshipType, termId,}: TermRelationshipViewProps) {
    const [isTableView, setIsTableView] = useState(false);

    function displayAllTerms() {
        setIsTableView(true);
    }

    return (
        <>
            {isTableView ? (
                <TermRelationshipTable
                    termId={termId}
                    relationshipType={relationshipType}
                />
            ) : (
                <TermRelationshipInlineView
                    termId={termId}
                    relationshipType={relationshipType}
                    expandCallback={() => {
                        displayAllTerms();
                    }}
                />
            )}
        </>
    );
}
