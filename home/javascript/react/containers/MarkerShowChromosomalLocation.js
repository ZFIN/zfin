import React, {useState} from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import AddEditList from '../components/AddEditList';
import LoadingSpinner from '../components/LoadingSpinner';

const MarkerShowChromosomalLocation = ({
    markerId,
}) => {
    const [error] = useState('');

    const {
        value: liveData,
        pending,
    } = useFetch(`/action/marker/${markerId}/chromosomal-location`);

    const formatLink = (item) => {
        const leftColumnClass = 'col-sm-6 col-md-5 col-lg-4';
        const rightColumnClass = 'col-sm-6 col-md-7 col-lg-8';

        return <>
            <dl className='row'>
                <dt className={leftColumnClass}>Chromosomal Location</dt>
                <dd className={rightColumnClass}><>Chr {item.chromosome}: </>
                    {' ' + item.startLocation.toLocaleString()} - {item.endLocation.toLocaleString() + ' '}
                    ({item.assembly})
                    {item.references && item.references.length && <span> ({
                        item.references.map( (reference, index) =>
                            <>{(index ? ', ' : '')}<a href={'/' + reference.zdbID}>{index + 1}</a></>
                        )
                    })</span> }
                </dd>
            </dl>
        </>;
    };

    if (pending) {
        return <LoadingSpinner/>;
    }

    if (!liveData) {
        return null;
    }

    return (
        <>
            {error && <div className='text-danger'>{error}</div>}
            <AddEditList
                formatItem={formatLink}
                itemKeyProp='zdbID'
                items={liveData}
                readOnly={true}
                blankOnEmpty={true}
            />
        </>
    );
};

MarkerShowChromosomalLocation.propTypes = {
    markerId: PropTypes.string,
    type: PropTypes.string,
};

export default MarkerShowChromosomalLocation;