import React, {useRef, useState, useEffect} from 'react';
import PropTypes from 'prop-types';
import NoData from '../components/NoData';

import configJbrowse from '../constants/config-jbrowse2.json';
import {createViewState, JBrowseLinearGenomeView} from '@jbrowse/react-linear-genome-view';

function getTrackConfigFromImageUrl(imageUrl, assembly) {
    const parsedImageUrl = new URL(imageUrl, window.location.href);
    const requestedTracks = parsedImageUrl.searchParams.get('tracks');
    const configuredTracks = [];
    
    // Use tracks from the local config file
    const tracks = configJbrowse.tracks || [];

    for (const trackName of requestedTracks.split(',')) {
        const track = tracks.find(t => t.name === trackName && t.assemblyNames && t.assemblyNames.includes(assembly.name));
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
    const [assembly, setAssembly] = useState(null);
    const [tracks, setTracks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadConfig = async () => {
            try {
                setLoading(true);
                
                // Load assembly and tracks from local config file
                if (configJbrowse.assemblies && configJbrowse.assemblies.length > 0) {
                    const assemblyConfig = configJbrowse.assemblies.find(a => a.name === build) || configJbrowse.assemblies[0];
                    setAssembly(assemblyConfig);
                } else {
                    throw new Error('No assembly configuration found in local config');
                }
                
                if (configJbrowse.tracks) {
                    setTracks(configJbrowse.tracks);
                } else {
                    // eslint-disable-next-line no-console
                    console.warn('No tracks found in config, using empty array');
                    setTracks([]);
                }
                
            } catch (err) {
                // eslint-disable-next-line no-console
                console.error('Error loading JBrowse config:', err);
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        loadConfig();
    }, []);

    if (!imageUrl) {
        return <NoData/>;
    }

    if (loading) {
        return <div className='alert alert-info'>Loading JBrowse configuration...</div>;
    }

    if (!assembly) {
        return <div className='alert alert-danger'>Failed to load JBrowse configuration: {error}</div>;
    }

    const configuredTracks = getTrackConfigFromImageUrl(imageUrl, assembly);

    const state = createViewState({
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
