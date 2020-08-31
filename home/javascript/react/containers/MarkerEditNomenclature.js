import React from 'react';
import PropTypes from 'prop-types';
import MarkerNameForm from '../components/MarkerNameForm';

const MarkerEditNomenclature = ({markerId}) => {
    return (
        <MarkerNameForm markerId={markerId} />
    )
};

MarkerEditNomenclature.propTypes = {
    markerId: PropTypes.string.isRequired,
};

export default MarkerEditNomenclature;
