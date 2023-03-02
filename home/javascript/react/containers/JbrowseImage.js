import React, {useLayoutEffect, useRef, useState} from 'react';
import PropTypes from 'prop-types';
import NoData from '../components/NoData';

// when you request a gbrowse image of a specific width, the image that comes back is actually 90 pixels wider
const GBROWSE_PADDING = 90;
const IMAGE_SIZE_STEP = 200;
const DEBOUNCE_INTERVAL = 250;

const JbrowseImage = ({imageUrl, linkUrl, build, chromosome, height = '400'}) => {
    const [imgSrc, setImageSrc] = useState(null);
    const containerRef = useRef(null);
    const sep = imageUrl.indexOf('?') < 0 ? '?' : '&';

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
        return <NoData/>;
    }

    //<div ref={containerRef} style={imageLoaded ? undefined : hiddenStyle}>
    return (
        <div className='position-relative'>
            <div ref={containerRef}>
                {build && <div><span className='gbrowse-source-label'>Genome Build: {build}</span><span className='gbrowse-source-label'>Chromosome: {chromosome}</span></div>}
                <a href={linkUrl}>
                    <object
                        type='text/html'
                        width='1000'
                        height={height}
                        className='d-block mx-auto mb-3 pe-none'
                        data={imgSrc}
                        //onLoad={() => setImageLoaded(true)}
                    />
                </a>
            </div>
        </div>
    );
};

JbrowseImage.propTypes = {
    imageUrl: PropTypes.string.isRequired,
    linkUrl: PropTypes.string.isRequired,
    height: PropTypes.string,
    build: PropTypes.string,
    chromosome: PropTypes.string,
};

export default JbrowseImage;
