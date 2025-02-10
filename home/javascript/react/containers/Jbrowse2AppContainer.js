import React, {useRef} from 'react';
import {JBrowseApp, createViewState} from '@jbrowse/react-app';
import assembly from '../constants/GRCz11_assembly.json';
import tracks from '../constants/GRCz11_tracks.json';

const assemblies = [{...assembly}];

const Jbrowse2AppContainer = () => {
    const containerRef = useRef(null);

    const config = {
        assemblies,
        assembly,
        tracks,
        defaultSession: {
            'drawerPosition': 'right',
            'drawerWidth': 384,
            'widgets': {
                'GridBookmark': {
                    'id': 'GridBookmark',
                    'type': 'GridBookmarkWidget'
                },
                'hierarchicalTrackSelector': {
                    'id': 'hierarchicalTrackSelector',
                    'type': 'HierarchicalTrackSelectorWidget',
                    'view': 'linearGenomeView',
                    'faceted': {
                        'filterText': '',
                        'showSparse': false,
                        'showFilters': true,
                        'showOptions': false,
                        'panelWidth': 400
                    }
                }
            },
            'activeWidgets': {
                'hierarchicalTrackSelector': 'hierarchicalTrackSelector'
            },
            'minimized': false,
            'id': 'GGAgRzryv3_h4xvQRVLa8',
            'name': 'New session',
            'margin': 0,
            'views': [
                {
                    'id': 'linearGenomeView',
                    'minimized': false,
                    'type': 'LinearGenomeView',
                    'offsetPx': 0,
                    'bpPerPx': 1,
                    'displayedRegions': [
                        {
                            'refName': '18',
                            'start': 20408298,
                            'end': 30612993,
                            'reversed': false,
                            'assemblyName': 'GRCz11'
                        }
                    ],
                    'tracks': [
                        {
                            'id': 'IGmtbEtO6W3gGz6jLZn8y',
                            'type': 'FeatureTrack',
                            'configuration': 'zfin_transcript',
                            'minimized': false,
                            'displays': [
                                {
                                    'id': 'wtd5ZK2nDTAny42aNSDIf',
                                    'type': 'LinearBasicDisplay',
                                    'configuration': 'transcript-1687907635485-LinearBasicDisplay'
                                }
                            ]
                        },
                        {
                            'id': 'mu5QIVRfi6Zrm0U8XcxxL',
                            'type': 'FeatureTrack',
                            'configuration': 'zfin_gene',
                            'minimized': false,
                            'displays': [
                                {
                                    'id': 'ER8CBH6YcjAgfyW0Ekmio',
                                    'type': 'LinearBasicDisplay',
                                    'configuration': 'zfin_gene-1687907419159-LinearBasicDisplay'
                                }
                            ]
                        },
                        {
                            'id': 'zhvG5StLbDH770pysPOB6',
                            'type': 'FeatureTrack',
                            'configuration': 'zfin_features',
                            'minimized': false,
                            'displays': [
                                {
                                    'id': 'kleS1dI-Q3JQ7HmDL2MV6',
                                    'type': 'LinearBasicDisplay',
                                    'configuration': 'zfin_features-1687908884139-LinearBasicDisplay'
                                }
                            ]
                        },
                    ],
                    'hideHeader': false,
                    'hideHeaderOverview': false,
                    'hideNoTracksActive': false,
                    'trackSelectorType': 'hierarchical',
                    'showCenterLine': false,
                    'showCytobandsSetting': true,
                    'trackLabels': 'overlapping',
                    'showGridlines': true,
                    'highlight': [],
                    'colorByCDS': false,
                    'showTrackOutlines': true,
                    'bookmarkHighlightsVisible': true,
                    'bookmarkLabelsVisible': true
                }
            ],
            'sessionTracks': [],
            'sessionAssemblies': [],
            'temporaryAssemblies': [],
            'connectionInstances': [],
            'sessionConnections': [],
            'focusedViewId': 'linearGenomeView',
            'sessionPlugins': []
        }
    };

    const state = new createViewState({config});

    return (
        <div className='position-relative'>
            <div ref={containerRef}>
                <JBrowseApp viewState={state}/>
            </div>
        </div>
    );
};


export default Jbrowse2AppContainer;
