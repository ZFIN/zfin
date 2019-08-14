import React from 'react';
import {getBlogPosts} from "../api/wiki";
import WikiBlogPostList from "../components/WikiBlogPostList";

class NewsAndMeetingsFeed extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            posts: [],
        };
    }

    componentDidMount() {
        getBlogPosts('news').then(response => this.setState({posts: response.results}));
    }

    render() {
        return <WikiBlogPostList posts={this.state.posts} />
    }
}

export default NewsAndMeetingsFeed;
