import React, {Component} from 'react';
import PropTypes from 'prop-types';
import LoadingSpinner from './LoadingSpinner';

class WikiPostList extends Component {
    constructor(props) {
        super(props);
        this.state = {
            loading: false,
            posts: [],
        };
    }

    componentDidMount() {
        this.setState({loading: true});
        this.props.onInit().then(response => this.setState({
            posts: response.results,
            loading: false,
        }));
    }

    render() {
        const {posts, loading} = this.state;
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
                    <span/>
                    <span>{showAll &&
                    <a href={`https://zfin.atlassian.net/wiki${showAll}`}>See All <i className='fas fa-chevron-right'/></a>}</span>
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
