import React, {useEffect, useState} from 'react';
import {TermRelationshipTypeStat} from '../../containers/TermRelationshipListView';
import qs from 'qs';
import {RelatedTerm} from "./TermRelationshipView";

export interface RelatedOntologyTermsProps {
    relationshipType: TermRelationshipTypeStat;
    termId: string;
    expandCallback: () => void;
}

// In this context, works as a substitute for zfin:link (aka CreateLinkTag.java)
export const TermLink = ({term}: { term: RelatedTerm }) => {
    return (
        <>
            <a href={`/action/ontology/term/${term.oboID}`}>{term.termName}</a>
            <a
                href={`/action/ontology/term-detail-popup?termID=${term.oboID}`}
                className='popup-link data-popup-link'
                title='Term definition, synonyms and links'
            />
        </>
    );
};

export default function TermRelationshipInlineView({relationshipType, termId, expandCallback}: RelatedOntologyTermsProps) {
    const [showAll, setShowAll] = useState(false);
    const [relationships, setRelationships] = useState([]);
    const totalCount = relationshipType.count;

    useEffect(() => {
        loadRelationships();
    }, []);

    async function loadRelationships() {
        const params = {
            relationshipType: relationshipType.relationshipType,
            isForward: relationshipType.isForward,
        };

        await fetch(
            `/action/api/ontology/${termId}/relationships?${qs.stringify(params)}`
        )
            .then((response) => response.json())
            .then((data) => {
                setRelationships(data.results);
            });
    }

    const shortId = `short-${termId}`;
    const longId = `long-${termId}`;

    const toggleDisplay = () => {
        setShowAll(!showAll);
    };

    function expandMessage() {
        if (totalCount > relationships.length) {
            return `Show ${relationships.length} of ${totalCount} Terms`;
        } else {
            return `Show All ${relationships.length} Terms`;
        }
    }

    return (
        relationships && (
            <div>
                <div id={shortId} style={{ display: showAll ? 'none' : 'inline' }}>
                    {relationships.slice(0, 5).map((term, index) => (
                        <span
                            key={index}
                            className='related-ontology-term'
                            id={term.termName}
                        >
                            <TermLink term={term} />
                        </span>
                    ))}

                    {relationships.length > 5 && (
                        <div className='mt-2'>
                            <a
                                href='#'
                                onClick={(e) => {
                                    e.preventDefault();
                                    toggleDisplay();
                                }}
                            >
                                ... <img src='/images/plus-symbol.png' alt='expand' />{' '}
                                {expandMessage()}
                            </a>
                        </div>
                    )}
                </div>

                <div id={longId} style={{ display: showAll ? 'inline' : 'none' }}>
                    {relationships.map((term, index) => (
                        <span
                            key={index}
                            className='related-ontology-term'
                            id={term.termName}
                        >
                            <TermLink term={term} />
                        </span>
                    ))}
                    <div className={'mt-2'}>
                        <a
                            href='#'
                            onClick={(e) => {
                                e.preventDefault();
                                toggleDisplay();
                            }}
                            className='text-sm mr-3'
                        >
                            <img src='/images/minus-symbol.png' alt='collapse' /> Show First 5
                            Terms
                        </a>
                        <a
                            href='#'
                            onClick={(e) => {
                                e.preventDefault();
                                expandCallback();
                            }}
                            className='text-sm'
                        >
                            <img src='/images/plus-symbol.png' alt='expand more' /> Show All
                            Terms
                        </a>
                    </div>
                </div>
            </div>
        )
    );
}
