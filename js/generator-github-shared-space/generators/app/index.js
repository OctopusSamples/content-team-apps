const Generator = require('yeoman-generator');
module.exports = class extends Generator {
    writing() {
        this.fs.copyTpl(
            this.templatePath('github/action.yaml'),
            this.destinationPath('github/action.yaml'),
            { }
        );

        this.fs.copyTpl(
            this.templatePath('octopus/*.tf'),
            this.destinationPath('octopus/*.tf'),
            { s3_bucket_suffix: crypto.randomUUID()}
        );
    }
};
