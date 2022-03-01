const crypto = require('crypto');
const Generator = require('yeoman-generator');

module.exports = class extends Generator {
    constructor(args, opts) {
        super(args, opts);

        this.option("aws_region", {type: String});
        this.option("octopus_user_id", {type: String});
        this.option("s3_bucket_suffix", {type: String, default: crypto.randomUUID()});
    }

    initializing() {
        this.composeWith(
            require.resolve('../../../generator-github-shared-space/generators/app'),
            {
                s3_bucket_suffix: this.options["s3BucketSuffix"],
                aws_region: this.options["awsRegion"]
            });
        this.composeWith(
            require.resolve('../../../generator-github-shared-infrastructure/generators/app'),
            {
                s3_bucket_suffix: this.options["s3BucketSuffix"],
                aws_region: this.options["awsRegion"]
            });
        this.composeWith(
            require.resolve('../../../generator-octopus-java-microservice/generators/app'),
            {});
    }

    writing() {
        this.fs.copyTpl(
            this.templatePath('.github/workflows/eks-deployment.yaml'),
            this.destinationPath('.github/workflows/eks-deployment.yaml'),
            {
                octopus_space: "Octo App Builder",
                octopus_user_id: this.options["octopusUserId"],
                s3_bucket_suffix: this.options["s3BucketSuffix"],
                aws_region: this.options["awsRegion"]
            }
        );
    }
};
