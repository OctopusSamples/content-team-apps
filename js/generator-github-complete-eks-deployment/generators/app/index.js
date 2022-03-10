const crypto = require('crypto');
const Generator = require('yeoman-generator');

module.exports = class extends Generator {
    constructor(args, opts) {
        super(args, opts);

        this.option("aws_region", {type: String});
        this.option("aws_state_bucket_region", {type: String});
        this.option("octopus_user_id", {type: String});
        this.option("s3_bucket_suffix", {type: String, default: crypto.randomUUID()});
    }

    initializing() {
        const awsRegion = this.options["awsRegion"];
        const awsStateBucketRegion = this.options["awsStateBucketRegion"] || awsRegion;
        const s3BucketSuffix = this.options["s3BucketSuffix"];
        const args = {
            s3_bucket_suffix: s3BucketSuffix,
            aws_state_bucket_region: awsStateBucketRegion,
            aws_region: awsRegion
        };

        this.composeWith(
            require.resolve('@octopus-content-team/generator-octopus-java-microservice/generators/app'), args);
        this.composeWith(
            require.resolve('@octopus-content-team/generator-github-shared-space/generators/app'), args);
        this.composeWith(
            require.resolve('@octopus-content-team/generator-github-shared-infrastructure/generators/app'), args);
        this.composeWith(
            require.resolve('@octopus-content-team/generator-github-kubernetes-deployment/generators/app'), args);
        this.composeWith(
            require.resolve('@octopus-content-team/generator-aws-ecr/generators/app'), args);
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
