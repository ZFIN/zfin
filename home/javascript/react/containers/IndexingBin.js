import React from 'react';
import PropTypes from 'prop-types';
import produce, {setAutoFreeze} from 'immer';
setAutoFreeze(false);

import {getLocations, searchPubStatus, updateStatus} from '../api/publication';

import Pagination from '../components/Pagination';
import FilterBar from '../components/FilterBar';
import SelectBox from '../components/SelectBox';
import RefreshButton from '../components/RefreshButton';
import LoadingCount from '../components/LoadingCount'
import BinPubList from '../components/BinPubList';
import RelativeDate from '../components/RelativeDate';
import PubPDFLink from '../components/PubPDFLink';
import PubClaimButton from '../components/PubClaimButton';
import intertab from '../utils/intertab';

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
    {
        value: 'pub.entryDate',
        display: 'Entry date (oldest)'
    },
    {
        value: '-pub.entryDate',
        display: 'Entry date (newest)'
    }
];

class IndexingBin extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            page: 1,
            loading: false,
            priorities: [],
            priority: '',
            results: {},
            sort: SORT_OPTIONS[0].value,
        };
        this.handlePriorityChange = this.handlePriorityChange.bind(this);
        this.handlePageChange = this.handlePageChange.bind(this);
        this.handleSortChange = this.handleSortChange.bind(this);
        this.fetchPubs = this.fetchPubs.bind(this);
        this.updatePubStatus = this.updatePubStatus.bind(this);
    }

    componentDidMount() {
        intertab.addListener(intertab.EVENTS.PUB_STATUS, () => this.fetchPubs());
        getLocations().then(locations => {
            const priorities = locations.filter(location => location.role === 'INDEXER');
            this.setState({
                priorities,
                priority: priorities[0].id
            });
        });
    }

    componentDidUpdate(prevProps, prevState) {
        const { sort, page, priority } = this.state;
        if (sort !== prevState.sort || page !== prevState.page || priority !== prevState.priority) {
            this.fetchPubs();
        }
    }

    fetchPubs() {
        const { currentStatus } = this.props;
        const { sort, page, priority } = this.state;
        const params = {
            status: currentStatus,
            location: priority,
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
        this.setState(produce(state => {
            state.results.publications[index][field] = value;
        }));
    }

    updatePubStatus(pub, index) {
        const { nextStatus, userId } = this.props;
        this.setPubState(index, 'saving', true);
        const status = {
            status: { id: nextStatus },
            location: null,
            owner: { zdbID: userId }
        };
        updateStatus(pub.zdbId, status, {checkOwner: true})
            .then(() => this.setPubState(index, 'claimed', true))
            .fail(error => error.responseJSON && this.setPubState(index, 'claimError', error.responseJSON.message))
            .always(() => this.setPubState(index, 'saving', false));
    }

    updatePubLocation(pub, index, location) {
        this.setPubState(index, 'saving', true);
        const status = this.state.results.publications.slice(index, index + 1)[0].status;
        status.location = { id: location };
        updateStatus(pub.zdbId, status)
            .always(() => this.setPubState(index, 'saving', false));
    }

    handlePriorityChange(priority) {
        this.setState({priority});
    }

    handleSortChange(sort) {
        this.setState({sort});
    }

    handlePageChange(page) {
        this.setState({page});
    }

    render() {
        const { loading, page, priorities, priority, sort, results } = this.state;
        const priorityOptions = priorities.map(priority => ({
            value: priority.id,
            display: priority.name,
        }));
        priorityOptions.push({value: 0, display: 'Not Set'});

        const tableColumns = [
            {
                label: 'Priority',
                width: '70px',
                content: (pub, index) => (
                    <SelectBox
                        options={priorityOptions}
                        value={pub.status.location ? pub.status.location.id : 0}
                        onSelect={location => this.updatePubLocation(pub, index, location)}
                    />
                )
            },
            {
                label: '',
                width: '115px',
                content: (pub, index) => <PubClaimButton publication={pub} onClaimPub={() => this.updatePubStatus(pub, index)} />,
            },
            {
                label: 'ZDB-ID',
                width: '150px',
                content: pub => <a href={`/${pub.zdbId}`} target='_blank' rel='noopener noreferrer'>{pub.zdbId}</a>,
            },
            {
                label: 'Details',
                content: pub => (
                    <div>
                        <p><b dangerouslySetInnerHTML={{__html: pub.title}}/></p>
                        <p dangerouslySetInnerHTML={{__html: pub.abstractText}}/>
                    </div>
                ),
            },
            {
                label: 'Time in Bin',
                width: '100px',
                content: pub => <RelativeDate date={pub.status.updateDate} />,
            },
            {
                label: 'PDF',
                width: '50px',
                content: pub => <PubPDFLink publication={pub} />,
            }
        ];

        return (
            <div className='pub-dashboard'>
                <FilterBar>
                    <b><LoadingCount count={results.totalCount} loading={loading}/></b> Pubs with priority
                    <SelectBox options={priorityOptions} value={priority} onSelect={this.handlePriorityChange}/>
                    by
                    <SelectBox options={SORT_OPTIONS} value={sort} onSelect={this.handleSortChange} />
                    <RefreshButton loading={loading} onClick={this.fetchPubs} />
                </FilterBar>

                <BinPubList columns={tableColumns} loading={loading} pubs={results.publications} />

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

IndexingBin.propTypes = {
    currentStatus: PropTypes.string,
    nextStatus: PropTypes.string,
    userId: PropTypes.string,
};

export default IndexingBin;
