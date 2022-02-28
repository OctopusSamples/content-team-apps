const Generator = require('yeoman-generator');
module.exports = class extends Generator {
    initializing() {
        this.composeWith(require.resolve('generator-github-shared-space/generators/app'));
    }

    writing() {
        this.fs.copyTpl(
            this.templatePath('.github/workflows/eks-deployment.yaml'),
            this.destinationPath('.github/workflows/eks-deployment.yaml'),
            { }
        );
    }
};
