import React from 'react';
import PropTypes from 'prop-types';
import TimeAgo from 'react-timeago';

const formatter = (value, unit) => `${value} ${unit}${value !== 1 ? 's': ''}`;

const RelativeDate = ({date}) => <TimeAgo date={date} formatter={formatter} />;

RelativeDate.propTypes = {
    date: PropTypes.oneOfType([PropTypes.number, PropTypes.string, PropTypes.instanceOf(Date)])
};

export default RelativeDate;