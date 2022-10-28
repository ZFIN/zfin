import React  from 'react';
import PropTypes from 'prop-types';
// import publicationStore from '../state/PublicationStore';
import { useSelector } from 'react-redux';

const selectNavigationCounts = state => state.navigationCounts;

const NavigationItem = ({ title, store }) => {
    // store.subscribe( () => { console.log("store state update from NI:", store.getState()); } );

    // const navigationCounts = useSelector(selectNavigationCounts);
    const navigationCounts = useSelector(selectNavigationCounts);
    const count = navigationCounts[title];
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
    store: PropTypes.object,
}

export default NavigationItem;
