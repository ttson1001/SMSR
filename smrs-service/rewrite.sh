#!/bin/bash
git filter-branch --env-filter '
if [ "$GIT_COMMITTER_EMAIL" = "vuhongbao2807@gmail.com" ]; then
    export GIT_COMMITTER_NAME="Your Name"
    export GIT_COMMITTER_EMAIL="baobao2807990@gmail.com"
fi
if [ "$GIT_AUTHOR_EMAIL" = "vuhongbao2807@gmail.com" ]; then
    export GIT_AUTHOR_NAME="Your Name"
    export GIT_AUTHOR_EMAIL="baobao2807990@gmail.com"
fi
' -- --all
