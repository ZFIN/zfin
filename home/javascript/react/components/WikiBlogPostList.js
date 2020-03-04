import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {getLink} from '../api/wiki';
import LoadingSpinner from './LoadingSpinner';

class WikiBlogPostList extends Component {
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
            next: response._links.next,
            loading: false,
        }));
    }

    loadNext(evt) {
        evt.preventDefault();
        this.setState({loading: true});
        getLink(this.state.next).then(response => this.setState(state => ({
            posts: state.posts.concat(response.results),
            next: response._links.next,
            loading: false,
        })));
    }

    render() {
        const {posts, next, loading} = this.state;
        const { showAll } = this.props;

        return (
            <div>
                <ul className='list-unstyled'>
                    {posts.map(post => (
                        <li key={post.id}>
                            <small>{new Date(post.history.createdDate).toLocaleString('en-us', {
                                day: 'numeric',
                                month: 'long',
                                hour: 'numeric',
                                minute: '2-digit',
                                year: 'numeric'
                            })}</small>
                            <p><a href={`https://${process.env.WIKI_HOST}${post._links.webui}`}>{post.title}</a></p>
                        </li>
                    ))}
                </ul>
                <LoadingSpinner loading={loading} />
                <div className='wiki-list-controls'>
                    <span>{ next && <a href='#' onClick={this.loadNext}>Load More</a> }</span>
                    <span>{ showAll && <a href={`https://${process.env.WIKI_HOST}${showAll}`}>See All <i className='fas fa-chevron-right' /></a>}</span>
                </div>
            </div>
        );
    }
}

WikiBlogPostList.propTypes = {
    onInit: PropTypes.func,
    showAll: PropTypes.string,
};

export default WikiBlogPostList;
