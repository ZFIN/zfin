import React from 'react';
import PropTypes from 'prop-types';
import {
    getCurators,
    getLocations,
    getStatus,
    getStatuses,
    updateStatus,
    getNotes,
    getTopics,
    validate,
    getIndexed,
    updateIndexed
} from "../api/publication";
import PubTrackerPanel from "../components/PubTrackerPanel";
import PubTrackerStatus from "../components/PubTrackerStatus";
import PubTrackerIndexed from "../components/PubTrackerIndexed";

class PubTracker extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            notes: [],
            topics: [],
            status: null,
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
        getNotes(pubId).then(notes => this.setState({notes}));
        getTopics(pubId).then(topics => this.setState({topics}));
    }

    handleStatusSave(status) {
        this.setState({statusLoading: true});
        updateStatus(this.props.pubId, status).then(status => this.setState({
            status,
            statusLoading: false,
            validationWarnings: [],
        }));
        // if closing, refetch topics
    }

    handleCloseValidate() {
        this.setState({statusLoading: true});
        validate(this.props.pubId).then(validation => {
            if (validation.warnings.length > 0) {
                this.setState({
                    validationWarnings: validation.warnings,
                    statusLoading: false,
                });
            } else {
                this.handleStatusSave()
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
        const { pubId, userId } = this.props;
        const { curators, indexed, indexedLoading, locations, status, statusLoading, statuses, validationWarnings } = this.state;

        const statusHeader = [
            'Status',
            <span className="pull-right" key='history'>
                <small>
                    <a href={`/action/publication/${pubId}/status-history`} target="_blank">
                        History <i className="fas fa-external-link-alt" />
                    </a>
                </small>
            </span>
        ];

        return (
            <div>
                <PubTrackerPanel title={statusHeader}>
                    <div className="row clearfix">
                        <div className="col-xs-6" style={{borderRight: '1px solid #dddddd'}}>
                            {status && curators.length > 0 && statuses.length > 0 && locations.length > 0 &&
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
                        <div className="col-xs-5">
                            <div className="row">
                                <div className="col-xs-offset-1" style={{marginTop: '7px'}}>
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

                <PubTrackerPanel title='Topics'>
                    !! TOPICS !!
                </PubTrackerPanel>

                <PubTrackerPanel title='Notes'>
                    !! NOTES !!
                </PubTrackerPanel>

                <PubTrackerPanel title='Contact Authors'>
                    !! AUTHOR NOTIF !!
                </PubTrackerPanel>
            </div>
        )
    }
}

PubTracker.propTypes = {
    pubId: PropTypes.string,
    userId: PropTypes.string,
};

export default PubTracker;
