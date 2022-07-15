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
    }

    install() {

    }
};
