import React from 'react';
import PropTypes from 'prop-types';

interface CurateConstructRelationshipsProps {
    publicationId: string;
}

const CurateConstructRelationships = ({publicationId}: CurateConstructRelationshipsProps) => {
    return <>
        <div className={`mb-3 pub-${publicationId}`}>
            <span className='bold'>CONSTRUCT RELATIONSHIPS: </span>
            <a style={{textDecoration: 'underline'}}>Show</a>
        </div>
    </>;
}

export default CurateConstructRelationships;