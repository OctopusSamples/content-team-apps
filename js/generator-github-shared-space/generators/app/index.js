const Generator = require('yeoman-generator');
module.exports = class extends Generator {
    constructor(args, opts) {
        super(args, opts);

        this.option("s3_bucket_suffix", {type: String});
        this.option("aws_region", {type: String});
    }

    writing() {
        console.log(Object.keys(this.options))
        this.fs.copyTpl(
            this.templatePath('github/shared-space/action.yaml'),
            this.destinationPath('github/shared-space/action.yaml'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_region: this.options["aws_region"]
            }
        );

        this.fs.copyTpl(
            this.templatePath('octopus/*.tf'),
            this.destinationPath('octopus'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_region: this.options["aws_region"]
            },
            null,
            { globOptions: { dot: true } }
        );
    }
};
