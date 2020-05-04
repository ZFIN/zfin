import React from 'react';
import { fetchType } from '../utils/types';
import LoadingSpinner from './LoadingSpinner';
import CommaSeparatedList from './CommaSeparatedList';
// import StageLink from './StageLink';


const FigureGalleryPhenotypeDetails = ({figureDetails}) => {
    if (figureDetails.pending) {
        return <LoadingSpinner />
    }

    if (figureDetails.failed || !figureDetails.value) {
        return null;
    }

    const { expression } = figureDetails.value;

    return (
        <dl className='d-sm-table mt-2 mb-0'>
            {expression.fish.length > 0 &&
                <div className='d-sm-table-row'>
                    <dt className='d-sm-table-cell nowrap'>Fish</dt>
                    <dd className='my-sm-0 mx-sm-2'>
                        <CommaSeparatedList>
                            {expression.fish.map(fish => (
                                <a href={`/${fish.zdbID}`} dangerouslySetInnerHTML={{__html: fish.displayName}} key={fish.zdbID} />
                            ))}
                        </CommaSeparatedList>
                    </dd>
                </div>
            }

            {expression.experiments.length > 0 &&
                <div className='d-sm-table-row'>
                    <dt className='d-sm-table-cell nowrap'>Conditions</dt>
                    <dd className='my-sm-0 mx-sm-2'>
                        <CommaSeparatedList semicolon>
                            {expression.experiments.map(experiment => (
                                <a href={`/action/expression/experiment?id=${experiment.zdbID}`} key={experiment.zdbID}>
                                    {experiment.experimentConditions
                                        .map(condition => condition.displayName)
                                        .join(', ')
                                    }
                                </a>
                            ))}
                        </CommaSeparatedList>
                    </dd>
                </div>
            }

            {expression.sequenceTargetingReagents.length > 0 &&
                <div className='d-sm-table-row'>
                    <dt className='d-sm-table-cell nowrap'>Knockdown Reagents</dt>
                    <dd className='my-sm-0 mx-sm-2'>
                        <CommaSeparatedList>
                            {expression.sequenceTargetingReagents.map(str => (
                                <a href={`/${str.zdbID}`} key={str.zdbID}>{str.abbreviation}</a>
                            ))}
                        </CommaSeparatedList>
                    </dd>
                </div>
            }

            {expression.entities.length > 0 &&
                <div className='d-sm-table-row'>
                    <dt className='d-sm-table-cell nowrap'>Anatomical Terms</dt>
                    <dd className='my-sm-0 mx-sm-2'>
                        <CommaSeparatedList>
                            {expression.entities.map(entity => {
                                if (entity.subterm) {
                                    return (
                                        <a href={`/action/ontology/post-composed-term-detail?superTermID=${entity.superterm.oboID}&subTermID=${entity.subterm.oboID}`} key={`${entity.superterm.oboID}-${entity.subterm.oboID}`}>
                                            {entity.superterm.termName} {entity.subterm.termName}
                                        </a>
                                    );
                                } else {
                                    return (
                                        <a href={`/${entity.superterm.oboID}`} key={entity.superterm.oboID}>
                                            {entity.superterm.termName}
                                        </a>
                                    );
                                }
                            })}
                        </CommaSeparatedList>
                    </dd>
                </div>
            }

            {/*<div className='d-sm-table-row'>
                <dt className='d-sm-table-cell nowrap'>
                    {expression.startStage.zdbID === expression.endStage.zdbID ?
                        'Stage' :
                        'Stage Range'
                    }
                </dt>
                <dd className='my-sm-0 mx-sm-2'>
                    <StageLink stage={expression.startStage} /> {expression.startStage.zdbID !== expression.endStage.zdbID &&
                    <React.Fragment>
                        to <StageLink stage={expression.endStage} />
                    </React.Fragment>}
                </dd>
            </div>*/}



        </dl>
    )
};

FigureGalleryPhenotypeDetails.propTypes = {
    figureDetails: fetchType,
};

export default FigureGalleryPhenotypeDetails;
