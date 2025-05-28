import React, {useRef} from 'react';
import PropTypes from 'prop-types';
import NoData from '../components/NoData';

import assembly from '../constants/GRCz11_assembly.json';
import tracks from '../constants/GRCz11_tracks.json';
import {createViewState, JBrowseLinearGenomeView} from '@jbrowse/react-linear-genome-view';

const Jbrowse2Image = ({imageUrl, build, chromosome, landmark, color}) => {
    const containerRef = useRef(null);

    if (!imageUrl) {
        return <NoData/>;
    }

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
                tracks: [
                    {
                        type: 'FeatureTrack',
                        configuration: 'zfin_transcript',
                        displays: [
                            {
                                type: 'LinearBasicDisplay',
                                configuration: 'transcript-1687907635485-LinearBasicDisplay',
                            },
                        ],
                    },
                    {
                        type: 'FeatureTrack',
                        configuration: 'zfin_features',
                        displays: [
                            {
                                type: 'LinearBasicDisplay',
                                configuration: 'zfin_features-1687908884139-LinearBasicDisplay'
                            }
                        ]
                    },
                    {
                        type: 'FeatureTrack',
                        configuration: 'zfin_gene',
                        displays: [
                            {
                                type: 'LinearBasicDisplay',
                                configuration: 'zfin_gene-1687907419159-LinearBasicDisplay'
                            }
                        ]
                    },
                    {
                        type: 'FeatureTrack',
                        configuration: 'zfin_knockdown_reagent',
                        displays: [
                            {
                                type: 'LinearBasicDisplay',
                                configuration: 'knockdown_reagent-1687908500113-LinearBasicDisplay'
                            }
                        ]
                    }
                ],
            },
        },
    });

    return (
        <div className='position-relative'>
            <div ref={containerRef}>
                {build && <div><span className='gbrowse-source-label'>Genome Build: {build}</span><span className='gbrowse-source-label'>Chromosome: {chromosome}</span></div>}
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
