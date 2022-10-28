import React, {useState}  from 'react';
import PropTypes from 'prop-types';

const NavigationItem = ({ title, navigationCountState }) => {
    const [count, setCount] = useState(null);
    navigationCountState.subscribe(title, newCount => setCount(newCount));

    if (count) {
        return (
            <>{title} <span className="badge badge-pill badge-secondary">{count}</span></>
        );
    } else {
        return (<>{title}</>);
    }
};

NavigationItem.propTypes = {
    title: PropTypes.string,
    navigationCountState: PropTypes.object,
}

export default NavigationItem;
