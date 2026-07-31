import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';

function timeCounter({initialTime = ''}) {
    const [count, setCount] = useState(0);

    useEffect(() => {
        const id = setInterval(() => setCount((oldCount) => oldCount + 1), 1000);

        return () => {
            clearInterval(id);
        };
    }, []);


    const timeArray = initialTime.split(':');
    let second = (1 * timeArray[2]);
    let minute = (1 * timeArray[1]);
    let hour = (1 * timeArray[0]);

    second = second + count;
    if (second > 59) {
        let minuteAdjustment = Math.floor(second / 60);
        second = second % 60;
        minute = minute + minuteAdjustment;
        let hourAdjustment = Math.floor(minute / 60);
        minute = minute % 60;
        hour = hour + hourAdjustment;
    }
    if (second < 10) {
        second = '0' + second;
    }
    if (minute < 10) {
        minute = '0' + minute;
    }
    if (hour < 10) {
        hour = '0' + hour;
    }

    let newTime = hour + ':' + minute + ':' + second
    return (
        <>
            <div>{newTime}</div>
        </>
    );
}

timeCounter.propTypes = {
    initialTime: PropTypes.string,
};

export default timeCounter;
