import React from 'react';
import PropTypes from 'prop-types';
import {publicationType} from '../utils/types';
import PublicationCitationLink from './PublicationCitationLink';

const PublicationSummary = ({numberOfPublications, firstPublication, fishID, experimentID, termID}) => {

    const publicationDisplay = numberOfPublications === 1 ?
        <PublicationCitationLink publication={firstPublication}/> :
        <>
            {numberOfPublications} publications
            <a
                className='popup-link data-popup-link'
                href={`/action/ontology/disease-model-publication-popup?fishID=${fishID}&experimentID=${experimentID}&termID=${termID}`}
            />
        </>

    return <>{publicationDisplay}</>;
};

PublicationSummary.propTypes = {
    numberOfPublications: PropTypes.number,
    fishID: PropTypes.String,
    experimentID: PropTypes.String,
    termID: PropTypes.String,
    firstPublication: publicationType,
}

export default PublicationSummary;
