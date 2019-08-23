import React from 'react';
import {getBlogPosts} from "../api/wiki";
import WikiBlogPostList from "../components/WikiBlogPostList";

function JobsFeed() {
    return <WikiBlogPostList
        onInit={() => getBlogPosts('jobs', { query: 'created >= now("-120d")'})}
        showAll='/display/jobs/Zebrafish-Related+Job+Announcements'
    />
}

export default JobsFeed;
