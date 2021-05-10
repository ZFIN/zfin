import React, { useState, useRef, useLayoutEffect } from 'react';
import PropTypes from 'prop-types';
import NoData from '../components/NoData';

// when you request a gbrowse image of a specific width, the image that comes back is actually 90 pixels wider
const GBROWSE_PADDING = 90;
const IMAGE_SIZE_STEP = 200;
const DEBOUNCE_INTERVAL = 250;

const GbrowseImage = ({imageUrl, linkUrl, build}) => {
    const [imageLoaded, setImageLoaded] = useState(false);
    const [imgSrc, setImageSrc] = useState(null);
    const containerRef = useRef(null);
    const sep = imageUrl.indexOf('?') < 0 ? '?' : '&';
    const hiddenStyle = {
        position: 'absolute',
        height: 0,
        width: '100%',
        overflow: 'hidden',
    };

    // useLayoutEffect instead of useEffect since we're going to measure elements inside
    useLayoutEffect(() => {
        if (!containerRef.current) {
            return;
        }

        const doUpdate = () => {
            const containerWidth = containerRef.current.clientWidth;
            const imgWidth = Math.max(Math.floor(containerWidth / IMAGE_SIZE_STEP) * IMAGE_SIZE_STEP, IMAGE_SIZE_STEP);
            setImageSrc(`${imageUrl}${sep}width=${imgWidth - GBROWSE_PADDING}`);
        };
        let timer;
        const debouncedUpdate = () => {
            clearTimeout(timer);
            timer = setTimeout(doUpdate, DEBOUNCE_INTERVAL);
        };

        doUpdate();

        window.addEventListener('resize', debouncedUpdate);
        return () => window.removeEventListener('resize', debouncedUpdate);
    }, []);

    if (!imageUrl) {
        return <NoData />;
    }

    return (
        <div className='position-relative'>
            <div ref={containerRef} style={imageLoaded ? undefined : hiddenStyle}>
                {build && <span className='gbrowse-source-label'>Genome Build: {build}</span>}
                <a href={linkUrl}>
                    <img
                        className='d-block mx-auto mb-3'
                        src={imgSrc}
                        onLoad={() => setImageLoaded(true)}
                    />
                </a>
            </div>
        </div>
    );
};

GbrowseImage.propTypes = {
    imageUrl: PropTypes.string.isRequired,
    linkUrl: PropTypes.string.isRequired,
    build: PropTypes.string,
};

export default GbrowseImage;
