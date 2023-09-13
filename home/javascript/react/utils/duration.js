import React from 'react';
import PropTypes from 'prop-types';
import TimeCounter from './time-counter';

function duration({duration = null, currentDuration = null, isRunning = false}) {
    let display
    if (!isRunning && duration) {
        display = duration.toLocaleString();
    }

    if (!isRunning && currentDuration) {
        display ='aborted'
    }

    if(isRunning && currentDuration){
        return <TimeCounter initialTime={currentDuration}/>
    }

    return (
        <>
            <div>{display}</div>
        </>
    );
}

duration.propTypes = {
    duration: PropTypes.string,
    currentDuration: PropTypes.string,
    isRunning: PropTypes.bool,
};

export default duration;
