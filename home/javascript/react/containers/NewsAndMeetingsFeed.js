import React from 'react';
import {getBlogPosts} from "../api/wiki";
import WikiBlogPostList from "../components/WikiBlogPostList";

function NewsAndMeetingsFeed() {
    return <WikiBlogPostList onInit={() => getBlogPosts('news')} showAll='/display/news' />
}

export default NewsAndMeetingsFeed;
