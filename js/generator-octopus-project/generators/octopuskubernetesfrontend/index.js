const Generator = require('yeoman-generator');
const {v4: uuidv4} = require('uuid');

module.exports = class extends Generator {
    async prompting() {
        this.answers = await this.prompt([
            {
                type: "input",
                name: "project_name",
                message: "The Project Name",
                default: "k8sfrontend"
            },
            {
                type: "input",
                name: "docker_image",
                message: "The Docker image to deploy",
                default: "octopussamples/underwater-app"
            },
            {
                type: "input",
                name: "docker_image_port",
                message: "The web port to expose in the pod",
                default: "80"
            },
            {
                type: "input",
                name: "octopus_project_name",
                message: "Your project name",
                default: "Kubernetes Frontend WebApp"
            },
            {
                type: "input",
                name: "octopus_project_description",
                message: "Your project description",
                default: "A description of my project"
            },
            {
                type: "confirm",
                name: "existing_project_group",
                message: "Use an existing project group",
                default: false
            },
            {
                type: "input",
                name: "octopus_project_group_name",
                message: "Your project group name",
                default: "Kubernetes Frontend"
            },
            {
                type: "input",
                name: "aws_access_key",
                message: "The AWS access key.",
                default: "${{ secrets.AWS_ACCESS_KEY_ID }}"
            },
            {
                type: "password",
                name: "aws_secret_key",
                message: "The AWS secret key.",
                default: "${{ secrets.AWS_SECRET_ACCESS_KEY }}"
            },
            {
                type: "input",
                name: "octopus_server",
                message: "The hostname of your Octopus instance.",
                default: "${{ secrets.OCTOPUS_SERVER }}"
            },
            {
                type: "password",
                name: "octopus_apikey",
                message: "The Octopus API key.",
                default: "${{ secrets.OCTOPUS_APIKEY }}"
            },
            {
                type: "input",
                name: "octopus_space_id",
                message: "The Octopus Space",
                default: "${{ secrets.OCTOPUS_SPACE_ID }}"
            },
            {
                type: "input",
                name: "octopus_development_environment_id",
                message: "The development environment ID",
                default: "${{ secrets.OCTOPUS_DEVELOPMENT_ENVIRONMENT_ID }}"
            },
            {
                type: "input",
                name: "octopus_production_environment_id",
                message: "The production environment ID",
                default: "${{ secrets.OCTOPUS_PRODUCTION_ENVIRONMENT_ID }}"
            },
            {
                type: "input",
                name: "octopus_development_security_environment_id",
                message: "The development environment ID",
                default: "${{ secrets.OCTOPUS_DEVELOPMENT_SECURITY_ENVIRONMENT_ID }}"
            },
            {
                type: "input",
                name: "octopus_production_security_environment_id",
                message: "The production environment ID",
                default: "${{ secrets.OCTOPUS_PRODUCTION_SECURITY_ENVIRONMENT_ID }}"
            },
            {
                type: "input",
                name: "octopus_lifecycle_id",
                message: "The lifecycle to assign to the project",
                default: "${{ secrets.OCTOPUS_APPLICATION_LIFECYCLE_ID }}"
            },
            {
                type: "input",
                name: "octopus_aws_development_account_id",
                message: "The ID of the development AWS account used to deploy the Cloudformation template",
                default: "${{ secrets.OCTOPUS_AWS_DEVELOPMENT_ACCOUNT_ID }}"
            },
            {
                type: "input",
                name: "octopus_aws_production_account_id",
                message: "The ID of the production AWS account used to deploy the Cloudformation template",
                default: "${{ secrets.OCTOPUS_AWS_PRODUCTION_ACCOUNT_ID }}"
            },
            {
                type: "input",
                name: "aws_region",
                message: "The AWS region to deploy the ECR repository in",
                default: "us-west-1"
            },
            {
                type: "input",
                name: "octopus_dockerhub_feed_id",
                message: "The ID of the DockerHub feed",
                default: "${{ secrets.OCTOPUS_DOCKERHUB_FEED_ID }}"
            },
            {
                type: "input",
                name: "dockerhub_username",
                message: "The Dockerhub username",
                default: "${{ secrets.DOCKERHUB_USERNAME }}"
            },
            {
                type: "input",
                name: "dockerhub_password",
                message: "The DockerHub password",
                default: "${{ secrets.DOCKERHUB_PASSWORD }}"
            },
        ]);
    }

    writing() {
        const options = {
            project_name: this.answers["project_name"],
            existing_project_group: this.answers["existing_project_group"],
            aws_access_key: this.answers["aws_access_key"],
            aws_secret_key: this.answers["aws_secret_key"],
            octopus_server: this.answers["octopus_server"],
            octopus_apikey: this.answers["octopus_apikey"],
            octopus_space_id: this.answers["octopus_space_id"],
            octopus_development_environment_id: this.answers["octopus_development_environment_id"],
            octopus_production_environment_id: this.answers["octopus_production_environment_id"],
            octopus_development_security_environment_id: this.answers["octopus_development_security_environment_id"],
            octopus_production_security_environment_id: this.answers["octopus_production_security_environment_id"],
            octopus_project_name: this.answers["octopus_project_name"],
            octopus_project_description: this.answers["octopus_project_description"],
            octopus_project_group_name: this.answers["octopus_project_group_name"],
            octopus_lifecycle_id: this.answers["octopus_lifecycle_id"],
            octopus_aws_development_account_id: this.answers["octopus_aws_development_account_id"],
            octopus_aws_production_account_id: this.answers["octopus_aws_production_account_id"],
            aws_region: this.answers["aws_region"],
            octopus_dockerhub_feed_id: this.answers["octopus_dockerhub_feed_id"],
            docker_image_port: this.answers["docker_image_port"],
            docker_image: this.answers["docker_image"],
            dockerhub_username: this.answers["dockerhub_username"],
            dockerhub_password: this.answers["dockerhub_password"],
        };

        this.fs.copyTpl(
            this.templatePath('.github/workflows/octopus.yaml'),
            this.destinationPath('.github/workflows/' + this.answers["project_name"] + '.yaml'),
            options
        );

        this.fs.copyTpl(
            this.templatePath('github/octopus/action.yaml'),
            this.destinationPath('github/' + this.answers["project_name"] + '/action.yaml'),
            options
        );

        this.fs.copyTpl(
            this.templatePath('terraform/octopus/*.tf'),
            this.destinationPath('terraform/' + this.answers["project_name"]),
            options,
            null,
            { globOptions: { dot: true } }
        );

        this.fs.copyTpl(
            this.templatePath('bash/octopus/*.sh'),
            this.destinationPath('bash/' + this.answers["project_name"]),
            options,
            null,
            { globOptions: { dot: true } }
        );
    }

    install() {

    }
};
