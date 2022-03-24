#!/bin/bash
# Install the Yeoman generator
npm install -g @octopus-content-team/generator-github-complete-eks-deployment
# Build the template files
yo @octopus-content-team/github-complete-eks-deployment \
  '--aws-state-bucket-region=<%= aws_state_bucket_region %>' \
  '--s3-bucket-suffix=<%= s3_bucket_suffix %>' \
  '--aws-region=<%= aws_region %>' \
  '--platform=<%= platform %>' \
  '--framework=<%= framework %>'