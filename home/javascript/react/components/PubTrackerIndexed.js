import React from 'react';
import PropTypes from 'prop-types';

const PubTrackerIndexed = ({error, indexed, onToggle, saving}) => {
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
                    <label><input type="checkbox" checked={indexed.indexed} onChange={handleToggle} /> Indexed</label>
                    {indexed.indexed && <div>by {indexed.indexer.name} on {new Date(indexed.indexedDate).toLocaleDateString()}</div>}
                </div>
            }
            {saving && <span><i className="fas fa-spinner fa-spin" /></span>}
        </form>
    );
};

PubTrackerIndexed.propTypes = {
    indexed: PropTypes.object,
    onToggle: PropTypes.func,
    saving: PropTypes.bool,
};

export default PubTrackerIndexed;