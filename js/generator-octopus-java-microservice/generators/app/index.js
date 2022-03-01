const crypto = require('crypto');
const Generator = require('yeoman-generator');

module.exports = class extends Generator {
    constructor(args, opts) {
        super(args, opts);
    }

    initializing() {

    }

    writing() {
        this.fs.copyTpl(
            this.templatePath('**/*'),
            this.destinationRoot(),
            {},
            {},
            {globOptions: {ignore: ["**/target", "**/*.iml", "**/.idea"]}}
        )
    }
};
