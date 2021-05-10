import React, {Component} from 'react';
import PropTypes from 'prop-types';
import ObjectSelectBox from '../ObjectSelectBox';
import LoadingButton from '../LoadingButton';
import {isEmptyObject} from '../../utils';

const getId = (obj) => obj && obj.id;
const getZdbId = (obj) => obj && obj.zdbID;

const STATUS_LOCATIONS = {
    READY_FOR_INDEXING: {
        required: false,
        role: 'INDEXER',
        label: 'Priority',
    },
    MANUAL_PDF: {
        required: false,
        role: 'STUDENT',
        label: 'Priority',
    },
    READY_FOR_CURATION: {
        required: true,
        role: 'CURATOR',
        label: 'Location',
    },
};

const statusHasLocation = (status = {}) => STATUS_LOCATIONS[status.type] !== undefined;

const statusRequiresLocation = (status = {}) => statusHasLocation(status) && STATUS_LOCATIONS[status.type].required;

const getLocationRoleForStatus = (status = {}) => statusHasLocation(status) && STATUS_LOCATIONS[status.type].role;

const getLocationLabelForStatus = (status = {}) => statusHasLocation(status) ? STATUS_LOCATIONS[status.type].label : '';

const statusRequiresOwner = (status = {}) => {
    const { type } = status;
    return type === 'INDEXING' ||
        type === 'CURATING' ||
        type === 'WAIT' ||
        type === 'PROCESSING' ||
        type === 'MANUAL_PDF';
};

class PubTrackerStatus extends Component {
    constructor(props) {
        super(props);
        this.state = {
            status: {},
            location: {},
            owner: {},
            saved: false,
            resetTopics: false,
        };
        this.populateState = this.populateState.bind(this);
        this.handleSave = this.handleSave.bind(this);
        this.handleValidate = this.handleValidate.bind(this);
        this.handleValidateCancel = this.handleValidateCancel.bind(this);
    }

    componentDidMount() {
        this.populateState();
    }

    componentDidUpdate(prevProps, prevState) {
        const { curators, defaultLocation, defaultOwner, defaultStatus, userId } = this.props;
        const { status, location, owner } = this.state;

        // if we received a new status through props, populate the state fields with it
        if (getId(defaultStatus) !== getId(prevProps.defaultStatus) ||
            getId(defaultLocation)  !== getId(prevProps.defaultLocation) ||
            getZdbId(defaultOwner) !== getZdbId(prevProps.defaultOwner)) {
            this.populateState();
            if (prevProps.defaultStatus) {
                this.setState({saved: true});
            }
        }

        // if the user changed the status, set the owner accordingly and clear location if not a valid value
        if (!isEmptyObject(prevState.status) && getId(status) !== getId(prevState.status)) {
            if (status.type === 'CURATING' || status.type === 'INDEXING' || status.type === 'PROCESSING') {
                this.setState({owner: curators.find(curator => curator.zdbID === userId)});
            } else {
                this.setState({owner: defaultOwner});
            }

            if (location && location.role !== getLocationRoleForStatus(status)) {
                this.setState({location: null});
            }
        }

        // if any of the fields changes, clear the saved flag
        if (getId(status) !== getId(prevState.status) ||
            getId(location) !== getId(prevState.location) ||
            getZdbId(owner) !== getZdbId(prevState.owner)) {
            this.setState({saved: false});
        }
    }

    populateState() {
        const { defaultStatus, defaultOwner, defaultLocation } = this.props;
        this.setState({
            status: defaultStatus,
            location: defaultLocation,
            owner: defaultOwner,
        });
    }

    handleSave() {
        const { onSave } = this.props;
        const { status, location, owner, resetTopics } = this.state;
        this.setState({saved: false, resetTopics: false});
        onSave({
            status,
            location: (statusHasLocation(status) && getId(location)) ? location : null,
            owner: statusRequiresOwner(status) ? owner : null,
        }, {resetTopics});
    }

    handleValidate() {
        const { onValidate } = this.props;
        const { status } = this.state;
        this.setState({saved: false});
        onValidate({
            status,
            location: null,
            owner: null,
        });
    }

    handleValidateCancel() {
        this.props.onValidateCancel();
        this.populateState();
    }

    readyToSave() {
        const { defaultStatus, defaultLocation, defaultOwner } = this.props;
        const {status, location, owner} = this.state;

        const statusChanged = getId(defaultStatus) !== getId(status);
        const locationChanged = getId(defaultLocation) !== getId(location);
        const ownerChanged = getZdbId(defaultOwner) !== getZdbId(owner);
        const locationRequired = statusRequiresLocation(status);
        const ownerRequired = statusRequiresOwner(status);
        const statusSelected = getId(status);
        const locationSelected = getId(location);
        const ownerSelected = getZdbId(owner);

        return statusSelected && (
            (statusChanged && !locationRequired && !ownerRequired) ||
            (statusChanged && !locationRequired && ownerSelected) ||
            (statusChanged && !ownerRequired && locationSelected) ||
            (statusChanged && locationSelected && ownerSelected) ||
            (locationChanged && !locationRequired && !ownerRequired) ||
            (locationChanged && !locationRequired && ownerSelected) ||
            (locationChanged && !ownerRequired && locationSelected) ||
            (locationChanged && locationSelected && ownerSelected) ||
            (ownerChanged && !locationRequired && !ownerRequired) ||
            (ownerChanged && !locationRequired && ownerSelected) ||
            (ownerChanged && !ownerRequired && locationSelected) ||
            (ownerChanged && locationSelected && ownerSelected)
        );
    }

