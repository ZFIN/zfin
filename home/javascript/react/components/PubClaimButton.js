import React from 'react';
import PropTypes from 'prop-types';

const PubClaimButton = ({publication, onClaimPub}) => {
    return (
        <React.Fragment>
            <button className={`btn ${publication.claimed ? 'btn-success' : 'btn-default'}`}
                    disabled={publication.saving}
                    onClick={onClaimPub}
            >
                {publication.saving && <span><i className="fas fa-spinner fa-spin"/></span>}
                {!publication.saving && publication.claimed && <span><i className="fas fa-check"/> Claimed</span>}
                {!publication.saving && !publication.claimed && <span>Claim</span>}
            </button>
            {publication.claimError && <p className='text-danger'>{publication.claimError}</p>}
        </React.Fragment>
    );
};

PubClaimButton.propTypes = {
    publication: PropTypes.object.isRequired,
    onPubClaim: PropTypes.func,
};

export default PubClaimButton;
