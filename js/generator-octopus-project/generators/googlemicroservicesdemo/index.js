const Generator = require('yeoman-generator');
module.exports = class extends Generator {
    async prompting() {
        this.answers = await this.prompt([
            {
                type: "input",
                name: "project_name",
                message: "The Project Name",
                default: "googlemicroservicesdemo"
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
                name: "octopus_application_lifecycle_id",
                message: "The lifecycle to assign to the project",
                default: "${{ secrets.OCTOPUS_APPLICATION_LIFECYCLE_ID }}"
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
                name: "aws_region",
                message: "The AWS region to create the state bucket in",
                default: "us-west-1"
            },
            {
                type: "input",
                name: "octopus_development_app_environment_id",
                message: "The development environment ID",
                default: "${{ secrets.OCTOPUS_DEVELOPMENT_ENVIRONMENT_ID }}"
            },
            {
                type: "input",
                name: "octopus_development_security_environment_id",
                message: "The development security environment ID",
                default: "${{ secrets.OCTOPUS_DEVELOPMENT_SECURITY_ENVIRONMENT_ID }}"
            },
            {
                type: "input",
                name: "octopus_production_app_environment_id",
                message: "The production environment ID",
                default: "${{ secrets.OCTOPUS_PRODUCTION_ENVIRONMENT_ID }}"
            },
            {
                type: "input",
                name: "octopus_production_only_lifecycle_id",
                message: "The production only lifecycle ID",
                default: "${{ secrets.OCTOPUS_PRODUCTION_ONLY_LIFECYCLE_ID }}"
            },
            {
                type: "input",
                name: "octopus_simple_lifecycle_id",
                message: "The simple lifecycle ID",
                default: "${{ secrets.OCTOPUS_SIMPLE_LIFECYCLE_ID }}"
            },
            {
                type: "input",
                name: "octopus_production_security_environment_id",
                message: "The production security environment ID",
                default: "${{ secrets.OCTOPUS_PRODUCTION_SECURITY_ENVIRONMENT_ID }}"
            },
            {
                type: "input",
                name: "octopus_dockerhub_feed_id",
                message: "The ID of the DockerHub feed",
                default: "${{ secrets.OCTOPUS_DOCKERHUB_FEED_ID }}"
            },
            {
                type: "input",
                name: "github_package_pat",
                message: "The GitHub Personal Access Token used to access package feeds",
                default: "${{ secrets.GH_PACKAGES_PAT }}"
            },
            {
                type: "input",
                name: "namespace_prefix",
                message: "The prefix of the namespace used to hold the microservice demo. For example, you may set this to you own name to create an isolated deployment in a shared cluster.",
                default: '#{Octopus.Space.Name | Replace \\"[^A-Za-z0-9]\\" \\"-\\" | ToLower}-'
            },
        ]);
    }

    writing() {
        const options = {
            project_name: this.answers["project_name"],
            aws_access_key: this.answers["aws_access_key"],
            aws_secret_key: this.answers["aws_secret_key"],
            aws_region: this.answers["aws_region"],
            octopus_server: this.answers["octopus_server"],
            octopus_apikey: this.answers["octopus_apikey"],
            octopus_space_id: this.answers["octopus_space_id"],
            octopus_development_app_environment_id: this.answers["octopus_development_app_environment_id"],
            octopus_production_only_lifecycle_id: this.answers["octopus_production_only_lifecycle_id"],
            octopus_simple_lifecycle_id: this.answers["octopus_simple_lifecycle_id"],
            octopus_development_security_environment_id: this.answers["octopus_development_security_environment_id"],
            octopus_production_app_environment_id: this.answers["octopus_production_app_environment_id"],
            octopus_production_security_environment_id: this.answers["octopus_production_security_environment_id"],
            octopus_dockerhub_feed_id: this.answers["octopus_dockerhub_feed_id"],
            octopus_application_lifecycle_id: this.answers["octopus_application_lifecycle_id"],
            github_package_pat: this.answers["github_package_pat"],
            namespace_prefix: this.answers["namespace_prefix"],
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
    }
};
