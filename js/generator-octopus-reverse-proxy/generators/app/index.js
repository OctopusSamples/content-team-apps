const Generator = require('yeoman-generator');

module.exports = class extends Generator {
    constructor(args, opts) {
        super(args, opts);

        this.option("s3_bucket_suffix", {type: String});
        this.option("aws_state_bucket_region", {type: String});
        this.option("aws_region", {type: String});
    }

    writing() {
        this.fs.copyTpl(
            this.templatePath('golang/**/*'),
            this.destinationPath('golang'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            },
            null,
            {globOptions: {ignore: ["**/*.iml", "**/.idea", "**/.gitignorefile", "**/*.exe", "**/main"], dot: true}}
        );

        this.fs.copyTpl(
            this.templatePath('github/golang-reverse-proxy/action.yaml'),
            this.destinationPath('github/golang-reverse-proxy/action.yaml'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            }
        )

        this.fs.copyTpl(
            this.templatePath('.devcontainer/golang/devcontainer.json'),
            this.destinationPath('.devcontainer/golang/devcontainer.json'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            }
        )

        this.fs.copyTpl(
            this.templatePath('.devcontainer/golang/Dockerfile'),
            this.destinationPath('.devcontainer/golang/Dockerfile'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            }
        )

        /*
            npm publish will remove .gitignore files, but we need them in the resulting template. So each .gitignore file
            has a copy called .gitignorefile, which is copied to the correct destination.
            See https://github.com/Polymer/tools/issues/2324 and https://github.com/yeoman/generator/issues/812 for details.
         */

        this.fs.copyTpl(
            this.templatePath('golang/reverse-proxy/.gitignorefile'),
            this.destinationPath(`golang/reverse-proxy/.gitignore`),
            null
        );
    }
};
