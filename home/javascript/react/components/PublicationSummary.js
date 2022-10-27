import React from 'react';
import PropTypes from 'prop-types';
import {publicationType} from '../utils/types';
import PublicationCitationLink from './PublicationCitationLink';

const PublicationSummary = ({ numberOfPublications, firstPublication}) => {

    const publicationDisplay = numberOfPublications === 1 ?
        <PublicationCitationLink publication={firstPublication}/> :
        `${numberOfPublications} publications`;

    return <>{publicationDisplay}</>;
};

PublicationSummary.propTypes = {
    numberOfPublications: PropTypes.number,
    firstPublication: publicationType,
}

export default PublicationSummary;
