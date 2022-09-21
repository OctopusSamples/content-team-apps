const Generator = require('yeoman-generator');

module.exports = class extends Generator {
    async prompting() {
        this.answers = await this.prompt([
            {
                type: "input",
                name: "project_name",
                message: "The Project Name",
                default: "octopusdockerfeed"
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
                name: "github_app_id",
                message: "The GitHub App ID used by the Github Terraform provider",
                default: "${{ secrets.GH_APP_ID }}"
            },
            {
                type: "input",
                name: "github_installation_id",
                message: "The GitHub Installation ID used by the Github Terraform provider",
                default: "${{ secrets.GH_INSTALLATION_ID }}"
            },
            {
                type: "input",
                name: "github_pem_file",
                message: "The base 64 encoded pem file used by Github Terraform provider",
                default: "${{ secrets.GH_PEM_FILE }}"
            },
            {
                type: "input",
                name: "dockerhub_username",
                message: "The DockerHub username.",
                default: "${{ secrets.DOCKERHUB_USERNAME }}"
            },
            {
                type: "password",
                name: "dockerhub_password",
                message: "The DockerHub password.",
                default: "${{ secrets.DOCKERHUB_PASSWORD }}"
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
            dockerhub_username: this.answers["dockerhub_username"],
            dockerhub_password: this.answers["dockerhub_password"],
            aws_region: this.answers["aws_region"],
            github_app_id: this.answers["github_app_id"],
            github_installation_id: this.answers["github_installation_id"],
            github_pem_file: this.answers["github_pem_file"],
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
