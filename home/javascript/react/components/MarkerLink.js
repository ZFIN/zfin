import React from 'react';
import PropTypes from 'prop-types';
import MarkerAbbreviation from './MarkerAbbreviation';

const MarkerLink = ({marker}) => {
    return (
        <a href={`/${marker.zdbID}`}>
            <MarkerAbbreviation marker={marker} />
        </a>
    );
};

MarkerLink.propTypes = {
    marker: PropTypes.shape({
        zdbID: PropTypes.string.isRequired,
        abbreviation: PropTypes.string.isRequired,
    })
};

export default MarkerLink;
