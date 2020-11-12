import React from 'react';
import PropTypes from 'prop-types';
import CameraIcon from './CameraIcon';
import { publicationType } from '../utils/types';
import PublicationCitationLink from './PublicationCitationLink';

const FigureSummary = ({statistics, allFiguresUrl}) => {
    if (!statistics || statistics.numberOfFigures === 0) {
        return null;
    }

    const figureLink = statistics.numberOfFigures === 1 ?
        <a href={`/${statistics.firstFigure.zdbID}`}>{statistics.firstFigure.label}</a> :
        <a href={allFiguresUrl}>{statistics.numberOfFigures} figures</a>;

    const publicationDisplay = statistics.numberOfPublications === 1 ?
        <PublicationCitationLink publication={statistics.firstPublication} /> :
        `${statistics.numberOfPublications} publications`;

    return <>{figureLink} {statistics.imgInFigure && <CameraIcon />} from {publicationDisplay}</>;
};

FigureSummary.propTypes = {
    allFiguresUrl: PropTypes.string,
    statistics: PropTypes.shape({
        numberOfPublications: PropTypes.number,
        numberOfFigures: PropTypes.number,
        imgInFigure: PropTypes.bool,
        firstFigure: PropTypes.shape({
            zdbID: PropTypes.string,
            label: PropTypes.string,
        }),
        firstPublication: publicationType,
    })
}

export default FigureSummary;
