const Generator = require('yeoman-generator');

module.exports = class extends Generator {
    async prompting() {
        this.answers = await this.prompt([
            {
                type: "input",
                name: "project_name",
                message: "The Project Name",
                default: "octopusawsaccounts"
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
                name: "development_aws_access_key",
                message: "The AWS access key used for the development account",
                default: "${{ secrets.AWS_DEVELOPMENT_ACCESS_KEY_ID }}"
            },
            {
                type: "password",
                name: "development_aws_secret_access_key",
                message: "The AWS secret access key used for the development account",
                default: "${{ secrets.AWS_DEVELOPMENT_SECRET_ACCESS_KEY_ID }}"
            },
            {
                type: "input",
                name: "production_aws_access_key",
                message: "The AWS access key used for the production account",
                default: "${{ secrets.AWS_PRODUCTION_ACCESS_KEY_ID }}"
            },
            {
                type: "password",
                name: "production_aws_secret_access_key",
                message: "The AWS secret access key used for the production account",
                default: "${{ secrets.AWS_PRODUCTION_SECRET_ACCESS_KEY_ID }}"
            },
            {
                type: "input",
                name: "development_environment_id",
                message: "The development environment ID",
                default: "${{ secrets.OCTOPUS_DEVELOPMENT_ENVIRONMENT_ID }}"
            },
            {
                type: "input",
                name: "development_security_environment",
                message: "The development security environment ID",
                default: "${{ secrets.OCTOPUS_DEVELOPMENT_SECURITY_ENVIRONMENT_ID }}"
            },
            {
                type: "input",
                name: "production_environment_id",
                message: "The production environment ID",
                default: "${{ secrets.OCTOPUS_PRODUCTION_ENVIRONMENT_ID }}"
            },
            {
                type: "input",
                name: "production_security_environment",
                message: "The production security environment ID",
                default: "${{ secrets.OCTOPUS_PRODUCTION_SECURITY_ENVIRONMENT_ID }}"
            }
        ]);
    }

    writing() {
        const options = {
            project_name: this.answers["project_name"],
            aws_access_key: this.answers["aws_access_key"],
            aws_secret_key: this.answers["aws_secret_key"],
            octopus_server: this.answers["octopus_server"],
            octopus_apikey: this.answers["octopus_apikey"],
            octopus_space_id: this.answers["octopus_space_id"],
            aws_region: this.answers["aws_region"],
            development_aws_access_key: this.answers["development_aws_access_key"],
            development_aws_secret_access_key: this.answers["development_aws_secret_access_key"],
            production_aws_access_key: this.answers["production_aws_access_key"],
            production_aws_secret_access_key: this.answers["production_aws_secret_access_key"],
            development_environment_id: this.answers["development_environment_id"],
            development_security_environment: this.answers["development_security_environment"],
            production_environment_id: this.answers["production_environment_id"],
            production_security_environment: this.answers["production_security_environment"],
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

    install() {

    }
};
