import React, {useState}  from 'react';
import PropTypes from 'prop-types';

const NavigationItem = ({ title, navigationCounter }) => {
    const [count, setCount] = useState(null);
    navigationCounter.subscribe(title, newCount => setCount(newCount));

    if (count) {
        return (
            <><span className='badge badge-pill badge-secondary'>{count}</span></>
        );
    } else {
        return (<></>);
    }
};

NavigationItem.propTypes = {
    title: PropTypes.string,
    navigationCounter: PropTypes.object,
}

export default NavigationItem;
