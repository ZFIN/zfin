import React from 'react';
import PublicationCitationLink from './PublicationCitationLink';
import Link from './ExternalLinkMaybe';

const AttributionLink = ({accession, url, publicationCount, publication, multiPubAccessionID, multiPubs}) => {
    if (publicationCount === 0) {
        return <span><Link href={url}>{accession}</Link></span>;
    } else if (publicationCount === 1) {
        if (accession) {
            return (
                <span>
                    <Link href={url}>{accession}</Link> (<a href={`/${publication.zdbID}`}>{publicationCount}</a>)
                </span>
            );
        } else {
            return <PublicationCitationLink publication={publication} />;
        }
    } else {
        if (multiPubAccessionID) {
            return (
                <span>
                    <Link href={url}>{accession}</Link> (<a href={`/action/infrastructure/data-citation-list/${multiPubAccessionID}/${multiPubs}`}>{publicationCount}</a>)
                </span>
            );
        } else {
            return (
                <span>
                    <Link href={url}>{accession}</Link> (<a href={`/action/infrastructure/data-citation-list/${multiPubs}`}>{publicationCount}</a>)
                </span>
            );
        }
    }
};

export default AttributionLink;
