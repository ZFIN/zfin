import React from 'react';
import PropTypes from 'prop-types';

const CurateConstructRelationships = ({publicationId}) => {
    return <>
        <div className={`mb-3 pub-${publicationId}`}>
            <span className='bold'>CONSTRUCT RELATIONSHIPS: </span>
            <a style={{textDecoration: 'underline'}}>Show</a>
        </div>
    </>;
}

CurateConstructRelationships.propTypes = {
    publicationId: PropTypes.string,
}

export default CurateConstructRelationships;