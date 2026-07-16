import React from 'react';

const NUM_STAGES = 44;
const TICK_LABELS = [
    {label: '0 hpf', index: 0},
    {label: '2.00 hpf', index: 6},
    {label: '3.33 hpf', index: 11},
    {label: '5.66 hpf', index: 17},
    {label: '10.33 hpf', index: 22},
    {label: '24 hpf', index: 28},
    {label: '60 hpf', index: 33},
    {label: '7 dpf', index: 38},
    {label: '90 dpf', index: 43},
];

const StageTimelineHeader = () => {
    return (
        <div>
            <div>Stage Observed</div>
            <div className='stage-timeline-tick-container'>
                {TICK_LABELS.map(tick => {
                    const left = tick.index / NUM_STAGES * 100 + '%';
                    return (
                        <div key={tick.label} className='stage-timeline-tick-label' style={{left}}>
                            {tick.label}
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export { TICK_LABELS };
export default StageTimelineHeader;
