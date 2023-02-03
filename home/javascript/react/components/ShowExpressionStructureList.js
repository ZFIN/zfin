import React, {useState} from 'react';
import PropTypes from 'prop-types';
import CommaSeparatedList from './CommaSeparatedList';
import {EntityLink} from './entity';

const ShowExpressionStructureList = ({expressionTerms}) => {

    const [truncateElement, setTruncateElement] = useState(true);

    const truncatedExpressionList = expressionTerms.slice(0, 5);

    const emptyUrl = '';
    if (truncateElement) {
        return <>
            <CommaSeparatedList>
                {truncatedExpressionList.map(entity => {
                    return <>
                        <EntityLink entity={entity}/>
                        <a
                            className='popup-link data-popup-link'
                            href={`/action/ontology/term-detail-popup?termID=${entity.oboID}`}
                        />
                    </>
                })}
            </CommaSeparatedList>
            <>
                {expressionTerms.length > 5 && (
                    <a href={emptyUrl} onClick={() => setTruncateElement(false)}>(all {expressionTerms.length})</a>
                )}
            </>
        </>
    }
    if (!truncateElement) {
        return <>
            <CommaSeparatedList>
                {expressionTerms.map(entity => {
                    return <>
                        <EntityLink entity={entity}/>
                        <a
                            className='popup-link data-popup-link'
                            href={`/action/ontology/term-detail-popup?termID=${entity.oboID}`}
                        />
                    </>
                })}
            </CommaSeparatedList>
            <a href={emptyUrl} onClick={() => setTruncateElement(true)}>(first 5)</a>
        </>
    }
};

ShowExpressionStructureList.propTypes = {
    expressionTerms: PropTypes.array,
};

export default ShowExpressionStructureList;
