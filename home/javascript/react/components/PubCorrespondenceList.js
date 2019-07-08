import React from 'react';
import PropTypes from 'prop-types';
import PubCorrespondenceEntry from "./PubCorrespondenceEntry";

const PubCorrespondenceList = ({correspondences, onResend, onRecordReply, onSendReply, onDelete}) => {
    if (!correspondences) {
        return null;
    }

    if (!correspondences.length) {
        return <div className="text-muted text-center"><hr />No correspondences yet</div>;
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
                        <span className="text-muted"> Resent email from {new Date(correspondence.composedDate).toLocaleDateString()}</span>
                    </div> :
                    <div>
                        <div className="dropdown pull-right">
                            <button className="btn btn-default dropdown-toggle" type="button" data-toggle="dropdown">
                                <i className="fas fa-ellipsis-v" />
                            </button>
                            {correspondence.outgoing ?
                                <ul className="dropdown-menu">
                                    <li className={correspondence.to.length === 0 ? 'disabled' : ''}>
                                        <a href='#' onClick={preventDefault(() => onResend(correspondence))}>Resend</a>
                                    </li>
                                    <li><a href='#' onClick={preventDefault(() => onRecordReply(correspondence))}>Record Reply</a></li>
                                    <li><a href='#' onClick={preventDefault(() => onDelete(correspondence))}>Delete</a></li>
                                </ul> :
                                <ul className="dropdown-menu">
                                    <li><a href='#' onClick={preventDefault(() => onSendReply(correspondence))}>Reply</a></li>
                                    <li><a href='#' onClick={preventDefault(() => onDelete(correspondence))}>Delete</a></li>
                                </ul>
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
