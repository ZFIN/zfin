import React from 'react';
import PropTypes from 'prop-types';
import LoadingSpinner from '../LoadingSpinner';

const PubTrackerIndexed = ({indexed, onToggle, saving}) => {
    const handleToggle = event => onToggle({
        indexed: event.target.checked,
    });

    if (!indexed) {
        return null;
    }
    
    return (
        <form>
            {!saving &&
                <div>
                    <label><input type='checkbox' checked={indexed.indexed} onChange={handleToggle} /> Indexed</label>
                    {indexed.indexed && <div>by {indexed.indexer.name} on {new Date(indexed.indexedDate).toLocaleDateString()}</div>}
                </div>
            }
            <LoadingSpinner loading={saving} />
        </form>
    );
};

PubTrackerIndexed.propTypes = {
    indexed: PropTypes.object,
    onToggle: PropTypes.func,
    saving: PropTypes.bool,
};

export default PubTrackerIndexed;