A Yeoman generator to build a number of common projects in Octopus.

## Install

`npm install -g @octopus-content-team/generator-octopus-project`

## Octopus Configuration

These templates assume certain environments, accounts, feeds etc. These resources are captured
in GitHub Actions secrets for convenience.

### Environments

* `Development` - The development environment where applications are deployed.
* `Development (Security)` - This environment is used to run (and re-run) security scans on things like SBOM packages on development deployments.
* `Production` - The production environment where applications are deployed.
* `Production (Security)` - This environment is used to run (and re-run) security scans on things like SBOM packages om production deployments.

### Lifecycles

* `Application: Development -> Development (Security) -> Production -> Production (Security)` - The progression of an application deployment.
* `Production Only -> Production` - Deployments only to the production environment.
* `Infrastructure: Development -> Production` - Deployments of infrastructure that have no security scanning.

### Feeds

* `ECR` - An ECR Docker feed.
* `DockerHub` - A Docker feed accessing Docker Hub.

### Accounts

* `AWS Development` - The account used to deploy development applications. Scoped to the `Development` and `Development (Security)` environments.
* `AWS Production` - The account used to deploy production applications. Scoped to the `Production` and `Production (Security)` environments.

## Expected GitHub Actions Secrets

Many of the variables used by these templates default to secret values held by a GitHub repo. This
allows files to be moved between repos and still function correctly. However, it requires
repos to have a standard set of secrets defined, as shown below:

* `AWS_ACCESS_KEY_ID` - The AWS secret key used to create S3 buckets for the Terraform state.
* `AWS_SECRET_ACCESS_KEY` - The AWS secret key used to create S3 buckets for the Terraform state.
* `OCTOPUS_APIKEY` - The API key used to connect to the Octopus instance.
* `OCTOPUS_APPLICATION_LIFECYCLEID` - The lifecycle ID used when deploying application.
* `OCTOPUS_INFRASTRUCTURE_LIFECYCLEID` - The lifecycle ID used when deploying infrastructure.
* `OCTOPUS_PRODUCTION_ONLY_LIFECYCLEID` - The lifecycle ID used when deploying production resources.
* `OCTOPUS_AWS_DEVELOPMENT_ACCOUNTID` - The ID of the AWS account used when deploying to development environments.
* `OCTOPUS_AWS_PRODUCTION_ACCOUNTID` - The ID of the AWS account used when deploying to production environments.
* `OCTOPUS_DEVELOPMENT_ENVIRONMENT_ID` - The ID of the application development environment (i.e. the environment where applications are deployed).
* `OCTOPUS_DEVELOPMENT_SECURITY_ENVIRONMENT_ID` - The ID of the security development environment (i.e. the environment where security scans are run).
* `OCTOPUS_PRODUCTION_ENVIRONMENT_ID` - The ID of the application production environment (i.e. the environment where applications are deployed).
* `OCTOPUS_PRODUCTION_SECURITY_ENVIRONMENT_ID` - The ID of the security production environment (i.e. the environment where security scans are run).
* `OCTOPUS_SERVER` - The Octopus server URL.
* `OCTOPUS_SPACEID` - The Octopus space ID.

## SubGenerators

* `yo @octopus-content-team/octopus-project:apprunner` - creates a project to deploy an image as an App Runner instance.
* `yo @octopus-content-team/octopus-project:ecr` - creates a project to deploy an ECR repository.