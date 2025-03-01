import React from 'react';
import PropTypes from 'prop-types';
import CameraIcon from './CameraIcon';
import {publicationType} from '../utils/types';
import PublicationCitationLink from './PublicationCitationLink';
import CommaSeparatedList from './CommaSeparatedList';

const FigureSummary = ({statistics, fishID, markerID =('')}) => {
    if (!statistics || statistics.numberOfFigures === 0) {
        return null;
    }

    const publicationDisplay = statistics.numberOfPublications === 1 ?
        <PublicationCitationLink publication={statistics.firstPublication}/> :
        `${statistics.numberOfPublications} publications`;

    const urlPostfix = `?fishZdbID=${fishID}&expZdbID=${statistics.experiment.zdbID}&geneZdbID=${markerID}&imagesOnly=false`;

    const url = (statistics.experiment && statistics.experiment.standard) ?
        '/action/expression/fish-expression-figure-summary-standard' + urlPostfix :
        '/action/expression/fish-expression-figure-summary-experiment' + urlPostfix;

    if (statistics.numberOfFigures < 5) {
        return <CommaSeparatedList>
            {statistics.publications.map(publication => {
                return <>
                    <CommaSeparatedList>
                        {publication.figures.map(figure => {
                            return <><a href={`/${figure.zdbID}`}>{figure.label}</a>{figure.imgless !== true && <> <CameraIcon/></>}</>
                        })}
                    </CommaSeparatedList>
                    from <PublicationCitationLink publication={publication}/>
                </>
            })}
        </CommaSeparatedList>
    }

    const manyFigLink = <a href={url}>{statistics.numberOfFigures} figures </a>;
    return <>{manyFigLink}{statistics.imgInFigure && <CameraIcon/>} from {publicationDisplay}</>;

}

FigureSummary.propTypes = {
    fishID: PropTypes.string,
    markerID: PropTypes.string,
    statistics: PropTypes.shape({
        numberOfPublications: PropTypes.number,
        numberOfFigures: PropTypes.number,
        imgInFigure: PropTypes.bool,
        firstPublication: publicationType,
        experiment: PropTypes.object,
        antibody: PropTypes.object,
        publications: PropTypes.array,
    })
}

export default FigureSummary;
