import React from 'react';
import PropTypes from 'prop-types';
import CurateConstructNew from '../components/curate-construct/CurateConstructNew';
import CurateConstructEdit from '../components/curate-construct/CurateConstructEdit';
import CurateConstructRelationships from '../components/curate-construct/CurateConstructRelationships';


const CurateConstruct = ({publicationId}) => {
    return (
        <>
            {/*<CurateConstructNew publicationId={publicationId}/>*/}
            <CurateConstructEdit publicationId={publicationId}/>
            <CurateConstructRelationships publicationId={publicationId}/>
        </>
    );
};

CurateConstruct.propTypes = {
    publicationId: PropTypes.string,
};

export default CurateConstruct;

