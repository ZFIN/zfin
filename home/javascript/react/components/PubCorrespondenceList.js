import React from 'react';
import PropTypes from 'prop-types';
import PubCorrespondenceEntry from './PubCorrespondenceEntry';

const PubCorrespondenceList = ({correspondences, onResend, onRecordReply, onSendReply, onDelete}) => {
    if (!correspondences) {
        return null;
    }

    if (!correspondences.length) {
        return <div className='text-muted text-center'><hr />No correspondences yet</div>;
    }

    const preventDefault = (next) => {
        return (event) => {
            event.preventDefault();
            next();
        }
    };

    return (
        correspondences.map(correspondence => (
            <div key={correspondence.id}>
                <hr />
                {correspondence.resend ?
                    <div>
                        <strong>{new Date(correspondence.date).toLocaleDateString()}</strong>
                        <span className='text-muted'> Resent email from {new Date(correspondence.composedDate).toLocaleDateString()}</span>
                    </div> :
                    <div>
                        <div className='dropdown float-right'>
                            <button className='btn btn-outline-secondary' type='button' data-toggle='dropdown'>
                                <i className='fas fa-ellipsis-v' />
                            </button>
                            {correspondence.outgoing ?
                                <div className='dropdown-menu dropdown-menu-right'>
                                    <a className={`dropdown-item ${correspondence.to.length === 0 ? 'disabled' : ''}`} href='#' onClick={preventDefault(() => onResend(correspondence))}>
                                        Resend
                                    </a>
                                    <a className='dropdown-item' href='#' onClick={preventDefault(() => onRecordReply(correspondence))}>Record Reply</a>
                                    <a className='dropdown-item' href='#' onClick={preventDefault(() => onDelete(correspondence))}>Delete</a>
                                </div> :
                                <div className='dropdown-menu dropdown-menu-right'>
                                    <a className='dropdown-item' href='#' onClick={preventDefault(() => onSendReply(correspondence))}>Reply</a>
                                    <a className='dropdown-item' href='#' onClick={preventDefault(() => onDelete(correspondence))}>Delete</a>
                                </div>
                            }
                        </div>

                        <PubCorrespondenceEntry correspondence={correspondence} />
                    </div>
                }
            </div>
        ))
    );
};

PubCorrespondenceList.propTypes = {
    correspondences: PropTypes.array,
    onResend: PropTypes.func,
    onRecordReply: PropTypes.func,
    onSendReply: PropTypes.func,
    onDelete: PropTypes.func,
};

export default PubCorrespondenceList;
