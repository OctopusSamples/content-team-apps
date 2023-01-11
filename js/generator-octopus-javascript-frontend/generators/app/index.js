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
            {globOptions: {ignore: ["**/node_modules", "**/build", "**/*.iml", "**/.idea", "**/.serverless", "**/*.zip"]}}
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

        this.fs.copyTpl(
            this.templatePath('azure-devops/**/*'),
            this.destinationPath('azure-devops'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            },
            null,
            {globOptions: {dot: true}}
        );

        this.fs.copyTpl(
            this.templatePath('jenkins/**/*'),
            this.destinationPath('jenkins'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            },
            null,
            {globOptions: {dot: true}}
        );

        this.fs.copyTpl(
            this.templatePath('.devcontainer/js/devcontainer.json'),
            this.destinationPath('.devcontainer/js/devcontainer.json'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            }
        )

        this.fs.copyTpl(
            this.templatePath('.devcontainer/js/Dockerfile'),
            this.destinationPath('.devcontainer/js/Dockerfile'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            }
        )
    }
};
