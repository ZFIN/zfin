import React from 'react';
import PropTypes from 'prop-types';

const CurateConstructEdit = ({publicationId}) => {
    return <>
        <div className={`mb-3 pub-${publicationId}`}>
            <span className='bold'>EDIT CONSTRUCT: </span>
            <a style={{textDecoration: 'underline'}}>Show</a>
        </div>
    </>;
}

CurateConstructEdit.propTypes = {
    publicationId: PropTypes.string,
}

export default CurateConstructEdit;