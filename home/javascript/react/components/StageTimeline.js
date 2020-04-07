import React, { useCallback } from 'react';
import PropTypes from 'prop-types';

const StageTimeline = ({allStages, highlightedStages}) => {
    // get the indices of the highlighted stages in the all stages array, remove
    // non-existent ones and sort numerically.
    const numStages = allStages.length;
    const allStageIds = allStages.map(stage => stage.zdbID);
    const indices = highlightedStages
        .map(stage => allStageIds.indexOf(stage.zdbID))
        .filter(idx => idx >= 0)
        .sort((a, b) => a - b);

    // transform the flat array of indices into an array of nested [start, end] arrays
    let start = indices[0];
    const ranges = [];
    for (let j = 0; j < indices.length; j++) {
        const curr = indices[j];
        const next = indices[j + 1];
        if (next > curr + 1 || next === undefined) {
            ranges.push([start, curr]);
            start = next;
        }
    }

    const handleRef = useCallback(ref => {
        $(ref).tipsy({gravity: 'n', html: true});
    });

    return (
        <div className='stage-timeline-container'>
            <div className='stage-timeline-line' />
            {
                ranges.map(([start, end]) => {
                    const left = start * 100 / numStages + '%';
                    const width = (end - start + 1) * 100 / numStages + '%';
                    const stageNames = [];
                    for (let idx = start; idx <= end; idx++) {
                        stageNames.push(allStages[idx].termName);
                    }
                    return (
                        <div
                            key={start}
                            className='stage-timeline-block'
                            ref={handleRef}
                            style={{left, width}}
                            title={stageNames.join('<br>')}
                        />
                    );
                })
            }
        </div>
    );
};

StageTimeline.propTypes = {
    allStages: PropTypes.array,
    highlightedStages: PropTypes.array,
};

export default StageTimeline;
