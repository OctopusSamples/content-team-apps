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
            this.templatePath('js/**/*'),
            this.destinationPath('js'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            },
            null,
            {globOptions: {ignore: ["**/node_modules", "**/build", "**/*.iml", "**/.idea"]}}
        );

        this.fs.copyTpl(
            this.templatePath('github/js-frontend/action.yaml'),
            this.destinationPath('github/js-frontend/action.yaml'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            }
        )
    }
};
