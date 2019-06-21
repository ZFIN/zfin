import React from 'react';
import PropTypes from 'prop-types';

const LoadingButton = ({children, loading, ...rest}) => {
    return (
        <button disabled={loading} {...rest}>
            { loading ? (<span><i className="fas fa-spinner fa-spin" /></span>) : children }
        </button>
    );
};

LoadingButton.propTypes = {
    children: PropTypes.node,
    loading: PropTypes.bool,
};

export default LoadingButton;
