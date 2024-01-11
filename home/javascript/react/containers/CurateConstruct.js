import React from 'react';
import PropTypes from 'prop-types';
import CurateConstructNew from './CurateConstructNew';
import CurateConstructEdit from './CurateConstructEdit';
import CurateConstructRelationships from './CurateConstructRelationships';

const CurateConstruct = ({publicationId}) => {
    return (
        <>
            <CurateConstructNew publicationId={publicationId}/>
            <CurateConstructEdit publicationId={publicationId}/>
            <CurateConstructRelationships publicationId={publicationId}/>
        </>
    );
};

CurateConstruct.propTypes = {
    publicationId: PropTypes.string,
};

export default CurateConstruct;
