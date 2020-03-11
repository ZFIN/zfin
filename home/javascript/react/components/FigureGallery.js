import React from 'react';
import PropTypes from 'prop-types';
import LoadingSpinner from './LoadingSpinner';

const FigureGallery = ({images, loading, onLoadMore, total}) => {
    return (
        <div className='image-strip-container'>
            {images.map(image => (
                <img alt={image.zdbID} className='image-strip-image' key={image.zdbID} src={image.mediumUrl} />
            ))}
            {loading && <LoadingSpinner />}
            {total && !loading && images.length < total &&
                <button className='border-0 bg-transparent' onClick={onLoadMore}>
                    <i className='fas fa-chevron-right' />
                    More
                </button>
            }
        </div>
    );
};

FigureGallery.propTypes = {
    images: PropTypes.arrayOf(PropTypes.shape({
        zdbID: PropTypes.string,
        mediumUrl: PropTypes.string,
        url: PropTypes.string,
    })).isRequired,
    total: PropTypes.number,
    onLoadMore: PropTypes.func,
    loading: PropTypes.bool,
};

export default FigureGallery;
