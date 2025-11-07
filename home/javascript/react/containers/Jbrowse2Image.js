import React, {useRef} from 'react';
import PropTypes from 'prop-types';
import NoData from '../components/NoData';

import assembly from '../constants/GRCz11_assembly.json';
import tracks from '../constants/GRCz11_tracks.json';
import {createViewState, JBrowseLinearGenomeView} from '@jbrowse/react-linear-genome-view';

function getTrackConfigFromImageUrl(imageUrl) {
    const parsedImageUrl = new URL(imageUrl, window.location.href);
    const requestedTracks = parsedImageUrl.searchParams.get('tracks');
    const configuredTracks = [];
    for (const trackName of requestedTracks.split(',')) {
        const track = tracks.find(t => t.name === trackName);
        if (track) {
            const configuredTrack = {
                type: 'FeatureTrack',
                configuration: track.trackId,
                displays: [
                    {
                        type: 'LinearBasicDisplay',
                        configuration: track.displays.filter(d => d.type === 'LinearBasicDisplay')[0].displayId,
                    }
                ]
            }
            configuredTracks.push(configuredTrack);
        }
    }
    return configuredTracks;
}

const Jbrowse2Image = ({imageUrl, build, chromosome, landmark, color}) => {
    const containerRef = useRef(null);

    if (!imageUrl) {
        return <NoData/>;
    }

    const configuredTracks = getTrackConfigFromImageUrl(imageUrl);

    const state = new createViewState({
        assembly,
        tracks,
        location: landmark,
        configuration: {
            theme: {
                palette: {
                    primary: {
                        main: '#311b92',
                    },
                    secondary: {
                        main: color,
                    },
                    tertiary: {
                        main: '#f57c00',
                    },
                    quaternary: {
                        main: '#d50000',
                    }
                }
            }
        },
        defaultSession: {
            name: 'zfin embedded session',
            view: {
                type: 'LinearGenomeView',
                tracks: configuredTracks,
            },
        },
    });

    return (
        <div className='position-relative'>
            <div ref={containerRef}>
                {build && <div><span className='gbrowse-source-label'>Genome Assembly: {build}</span><span className='gbrowse-source-label'>Chromosome: {chromosome}</span></div>}
                <JBrowseLinearGenomeView viewState={state} />
            </div>
        </div>
    );
};

Jbrowse2Image.propTypes = {
    imageUrl: PropTypes.string.isRequired,
    linkUrl: PropTypes.string.isRequired,
    height: PropTypes.string,
    build: PropTypes.string,
    chromosome: PropTypes.string,
    landmark: PropTypes.string,
    color: PropTypes.string,
};

export default Jbrowse2Image;
