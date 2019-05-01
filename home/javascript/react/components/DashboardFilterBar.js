import React from 'react';
import PropTypes from 'prop-types';
import { getCurators, getStatuses } from "../api/publication";

class DashboardFilterBar extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            curators: [],
            statuses: [],
            orders: [
                {
                    value: 'date',
                    display: 'Last status change (oldest)'
                },
                {
                    value: '-date',
                    display: 'Last status change (newest)'
                },
                {
                    value: 'pub.lastCorrespondenceDate',
                    display: 'Last correspondence (oldest)'
                },
                {
                    value: '-pub.lastCorrespondenceDate',
                    display: 'Last correspondence (newest)'
                }
            ],
            selectedCurator: props.currentUserId,
            selectedStatus: '',
            selectedOrder: 'date'
        }
    }

    componentDidMount() {
        const { currentUserId } = this.props;
        getStatuses().then(statuses => this.setState({statuses}));
        getCurators().then(curators => {
            const meIdx = curators.findIndex(curator => curator.zdbID === currentUserId);
            const me = curators.splice(meIdx, 1)[0];
            me.name = 'Me';
            this.setState({
                curators: [
                    me,
                    {zdbID: '*', name: "Anyone"},
                    {zdbID: '-', name: "──────────", disabled: true},
                    ...curators
                ]
            })
        });
        this.fireChangeCallback();
    }

    componentDidUpdate(prevProps, prevState) {
        const { selectedCurator, selectedOrder, selectedStatus } = this.state;
        if (selectedCurator !== prevState.selectedCurator ||
            selectedOrder !== prevState.selectedOrder ||
            selectedStatus !== prevState.selectedStatus) {
            this.fireChangeCallback();
        }
    }

    fireChangeCallback() {
        const { selectedCurator, selectedOrder, selectedStatus } = this.state;
        this.props.onChange(selectedCurator, selectedOrder, selectedStatus);
    }

    updateCurator(event) {
        this.setState({selectedCurator: event.target.value});
    }

    updateOrder(event) {
        this.setState({selectedOrder: event.target.value});
    }

    updateStatus(event) {
        this.setState({selectedStatus: event.target.value});
    }

    render() {
        const { curators, orders, statuses, selectedCurator, selectedOrder, selectedStatus } = this.state;
        const { loading } = this.props;
        const selectStyle = {margin: '0 5px'};
        return (
            <div className="row filter-bar">
                <div className="col-sm-12">
                    <form className="form-inline">
                        Pubs assigned to
                        <select className="form-control" onChange={(event) => this.updateCurator(event)} style={selectStyle} value={selectedCurator}>
                            {curators.map(curator => (
                                <option disabled={curator.disabled} key={curator.zdbID} value={curator.zdbID}>
                                    {curator.name}
                                </option>
                            ))}
                        </select>

                        with status
                        <select className="form-control" onChange={(event) => this.updateStatus(event)} style={selectStyle} value={selectedStatus}>
                            <option value=''>Any</option>
                            {statuses.map(status => (
                                <option key={status.id} value={status.id}>
                                    {status.name}
                                </option>
                            ))}
                        </select>

                        by
                        <select className="form-control" onChange={(event) => this.updateOrder(event)} style={selectStyle} value={selectedOrder}>
                            {orders.map(order => (
                                <option key={order.value} value={order.value}>
                                    {order.display}
                                </option>
                            ))}
                        </select>

                        <button
                            title="Refresh query"
                            className="btn btn-link pub-dashboard-reload"
                            disabled={loading}
                            type="button"
                            onClick={() => this.fireChangeCallback()}
                        >
                            <span className={`fa-animation-container ${loading ? 'fa-spin' : ''}`}><i className="fas fa-sync" /></span>
                        </button>
                    </form>
                </div>
            </div>
        );
    }
}

DashboardFilterBar.propTypes = {
    currentUserId: PropTypes.string,
    loading: PropTypes.bool,
    onChange: PropTypes.func,
};

export default DashboardFilterBar;
