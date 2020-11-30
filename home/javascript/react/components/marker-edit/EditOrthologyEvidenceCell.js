import React from 'react';
import PropTypes from 'prop-types';
import { publicationType } from '../../utils/types';

const ORTHO_CURATION_PUB = 'ZDB-PUB-030905-1';

function zdbIdToDate(id) {
    const parts = id.split('-');
    const date = parts[2];
    if (!date) {
        return new Date(0);
    }
    // sorry, zfinners in the year 2090 :(
    const century = (date.substr(0, 1) === '9') ? '19' : '20';
    return new Date(century + date.substring(0, 2), Number(date.substring(2, 4)) - 1, date.substring(4, 6));
}

function comparePubs (a, b) {
    const aPubId = a[0];
    const bPubId = b[0];
    if (aPubId === ORTHO_CURATION_PUB) {
        return -1;
    }
    if (bPubId === ORTHO_CURATION_PUB) {
        return 1;
    }

    // ...the rest by zdbID
    const aDate = zdbIdToDate(aPubId);
    const bDate = zdbIdToDate(bPubId);
    return bDate.getTime() - aDate.getTime();
}

const EditOrthologyEvidenceCell = ({evidenceSet}) => {
    if (!evidenceSet) {
        return null;
    }

    const evidenceGroupedByPub = {};
    evidenceSet.forEach(evidence => {
        if (!evidenceGroupedByPub.hasOwnProperty(evidence.publication.zdbID)) {
            evidenceGroupedByPub[evidence.publication.zdbID] = [];
        }
        evidenceGroupedByPub[evidence.publication.zdbID].push(evidence.evidenceCode);
    });

    return (
        <ul className='list-unstyled'>
            {Object.entries(evidenceGroupedByPub)
                .sort(comparePubs)
                .map(([pubId, codes]) => (
                    <li key={pubId}><a href={'/' + pubId}>
                        {pubId === ORTHO_CURATION_PUB ? 'Ortho Curation Pub' : pubId}</a>: {codes.sort().join(', ')}
                    </li>
                ))
            }
        </ul>
    );
};

EditOrthologyEvidenceCell.propTypes = {
    evidenceSet: PropTypes.arrayOf(PropTypes.shape({
        evidenceCode: PropTypes.string,
        evidenceName: PropTypes.string,
        publication: publicationType,
    }))
};

export default EditOrthologyEvidenceCell;
