import React, {Component} from 'react';
import PropTypes from 'prop-types';
import LoadingSpinner from './LoadingSpinner';
import http from '../utils/http';

class WikiPostList extends Component {
    constructor(props) {
        super(props);
        this.state = {
            loading: false,
            posts: [],
            next: '',
        };
        this.loadNext = this.loadNext.bind(this);
    }

    componentDidMount() {
        this.setState({loading: true});
        this.props.onInit().then(response => this.setState({
            posts: response.results,
            next: response.nextPageURL,
            loading: false,
        }));
    }

    loadNext(evt) {
        evt.preventDefault();
        this.setState({loading: true});
        http.get(this.state.next).then(response => this.setState(state => ({
            posts: state.posts.concat(response.results),
            next: response.nextPageURL,
            loading: false,
        })));
    }

    render() {
        const {posts, next, loading} = this.state;
        const {showAll} = this.props;
        return (
            <div>
                <ul className='list-unstyled'>
                    {posts.map(post => (
                        <li key={post.id}>
                            <p><a href={post.url}>{post.title}</a></p>
                        </li>
                    ))}
                </ul>
                <LoadingSpinner loading={loading}/>
                <div className='wiki-list-controls'>
                    <span>{next && <a href='#' onClick={this.loadNext}>Load More</a>}</span>
                    <span>{showAll &&
                    <a href={`https://${process.env.WIKI_HOST}${showAll}`}>See All <i className='fas fa-chevron-right'/></a>}</span>
                </div>
            </div>
        );
    }
}

WikiPostList.propTypes = {
    onInit: PropTypes.func,
    showAll: PropTypes.string,
};

export default WikiPostList;
