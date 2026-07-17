import React from 'react';
import PropTypes from 'prop-types';

import {getCurators, getStatuses, searchPubStatus} from '../api/publication';
import DashboardPubList from '../components/DashboardPubList';
import Pagination from '../components/Pagination';
import FilterBar from '../components/FilterBar';
import CuratorSelectBox from '../components/CuratorSelectBox';
import StatusSelectBox from '../components/StatusSelectBox';
import SelectBox from '../components/SelectBox';
import RefreshButton from '../components/RefreshButton';
import LoadingSpinner from '../components/LoadingSpinner';
import intertab from '../utils/intertab';

const PUBS_PER_PAGE = 50;
const SORT_OPTIONS = [
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
];

class PubDashboard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            curators: [],
            statuses: [],
            loading: false,
            page: 1,
            owner: this.props.userId,
            sort: SORT_OPTIONS[0].value,
            status: '',
            results: {},
        };
        this.handleOwnerChange = this.handleOwnerChange.bind(this);
        this.handleStatusChange = this.handleStatusChange.bind(this);
        this.handleSortChange = this.handleSortChange.bind(this);
        this.handlePageChange = this.handlePageChange.bind(this);
        this.fetchPubs = this.fetchPubs.bind(this);
    }

    componentDidMount() {
        intertab.addListener(intertab.EVENTS.PUB_STATUS, () => this.fetchPubs());
        getCurators().then(curators => this.setState({curators}));
        getStatuses().then(statuses => this.setState({statuses}));
        this.fetchPubs();
    }

    componentDidUpdate(prevProps, prevState) {
        const {owner, status, sort, page} = this.state;
        if (owner !== prevState.owner || status !== prevState.status || sort !== prevState.sort || page !== prevState.page) {
            this.fetchPubs();
        }
    }

    fetchPubs() {
        const {owner, sort, status, page} = this.state;
        const params = {
            owner,
            status,
            count: PUBS_PER_PAGE,
            offset: (page - 1) * PUBS_PER_PAGE,
            sort
        };
        this.setState({
            results: {},
            loading: true
        });
        searchPubStatus(params)
            .then(results => this.setState({
                results,
                loading: false
            }));
    }

    handleOwnerChange(owner) {
        this.setState({owner, page: 1});
    }

    handleStatusChange(status) {
        this.setState({status, page: 1});
    }

    handleSortChange(sort) {
        this.setState({sort});
    }

    handlePageChange(page) {
        this.setState({page});
    }

    render() {
        const {curators, statuses, loading, page, owner, sort, status, results} = this.state;
        return (
            <div className='pub-dashboard'>
                <FilterBar>
                    Pubs assigned to
                    <CuratorSelectBox
                        curators={curators}
                        selectedId={owner}
                        userId={this.props.userId}
                        onSelect={this.handleOwnerChange}
                    />
                    with status
                    <StatusSelectBox statuses={statuses} selectedId={status} onSelect={this.handleStatusChange}/>
                    by
                    <SelectBox options={SORT_OPTIONS} value={sort} onSelect={this.handleSortChange}/>
                    <RefreshButton loading={loading} onClick={this.fetchPubs}/>
                </FilterBar>

                {loading && <div className='text-center text-muted'>
                    <LoadingSpinner/> Loading...
                </div>}

                {!loading && results.totalCount === 0 && <div className='text-center text-muted'>
                    No pubs match query
                </div>}

                <DashboardPubList pubs={results.publications} statusCounts={results.statusCounts}/>

                <div className='d-flex justify-content-center'>
                    <Pagination
                        onChange={this.handlePageChange}
                        page={page}
                        perPageSize={PUBS_PER_PAGE}
                        total={results.totalCount}
                    />
                </div>
            </div>
        );
    }
}

PubDashboard.propTypes = {
    userId: PropTypes.string,
};

export default PubDashboard;
