const Generator = require('yeoman-generator');
module.exports = class extends Generator {
    constructor(args, opts) {
        super(args, opts);

        this.option("aws_region", {type: String});
        this.option("s3_bucket_suffix", {type: String});
        this.option("aws_state_bucket_region", {type: String});
        this.option("platform", {type: String});
        this.option("framework", {type: String});
        this.option("repository", {type: String});
    }

    writing() {
        this.fs.copyTpl(
            this.templatePath('github/ecr-feed/action.yaml'),
            this.destinationPath('github/ecr-feed/action.yaml'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"],
                repository: this.options["repository"]
            }
        );

        this.fs.copyTpl(
            this.templatePath('terraform/ecr-feed/*.tf'),
            this.destinationPath('terraform/ecr-feed'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"],
                repository: this.options["repository"]
            },
            null,
            { globOptions: { dot: true } }
        );
    }
};
