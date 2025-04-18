import React from 'react';
import PropTypes from 'prop-types';
import produce, {setAutoFreeze} from 'immer';
setAutoFreeze(false);

import {getLocations, searchPubStatus, updateStatus} from '../api/publication';
import intertab from '../utils/intertab';

import Pagination from '../components/Pagination';
import FilterBar from '../components/FilterBar';
import SelectBox from '../components/SelectBox';
import RefreshButton from '../components/RefreshButton';
import LoadingCount from '../components/LoadingCount';
import PubClaimButton from '../components/PubClaimButton';
import BinPubList from '../components/BinPubList';
import RelativeDate from '../components/RelativeDate';
import PubPDFLink from '../components/PubPDFLink';
import FigureGallery from '../components/FigureGallery';

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

class CuratingBin extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            page: 1,
            loading: false,
            locations: [],
            location: '',
            results: {},
            sort: SORT_OPTIONS[0].value,
        };
        this.handleSortChange = this.handleSortChange.bind(this);
        this.handlePageChange = this.handlePageChange.bind(this);
        this.handleLocationChange = this.handleLocationChange.bind(this);
        this.updatePubStatus = this.updatePubStatus.bind(this);
        this.fetchPubs = this.fetchPubs.bind(this);
    }

    componentDidMount() {
        intertab.addListener(intertab.EVENTS.PUB_STATUS, () => this.fetchPubs());
        getLocations().then(allLocations => {
            const locations = allLocations.filter(location => location.role === 'CURATOR');
            this.setState({
                locations,
                location: locations[0].id
            });
        });
    }

    componentDidUpdate(prevProps, prevState) {
        const { location, sort, page } = this.state;
        if (sort !== prevState.sort || page !== prevState.page || location !== prevState.location) {
            this.fetchPubs();
        }
    }

    fetchPubs() {
        const { currentStatus } = this.props;
        const { location, sort, page } = this.state;
        const params = {
            status: currentStatus,
            location,
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

    handleSortChange(sort) {
        this.setState({sort});
    }

    handlePageChange(page) {
        this.setState({page});
    }

    handleLocationChange(location) {
        this.setState({location});
    }

    render() {
        const { loading, location, locations, page, sort, results } = this.state;
        const locationOptions = locations.map(location => ({
            value: location.id,
            display: location.name,
        }));
        const tableColumns = [
            {
                label: '',
                width: '115px',
                content: (pub, index) => <PubClaimButton publication={pub} onClaimPub={() => this.updatePubStatus(pub, index)} />
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
                        <p><b dangerouslySetInnerHTML={{__html: pub.title}} /></p>
                        <p>{pub.citation}</p>
                        <p>{pub.authors}</p>
                        <p dangerouslySetInnerHTML={{__html: pub.abstractText}} />
                        <p className='search-result-related-links mb-2 small'>
                            <ul>
                                {pub.relatedLinks.map(link => (
                                    <li key={link} dangerouslySetInnerHTML={{__html: link}} />
                                ))}
                            </ul>
                        </p>
                        <FigureGallery images={pub.images} />
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
            <React.Fragment>
                <div className='pub-dashboard'>
                    <FilterBar>
                        <b><LoadingCount count={results.totalCount} loading={loading}/></b> Pubs in
                        <SelectBox options={locationOptions} value={location} onSelect={this.handleLocationChange} />
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
            </React.Fragment>
        );
    }
}

CuratingBin.propTypes = {
    currentStatus: PropTypes.string,
    nextStatus: PropTypes.string,
    userId: PropTypes.string,
};

export default CuratingBin;
