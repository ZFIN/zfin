import React, {useLayoutEffect, useRef, useState} from 'react';
import PropTypes from 'prop-types';
import NoData from '../components/NoData';

const IMAGE_SIZE_STEP = 100;
const IMAGE_MAX_WIDTH = 1600;
const IMAGE_MIN_WIDTH = 300;
const DEBOUNCE_INTERVAL = 250;

const JbrowseImage = ({imageUrl, linkUrl, build, chromosome, height = '400'}) => {
    const [width, setWidth] = useState('1000');
    const containerRef = useRef(null);

    // useLayoutEffect instead of useEffect since we're going to measure elements inside
    useLayoutEffect(() => {
        if (!containerRef.current) {
            return;
        }

        const doUpdate = () => {
            const containerWidth = Math.min(containerRef.current.clientWidth, window.innerWidth);
            const imgWidth = Math.floor(containerWidth / IMAGE_SIZE_STEP) * IMAGE_SIZE_STEP;

            //constrain to within min and max
            setWidth(Math.min(IMAGE_MAX_WIDTH, Math.max(IMAGE_MIN_WIDTH, imgWidth)));
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

    return (
        <div className='position-relative'>
            <div ref={containerRef}>
                {build && <div><span className='gbrowse-source-label'>Genome Assembly: {build}</span><span className='gbrowse-source-label'>Chromosome: {chromosome}</span></div>}
                <a href={linkUrl}>
                    <object
                        type='text/html'
                        width={width}
                        height={height}
                        className='d-block mx-auto mb-3 pe-none'
                        data={imageUrl}
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
