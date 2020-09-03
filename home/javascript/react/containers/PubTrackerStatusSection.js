import React from 'react';
import PropTypes from 'prop-types';

import {
    getCurators,
    getIndexed,
    getLocations,
    getStatus,
    getStatuses,
    updateIndexed,
    updateStatus,
    validate,
} from '../api/publication';
import intertab from '../utils/intertab';

import PubTrackerPanel from '../components/PubTrackerPanel';
import PubTrackerStatus from '../components/PubTrackerStatus';
import PubTrackerIndexed from '../components/PubTrackerIndexed';


class PubTrackerStatusSection extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            curatedEntities: [],
            notificationLoading: false,
            notes: [],
            status: {},
            statusLoading: false,
            indexed: null,
            indexedLoading: false,
            statuses: [],
            locations: [],
            curators: [],
            validationWarnings: [],
        };
        this.handleStatusSave = this.handleStatusSave.bind(this);
        this.handleCloseValidate = this.handleCloseValidate.bind(this);
        this.handleValidationCancel = this.handleValidationCancel.bind(this);
        this.handleIndexedToggle = this.handleIndexedToggle.bind(this);

    }

    componentDidMount() {
        const {pubId} = this.props;
        getStatuses().then(statuses => this.setState({statuses}));
        getLocations().then(locations => this.setState({locations}));
        getCurators().then(curators => this.setState({curators}));
        getStatus(pubId).then(status => this.setState({status}));
        getIndexed(pubId).then(indexed => this.setState({indexed}));
    }

    handleStatusSave(status, options = {}) {
        const {pubId} = this.props;
        this.setState({statusLoading: true});
        updateStatus(pubId, status, options).then(status => {
            this.setState({
                status,
                statusLoading: false,
                validationWarnings: [],
            });
            intertab.fireEvent(intertab.EVENTS.PUB_STATUS);

        });
    }

    handleCloseValidate(status) {
        this.setState({statusLoading: true});
        validate(this.props.pubId).then(validation => {
            if (validation.warnings.length > 0) {
                this.setState({
                    validationWarnings: validation.warnings,
                    statusLoading: false,
                });
            } else {
                this.handleStatusSave(status)
            }
        })
    }

    handleValidationCancel() {
        this.setState({
            validationWarnings: []
        });
    }

    handleIndexedToggle(indexed) {
        this.setState({indexedLoading: true});
        updateIndexed(this.props.pubId, indexed).then(indexed => this.setState({
            indexed,
            indexedLoading: false
        }));
    }



    render() {
        const {pubId, userId} = this.props;
        const {
            curators,
            indexed,
            indexedLoading,
            locations,
            status,
            statuses,
            statusLoading,
            validationWarnings
        } = this.state;

        const statusHeader = [
            'Status',
            <span className='float-right' key='history'>
                <small>
                    <a href={`/action/publication/${pubId}/status-history`} target='_blank' rel='noopener noreferrer'>
                        History <i className='fas fa-external-link-alt'/>
                    </a>
                </small>
            </span>
        ];

        return (
            <div>
                <PubTrackerPanel title={statusHeader}>
                    <div className='row clearfix'>
                        <div className='col-6 border-right'>
                            {curators.length > 0 && statuses.length > 0 && locations.length > 0 &&
                            <PubTrackerStatus
                                curators={curators}
                                defaultLocation={status.location}
                                defaultOwner={status.owner}
                                defaultStatus={status.status}
                                loading={statusLoading}
                                locations={locations}
                                onSave={this.handleStatusSave}
                                onValidate={this.handleCloseValidate}
                                onValidateCancel={this.handleValidationCancel}
                                statuses={statuses}
                                userId={userId}
                                warnings={validationWarnings}
                            />
                            }
                        </div>
                        <div className='col-5'>
                            <div className='row'>
                                <div className='offset-1 mt-2'>
                                    <PubTrackerIndexed
                                        indexed={indexed}
                                        onToggle={this.handleIndexedToggle}
                                        saving={indexedLoading}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                </PubTrackerPanel>


            </div>
        )
    }
}

PubTrackerStatusSection.propTypes = {
    pubDetails: PropTypes.object,
    pubId: PropTypes.string,
    userId: PropTypes.string,
    userName: PropTypes.string,
    userEmail: PropTypes.string,
};

export default PubTrackerStatusSection;
