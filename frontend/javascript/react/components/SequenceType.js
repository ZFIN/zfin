import React from 'react';
import PropTypes from 'prop-types';


const SequenceType = ({type, showPopup, markerID}) => {
    return (
        <span>
            {type}
            {(showPopup && type === 'Polypeptide') &&<a
                className='popup-link data-popup-link'
                href={`/action/marker/gene-product-description/${markerID}`}
            />}
        </span>
    );
};

SequenceType.propTypes = {
    type: PropTypes.string,
    showPopup: PropTypes.bool,
    markerID: PropTypes.string,
};
export default SequenceType;
