import React, {useState}  from 'react';
import PropTypes from 'prop-types';

const NavigationItem = ({ title, navigationCounter }) => {
    const [count, setCount] = useState(0);
    navigationCounter.subscribe(title, newCount => setCount(newCount));

    return (
        <><span className="badge">({count})</span></>
    );

};

NavigationItem.propTypes = {
    title: PropTypes.string,
    navigationCounter: PropTypes.object,
}

export default NavigationItem;
