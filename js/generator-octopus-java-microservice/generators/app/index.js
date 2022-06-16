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
            this.templatePath('java/**/*'),
            this.destinationPath('java'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            },
            null,
            {globOptions: {ignore: ["**/target", "**/*.iml", "**/.idea", "**/.gitignorefile"], dot: true}}
        );

        this.fs.copyTpl(
            this.templatePath('.vscode/*'),
            this.destinationPath('.vscode'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            },
            null,
            {globOptions: {dot: true}}
        );

        this.fs.copyTpl(
            this.templatePath('github/java-microservice/action.yaml'),
            this.destinationPath('github/java-microservice/action.yaml'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            }
        )

        this.fs.copyTpl(
            this.templatePath('.devcontainer/java/devcontainer.json'),
            this.destinationPath('.devcontainer/java/devcontainer.json'),
            {
                s3_bucket_suffix: this.options["s3_bucket_suffix"],
                aws_state_bucket_region: this.options["aws_state_bucket_region"],
                aws_region: this.options["aws_region"]
            }
        )


        this.fs.copyTpl(
            this.templatePath('.devcontainer/java/Dockerfile'),
            this.destinationPath('.devcontainer/java/Dockerfile'),
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
            this.templatePath('java/.gitignorefile'),
            this.destinationPath(`java/.gitignore`),
            null
        );

        this.fs.copyTpl(
            this.templatePath('java/products-microservice/.gitignorefile'),
            this.destinationPath(`java/products-microservice/.gitignore`),
            null
        );

        this.fs.copyTpl(
            this.templatePath('java/microservice-utils/.gitignorefile'),
            this.destinationPath(`java/microservice-utils/.gitignore`),
            null
        );
    }
};