    isReopening() {
        const { defaultStatus } = this.props;
        const { status } = this.state;

        const statusChanged = getId(defaultStatus) !== getId(status);
        return statusChanged && defaultStatus.type === 'CLOSED' && status.type !== 'CLOSED';
    }

    render() {
        const { curators, defaultStatus, statuses, locations, loading, warnings } = this.props;
        const { status, location, owner, saved, resetTopics } = this.state;

        const statusOptions = [];
        if (!getId(defaultStatus)) {
            statusOptions.push({id: '', name: ''});
        }
        statusOptions.push(...statuses);

        const locationOptions = [];
        if (statusHasLocation(status)) {
            locationOptions.push({
                id: '',
                name: statusRequiresLocation(status) ? '' : 'Not Set',
            });
            locationOptions.push(...locations.filter(location => location.role === getLocationRoleForStatus(status)))
        }

        const curatorOptions = [{zdbID: '', name: ''}].concat(curators);

        return (
            <form className='form-horizontal'>
                <div className='form-group row'>
                    <label className='col-md-3 col-form-label'>Status</label>
                    <div className='col-md-8'>
                        <ObjectSelectBox
                            className='form-control'
                            getDisplay='name'
                            getValue='id'
                            options={statusOptions}
                            value={status}
                            onChange={status => this.setState({status})}
                        />
                    </div>
                </div>

                {statusHasLocation(status) &&
                <div className='form-group row'>
                    <label className='col-md-3 col-form-label'>{getLocationLabelForStatus(status)}</label>
                    <div className='col-md-8'>
                        <ObjectSelectBox
                            className='form-control'
                            getDisplay='name'
                            getValue='id'
                            options={locationOptions}
                            value={location}
                            onChange={location => this.setState({location})}
                        />
                    </div>
                </div>
                }

                {statusRequiresOwner(status) &&
                <div className='form-group row'>
                    <label className='col-md-3 col-form-label'>Owner</label>
                    <div className='col-md-8'>
                        <ObjectSelectBox
                            className='form-control'
                            getDisplay='name'
                            getValue='zdbID'
                            options={curatorOptions}
                            value={owner}
                            onChange={owner => this.setState({owner})}
                        />
                    </div>
                </div>
                }

                {this.isReopening() &&
                <div className='form-group row'>
                    <div className='col-md-8 offset-md-3'>
                        <label>
                            <input type='checkbox' value={resetTopics} onChange={event => this.setState({resetTopics: event.target.checked})} /> Reset topics
                        </label>
                    </div>
                </div>
                }

                {warnings.length === 0 &&
                <div className='form-group row'>
                    <div className='offset-md-3 col-md-9 horizontal-buttons'>
                        <button
                            type='button'
                            className='btn btn-outline-secondary'
                            disabled={loading || !this.readyToSave()}
                            onClick={this.populateState}
                        >
                            Reset
                        </button>

                        <LoadingButton
                            loading={loading}
                            type='button'
                            className='btn btn-primary'
                            disabled={loading || !this.readyToSave()}
                            onClick={status.type === 'CLOSED' ? this.handleValidate : this.handleSave}
                        >
                            Save
                        </LoadingButton>

                        {saved && <span className='text-success'><i className='fas fa-check'/> Saved</span>}
                    </div>
                </div>
                }

                {warnings.length > 0 &&
                <div className='offset-md-3 col-md-9 alert alert-warning' role='alert'>
                    <h4>Heads up!</h4>
                    <p className='bottom-buffer-sm'>You might not want to close this publication yet. Are you sure you
                        want to close it?</p>
                    <ul className='bottom-buffer'>
                        {warnings.map((warning, idx) => (
                            <li key={idx}>
                                <span dangerouslySetInnerHTML={{__html: warning}} />
                            </li>
                        ))}
                    </ul>
                    <p className='horizontal-buttons'>
                        <button type='button' className='btn btn-outline-secondary' disabled={loading} onClick={this.handleValidateCancel}>Cancel</button>
                        <LoadingButton loading={loading} type='button' className='btn btn-warning' disabled={loading} onClick={this.handleSave}>
                            Yes, close it
                        </LoadingButton>
                    </p>
                </div>
                }
            </form>
        );
    }
}

PubTrackerStatus.propTypes = {
    curators: PropTypes.array,
    defaultStatus: PropTypes.object,
    defaultLocation: PropTypes.object,
    defaultOwner: PropTypes.object,
    loading: PropTypes.bool,
    locations: PropTypes.array,
    onSave: PropTypes.func,
    onValidate: PropTypes.func,
    onValidateCancel: PropTypes.func,
    saved: PropTypes.bool,
    statuses: PropTypes.array,
    userId: PropTypes.string,
    warnings: PropTypes.array,
};

PubTrackerStatus.defaultProps = {
    defaultStatus: {},
    defaultLocation: {},
    defaultOwner: {},
};

export default PubTrackerStatus;