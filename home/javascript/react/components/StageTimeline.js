import React, { useCallback } from 'react';
import PropTypes from 'prop-types';
import {TICK_LABELS} from './StageTimelineHeader';

const formatHours = (hours) => {
    if (hours < 168) {
        return hours + ' hpf';
    } else {
        return hours / 24 + ' dpf';
    }
};

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
        $(ref).tipsy({
            gravity: 'n',
            html: true,
            className: 'stage-timeline-tooltip',
        });
    }, []);

    return (
        <div className='stage-timeline-container'>
            <div className='stage-timeline-line' />
            {
                TICK_LABELS.map(tick => {
                    const left = tick.index * 100 / numStages + '%';
                    return (
                        <div
                            className='stage-timeline-tick'
                            key={tick.label}
                            style={{left}}
                        />
                    );
                })
            }
            {
                ranges.map(([start, end]) => {
                    const left = start * 100 / numStages + '%';
                    const width = (end - start + 1) * 100 / numStages + '%';
                    const stageNames = [];
                    for (let idx = start; idx <= end; idx++) {
                        const stage = allStages[idx];
                        stageNames.push(`${stage.name} (${formatHours(stage.hoursStart)} - ${formatHours(stage.hoursEnd)})`);
                    }
                    return (
                        <div
                            key={start}
                            className='stage-timeline-block'
                            ref={handleRef}
                            style={{left, width}}
                            title={`<div class='text-left'>${stageNames.join('<br>')}</div>`}
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
