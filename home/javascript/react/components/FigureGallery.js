import React, {useState} from 'react';
import PropTypes from 'prop-types';
import LoadingSpinner from './LoadingSpinner';
import FigureGalleryModal from './FigureGalleryModal';

let animationRequest = null;

const FigureGallery = ({className, images, loading, onLoadMore, total}) => {
    const [selectedIndex, setSelectedIndex] = useState(null);
    const canLoadMore = !loading && total && images.length < total;
    const maxIndex = total || images.length;

    const handleScroll = event => {
        if (animationRequest) {
            return;
        }

        const target = event.target;
        animationRequest = requestAnimationFrame(() => {
            animationRequest = null;
            if (!canLoadMore) {
                return;
            }
            if (target.scrollWidth - target.scrollLeft - target.clientWidth < 200) {
                onLoadMore();
            }
        });
    };

    const handleModalNext = selectedIndex === maxIndex - 1 ? undefined : () => {
        const nextIndex = selectedIndex + 1
        if (canLoadMore && nextIndex === images.length - 1) {
            onLoadMore();
        }
        setSelectedIndex(nextIndex);
    };

    const handleModalPrev = selectedIndex === 0 ? undefined : () => setSelectedIndex(selectedIndex - 1);

    return (
        <div className={className} onScroll={handleScroll}>
            {images.map((image, idx) => (
                <img
                    alt={image.zdbID}
                    className='figure-gallery-image'
                    key={image.zdbID}
                    onClick={() => setSelectedIndex(idx)}
                    src={image.mediumUrl}
                />
            ))}
            {loading && <LoadingSpinner />}
            {canLoadMore &&
                <button className='border-0 bg-transparent' onClick={onLoadMore}>
                    <i className='fas fa-chevron-right' />
                    More
                </button>
            }

            <FigureGalleryModal
                image={images[selectedIndex]}
                onClose={() => setSelectedIndex(null)}
                onNext={handleModalNext}
                onPrev={handleModalPrev}
            />
        </div>
    );
};

FigureGallery.propTypes = {
    className: PropTypes.string,
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
