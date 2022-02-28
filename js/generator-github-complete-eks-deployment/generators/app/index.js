const crypto = require('crypto');
const Generator = require('yeoman-generator');

module.exports = class extends Generator {
    constructor(args, opts) {
        super(args, opts);

        this.s3_bucket_suffix = crypto.randomUUID();
        this.option("aws_region", {type: String});
    }

    initializing() {
        this.composeWith(
            require.resolve('generator-github-shared-space/generators/app'),
            {
                s3_bucket_suffix: this.s3_bucket_suffix,
                aws_region: this.options.aws_region
            });
    }

    writing() {
        this.fs.copyTpl(
            this.templatePath('.github/workflows/eks-deployment.yaml'),
            this.destinationPath('.github/workflows/eks-deployment.yaml'),
            {
                octopus_project: "Octo App Builder",
                s3_bucket_suffix: this.s3_bucket_suffix,
                aws_region: this.options.aws_region
            }
        );
    }
};
