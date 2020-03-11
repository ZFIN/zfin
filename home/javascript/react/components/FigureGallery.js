import React from 'react';
import PropTypes from 'prop-types';
import LoadingSpinner from './LoadingSpinner';

let animationRequest = null;

const FigureGallery = ({images, loading, onLoadMore, total}) => {
    const handleScroll = event => {
        if (animationRequest) {
            console.log('cancelled');
            return;
        }

        const target = event.target;
        animationRequest = requestAnimationFrame(() => {
            animationRequest = null;
            if (loading || images.length === total) {
                return;
            }
            if (target.scrollWidth - target.scrollLeft - target.clientWidth < 200) {
                onLoadMore();
            }
        });
    };

    return (
        <div className='image-strip-container' onScroll={handleScroll}>
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
