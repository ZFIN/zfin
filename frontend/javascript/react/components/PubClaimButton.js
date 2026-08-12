import React from 'react';
import PropTypes from 'prop-types';
import LoadingButton from './LoadingButton';

const PubClaimButton = ({publication, onClaimPub}) => {
    return (
        <React.Fragment>
            <LoadingButton
                loading={publication.saving || false}
                className={`btn ${publication.claimed ? 'btn-success' : 'btn-outline-secondary'}`}
                disabled={publication.saving}
                onClick={onClaimPub}
            >
                {publication.claimed && <span><i className='fas fa-check'/> Claimed</span>}
                {!publication.claimed && <span>Claim</span>}
            </LoadingButton>
            {publication.claimError && <p className='text-danger'>{publication.claimError}</p>}
        </React.Fragment>
    );
};

PubClaimButton.propTypes = {
    publication: PropTypes.object.isRequired,
    onClaimPub: PropTypes.func,
};

export default PubClaimButton;
