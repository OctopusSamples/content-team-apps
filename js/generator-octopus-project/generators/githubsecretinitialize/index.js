const Generator = require('yeoman-generator');

module.exports = class extends Generator {
    async prompting() {

    }

    writing() {
        const options = {

        };

        this.fs.copyTpl(
            this.templatePath('terraform/githubsecretinitialize/*.tf'),
            this.destinationPath('terraform/githubsecretinitialize'),
            options,
            null,
            { globOptions: { dot: true } }
        );

        this.fs.copyTpl(
            this.templatePath('terraform/githubsecretinitialize/.gitignorefile'),
            this.destinationPath(`terraform/githubsecretinitialize/.gitignore`),
            null
        );

        this.fs.copyTpl(
            this.templatePath('terraform/githubsecretinitialize/secrets.tfvars.template'),
            this.destinationPath(`terraform/githubsecretinitialize/secrets.tfvars.template`),
            null
        );

        this.fs.copyTpl(
            this.templatePath('terraform/githubsecretinitialize/README.md'),
            this.destinationPath(`terraform/githubsecretinitialize/README.md`),
            null
        );
    }

    install() {

    }
};
