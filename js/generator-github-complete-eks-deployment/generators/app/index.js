const crypto = require('crypto');
const Generator = require('yeoman-generator');

module.exports = class extends Generator {
    constructor(args, opts) {
        super(args, opts);

        this.s3_bucket_suffix = crypto.randomUUID();
        this.option("aws_region", {type: String});
        this.option("octopus_user_id", {type: String});
    }

    initializing() {
        this.composeWith(
            require.resolve('generator-github-shared-space/generators/app'),
            {
                s3_bucket_suffix: this.s3_bucket_suffix,
                aws_region: this.options["awsRegion"]
            });
    }

    writing() {
        this.fs.copyTpl(
            this.templatePath('.github/workflows/eks-deployment.yaml'),
            this.destinationPath('.github/workflows/eks-deployment.yaml'),
            {
                octopus_project: "Octo App Builder",
                octopus_user_id: this.options["octopusUserId"],
                s3_bucket_suffix: this.s3_bucket_suffix,
                aws_region: this.options["awsRegion"]
            }
        );
    }
};
