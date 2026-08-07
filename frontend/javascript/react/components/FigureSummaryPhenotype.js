import React from 'react';
import PropTypes from 'prop-types';
import CameraIcon from './CameraIcon';
import { publicationType } from '../utils/types';
import PublicationCitationLink from './PublicationCitationLink';

const FigureSummaryPhenotype = ({statistics, allFiguresUrl}) => {
    if (!statistics || statistics.numberOfFigs === 0) {
        return null;
    }

    const figureLink = statistics.numberOfFigs === 1 ?
        <a href={`/${statistics.figure.zdbID}`}>{statistics.figure.label}</a> :
        <a href={allFiguresUrl}>{statistics.numberOfFigs} figures</a>;

    const publicationDisplay = statistics.numberOfPubs === 1 ?
        <PublicationCitationLink publication={statistics.publication} /> :
        `${statistics.numberOfPubs} publications`;

    return <>{figureLink} {statistics.imgInFigure && <CameraIcon />} from {publicationDisplay}</>;
};

FigureSummaryPhenotype.propTypes = {
    allFiguresUrl: PropTypes.string,
    statistics: PropTypes.shape({
        numberOfPubs: PropTypes.number,
        numberOfFigs: PropTypes.number,
        imgInFigure: PropTypes.bool,
        figure: PropTypes.shape({
            zdbID: PropTypes.string,
            label: PropTypes.string,
        }),
        publication: publicationType,
    })
}

export default FigureSummaryPhenotype;
