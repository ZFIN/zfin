import React from 'react';
import PropTypes from 'prop-types';
import update from 'immutability-helper';
import {searchPubStatus, updateStatus} from "../api/publication";
import ProcessingBinList from "../components/ProcessingBinList";
import Pagination from "../components/Pagination";
import FilterBar from "../components/FilterBar";
import SelectBox from "../components/SelectBox";
import RefreshButton from "../components/RefreshButton";

const PUBS_PER_PAGE = 50;
const SORT_OPTIONS = [
    {
        value: 'date',
        display: 'Time in bin (oldest)'
    },
    {
        value: '-date',
        display: 'Time in bin (newest)'
    },
];

class ProcessingBin extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            page: 1,
            loading: false,
            results: {},
            sort: SORT_OPTIONS[0].value,
        };
        this.handleSortChange = this.handleSortChange.bind(this);
        this.handlePageChange = this.handlePageChange.bind(this);
        this.updatePubStatus = this.updatePubStatus.bind(this);
        this.fetchPubs = this.fetchPubs.bind(this);
    }

    componentDidMount() {
        this.fetchPubs();
    }

    componentDidUpdate(prevProps, prevState) {
        const { sort, page } = this.state;
        if (sort !== prevState.sort || page !== prevState.page) {
            this.fetchPubs();
        }
    }

    fetchPubs() {
        const { currentStatus } = this.props;
        const { sort, page } = this.state;
        const params = {
            status: currentStatus,
            sort,
            count: PUBS_PER_PAGE,
            offset: (page - 1) * PUBS_PER_PAGE
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

    setPubState(index, field, value) {
        this.setState({
            results: update(this.state.results, {publications: {[index]: {[field]: {$set: value}}}})
        });
    }

    updatePubStatus(pub, index) {
        const { nextStatus, userId } = this.props;
        this.setPubState(index, 'saving', true);
        const status = {
            pubZdbID: pub.zdbId,
            status: { id: nextStatus },
            location: null,
            owner: { zdbID: userId }
        };
        updateStatus(status, true)
            .then(() => this.setPubState(index, 'claimed', true))
            .fail(error => error.responseJSON && this.setPubState(index, 'claimError', error.responseJSON.message))
            .always(() => this.setPubState(index, 'saving', false));
    }

    handleSortChange(sort) {
        this.setState({sort});
    }

    handlePageChange(page) {
        this.setState({page});
    }

    render() {
        const { loading, page, sort, results } = this.state;
        return (
            <div className="pub-dashboard">
                <FilterBar>
                    <b>{loading ? '▒' : results.totalCount}</b> Pubs ready for processing by
                    <SelectBox options={SORT_OPTIONS} value={sort} onSelect={this.handleSortChange} />
                    <RefreshButton loading={loading} onClick={this.fetchPubs} />
                </FilterBar>

                <ProcessingBinList loading={loading}
                                   onClaimPub={this.updatePubStatus}
                                   pubs={results.publications}
                />

                <div className='center'>
                    <Pagination onChange={this.handlePageChange}
                                page={page}
                                perPageSize={PUBS_PER_PAGE}
                                total={results.totalCount}
                    />
                </div>
            </div>
        );
    }
}

ProcessingBin.propTypes = {
    currentStatus: PropTypes.string,
    nextStatus: PropTypes.string,
    userId: PropTypes.string,
};

export default ProcessingBin;
