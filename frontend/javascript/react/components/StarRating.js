import React from 'react';
import PropTypes from 'prop-types';

const StarRating = ({rating}) => {
    return <span className={`stars stars-${rating}`} />;
};

StarRating.propTypes = {
    rating: PropTypes.number,
};

export default StarRating;
