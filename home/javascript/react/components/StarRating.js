import React from 'react';
import PropTypes from 'prop-types';

const StarRating = ({rating, max = 5}) => {
    return (
        <span style={{color: 'goldenrod'}}>
            {
                Array.apply(null, {length: max}).map((_, idx) => (
                    <i className={`${idx < rating ? 'fas' : 'far'} fa-star`} key={idx} />
                ))
            }
        </span>
    );
};

StarRating.propTypes = {
    rating: PropTypes.number,
    max: PropTypes.number,
};

export default StarRating;
