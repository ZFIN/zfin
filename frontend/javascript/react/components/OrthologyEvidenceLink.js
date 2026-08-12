import React from 'react';

const OrthologyEvidenceLink = ({name, code, publications, orthoID}) => {
    if (publications.length === 1) {
        return <span>{name} (<a href={`/${publications[0]}`}>1</a>) </span>;
    } else {
        return (
            <span>
                {name} (
                <a href={`/action/ortholog/${orthoID}/citation-list?evidenceCode=${code}`}>{publications.length}</a>)
            </span>
        );
    }
};

export default OrthologyEvidenceLink;
