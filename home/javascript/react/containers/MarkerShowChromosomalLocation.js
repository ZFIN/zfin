import React, {useState} from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import LoadingSpinner from '../components/LoadingSpinner';

const MarkerShowChromosomalLocation = ({
    markerId,
}) => {
    const [error] = useState('');
    const {
        value: liveData,
        pending,
    } = useFetch(`/action/marker/${markerId}/chromosomal-location`);

    if (pending) {
        return <LoadingSpinner/>;
    }

    if (!liveData) {
        return null;
    }

    const formatChromosomalLocationItem = (item) => {
        const leftColumnClass = 'col-sm-6 col-md-5 col-lg-4';
        const rightColumnClass = 'col-sm-6 col-md-7 col-lg-8';

        return <dl className='row'>
            <dt className={leftColumnClass}>Location</dt>
            <dd className={rightColumnClass}><>Chr {item.chromosome}: </>
                {' ' + item.startLocation.toLocaleString()} - {item.endLocation.toLocaleString() + ' '}
                ({item.assembly})
                {item.references && item.references.length && <span> {' '}
                    (<a href={'/action/infrastructure/data-citation-list/' + markerId + '/'
                                + item.references.map( reference => reference.zdbID).join(',') }
                    >
                        {item.references.length}</a>)
                </span> }
            </dd>
        </dl>;
    };

    return (
        <>
            {error && <div className='text-danger'>{error}</div>}
            <ul className='list-unstyled'>
                {liveData.map(item => {
                    return (
                        <li key={item.zdbID}>
                            {formatChromosomalLocationItem(item)}
                        </li>
                    );
                })}
            </ul>
        </>
    );
};

MarkerShowChromosomalLocation.propTypes = {
    markerId: PropTypes.string,
    type: PropTypes.string,
};

export default MarkerShowChromosomalLocation;