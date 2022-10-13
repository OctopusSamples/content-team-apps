const Generator = require('yeoman-generator');
const {v4: uuidv4} = require('uuid');

module.exports = class extends Generator {
    async prompting() {
        this.answers = await this.prompt([
            {
                type: "input",
                name: "project_name",
                message: "The Project Name",
                default: "octopusapplicationfeaturebranch"
            },
            {
                type: "input",
                name: "channel_project_name",
                message: "Your project name",
                default: "${{ secrets.PROJECT_NAME }}"
            },
            {
                type: "input",
                name: "step_name",
                message: "Your project name",
                default: "${{ secrets.DEPLOYMENT_STEP_NAME }}"
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
                name: "aws_region",
                message: "The AWS region to deploy the ECR repository in",
                default: "us-west-1"
            }
        ]);
    }

    writing() {
        const options = {
            project_name: this.answers["project_name"],
            channel_project_name: this.answers["channel_project_name"],
            octopus_server: this.answers["octopus_server"],
            octopus_apikey: this.answers["octopus_apikey"],
            octopus_space_id: this.answers["octopus_space_id"],
            step_name: this.answers["step_name"],
            aws_access_key: this.answers["aws_access_key"],
            aws_secret_key: this.answers["aws_secret_key"],
            aws_region: this.answers["aws_region"]
        };

        this.fs.copyTpl(
            this.templatePath('.github/workflows/octopus.yaml'),
            this.destinationPath('.github/workflows/' + this.answers["project_name"] + '.yaml'),
            options
        );

        this.fs.copyTpl(
            this.templatePath('.github/workflows/octopusdestroy.yaml'),
            this.destinationPath('.github/workflows/' + this.answers["project_name"] + 'destroy.yaml'),
            options
        );

        this.fs.copyTpl(
            this.templatePath('github/octopus/action.yaml'),
            this.destinationPath('github/' + this.answers["project_name"] + '/action.yaml'),
            options
        );

        this.fs.copyTpl(
            this.templatePath('github/octopusdestroy/action.yaml'),
            this.destinationPath('github/' + this.answers["project_name"] + 'destroy/action.yaml'),
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
