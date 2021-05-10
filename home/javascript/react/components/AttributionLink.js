import React from 'react';
import PublicationCitationLink from './PublicationCitationLink';

const AttributionLink = ({accession, url, publicationCount, publication, multiPubAccessionID, multiPubs}) => {
    if (publicationCount === 0) {
        return <span><a href={url}>{accession}</a></span>;
    } else if (publicationCount === 1) {
        if (accession) {
            return (
                <span>
                    <a href={url}>{accession}</a> (<a href={`/${publication.zdbID}`}>{publicationCount}</a>)
                </span>
            );
        } else {
            return <PublicationCitationLink publication={publication} />;
        }
    } else {
        if (multiPubAccessionID) {
            return (
                <span>
                    <a href={url}>{accession}</a> (<a href={`/action/infrastructure/data-citation-list/${multiPubAccessionID}/${multiPubs}`}>{publicationCount}</a>)
                </span>
            );
        } else {
            return (
                <span>
                    <a href={url}>{accession}</a> (<a href={`/action/infrastructure/data-citation-list/${multiPubs}`}>{publicationCount}</a>)
                </span>
            );
        }
    }
};

export default AttributionLink;
