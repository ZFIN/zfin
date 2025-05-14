import React, {useState} from 'react';
import TermRelationshipInlineView, {RelatedOntologyTermsProps} from './TermRelationshipInlineView';
import TermRelationshipTable from './TermRelationshipTable';

export interface RelatedTerm {
    zdbID: string;
    termName: string;
    oboID: string;
    abbreviation: string;
}

export default function TermRelationshipView({relationshipType, termId,}: RelatedOntologyTermsProps) {
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
