const Generator = require('yeoman-generator');
module.exports = class extends Generator {
    writing() {
        this.fs.copyTpl(
            this.templatePath('github/kubernetes-deployment/action.yaml'),
            this.destinationPath('github/kubernetes-deployment/action.yaml'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            }
        );

        this.fs.copyTpl(
            this.templatePath('terraform/kubernetes-deployment/*.tf'),
            this.destinationPath('terraform/kubernetes-deployment'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            },
            null,
            { globOptions: { dot: true } }
        );
    }
};
