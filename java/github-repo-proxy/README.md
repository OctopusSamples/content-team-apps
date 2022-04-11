# GitHub Repo Proxy

After creating a GitHub repo with the sample project, the App Builder needs to monitor the status
of files being uploaded and the workflow being run.

This project provides a simple proxy over the GitHub API. It accepts the encrypted GitHub
session cookie for authentication, so the frontend app never has direct access to the GitHub
API.

It is expected that the frontend will poll this service to determine when the repo is created,
populated, and when the workflow has completed.