const Generator = require('yeoman-generator');

module.exports = class extends Generator {
    async prompting() {
        this.answers = await this.prompt([
            {
                type: "input",
                name: "docker_image",
                message: "The Docker image to deploy",
                default: "public.ecr.aws/nginx/nginx:stable"
            },
            {
                type: "input",
                name: "docker_port",
                message: "The port to expose on the Docker container",
                default: "80"
            },
            {
                type: "input",
                name: "apprunner_service_name",
                message: "The name of the App Runner service",
                default: "app-runner"
            },
            {
                type: "input",
                name: "cloudformation_stack_name",
                message: "The name of the CloudFormation stack to build the App Runner instance (also used to name the directory holding the terraform files)",
                default: "app-runner"
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
                default: "App Runner"
            },
            {
                type: "input",
                name: "octopus_project_name",
                message: "Your project name",
                default: "App Runner"
            },
            {
                type: "input",
                name: "aws_region",
                message: "The AWS region to deploy the App Runner instance in",
                default: "us-west-2"
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
                message: "The Octopus Space ID",
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
                name: "octopus_ecr_feed_name",
                message: "The name of the ECR feed",
                default: "ECR"
            }
        ]);
    }

    writing() {
        const options = {
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
            octopus_project_group_name: this.answers["octopus_project_group_name"],
            octopus_lifecycle_id: this.answers["octopus_lifecycle_id"],
            docker_image: this.answers["docker_image"],
            docker_port: this.answers["docker_port"],
            octopus_aws_development_account_id: this.answers["octopus_aws_development_account_id"],
            octopus_aws_production_account_id: this.answers["octopus_aws_production_account_id"],
            aws_region: this.answers["aws_region"],
            octopus_ecr_feed_name: this.answers["octopus_ecr_feed_name"],
            cloudformation_stack_name: this.answers["cloudformation_stack_name"],
            apprunner_service_name: this.answers["apprunner_service_name"],
            existing_project_group: this.answers["existing_project_group"],
        };

        this.fs.copyTpl(
            this.templatePath('.github/workflows/app-runner.yaml'),
            this.destinationPath('.github/workflows/' + this.answers["cloudformation_stack_name"] + '.yaml'),
            options
        );

        this.fs.copyTpl(
            this.templatePath('github/app-runner/action.yaml'),
            this.destinationPath('github/' + this.answers["cloudformation_stack_name"] + '/action.yaml'),
            options
        );

        this.fs.copyTpl(
            this.templatePath('terraform/app-runner/*.tf'),
            this.destinationPath('terraform/' + this.answers["cloudformation_stack_name"]),
            options,
            null,
            { globOptions: { dot: true } }
        );

        this.fs.copyTpl(
            this.templatePath('bash/app-runner/*.sh'),
            this.destinationPath('bash/' + this.answers["cloudformation_stack_name"]),
            options,
            null,
            { globOptions: { dot: true } }
        );

        this.fs.copyTpl(
            this.templatePath('cloudformation/app-runner/*.yml'),
            this.destinationPath('cloudformation/' + this.answers["cloudformation_stack_name"]),
            options,
            null,
            { globOptions: { dot: true } }
        );
    }
};
