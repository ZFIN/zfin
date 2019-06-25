import React from 'react';
import PropTypes from 'prop-types';
import TimeAgo from 'react-timeago';

const formatter = (value, unit) => `${value} ${unit}${value !== 1 ? 's': ''}`;

const RelativeDate = ({ago, date}) => (
    <span>
        <TimeAgo date={date} formatter={formatter} minPeriod={10} /> {ago ? 'ago' : ''}
    </span>
);

RelativeDate.propTypes = {
    ago: PropTypes.bool,
    date: PropTypes.oneOfType([PropTypes.number, PropTypes.string, PropTypes.instanceOf(Date)])
};

RelativeDate.defaultProps = {
    ago: false
};

export default RelativeDate;