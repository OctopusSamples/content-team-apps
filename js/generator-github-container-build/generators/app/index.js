const Generator = require('yeoman-generator');
module.exports = class extends Generator {
    writing() {
        this.fs.copyTpl(
            this.templatePath('.github/workflows/container.yaml'),
            this.destinationPath('.github/workflows/container.yaml'),
            { }
        );
    }
};
