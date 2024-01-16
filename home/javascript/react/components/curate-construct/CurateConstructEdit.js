import React from 'react';
import PropTypes from 'prop-types';

const CurateConstructEdit = ({publicationId}) => {
    return <>
        <div className='display-none'>DEBUG: CurateConstructEdit for {publicationId}</div>
        <div className='mb-3'>
            <span className='bold'>EDIT CONSTRUCT: </span>
            <a style={{textDecoration: 'underline'}}>Show</a>
        </div>
    </>;
}

CurateConstructEdit.propTypes = {
    publicationId: PropTypes.string,
}

export default CurateConstructEdit;