import React from 'react';
import PropTypes from 'prop-types';


const SequenceType = ({type, value, markerID}) => {
    if (value) {
        if (type.indexOf('Polypeptide') === 0) {
            return (
                <span>
                    {type}
                    <a
                        className='popup-link data-popup-link'
                        href={`/action/marker/gene-product-description/${markerID}`}
                    />
                </span>
            );
        }
        else {
            return type;
        }
    }
    if (!value) {
        return type;
    }
};

SequenceType.propTypes = {
    type: PropTypes.string,
    value: PropTypes.bool,
    markerID: PropTypes.string,
};
export default SequenceType;
