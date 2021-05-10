import React from 'react';
import PropTypes from 'prop-types';

const LoadingCount = ({count, loading}) => <span>{loading ? '▒' : count}</span>;

LoadingCount.propTypes = {
    count: PropTypes.number,
    loading: PropTypes.bool.isRequired,
};

export default LoadingCount;
