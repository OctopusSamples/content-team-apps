const crypto = require('crypto');
const Generator = require('yeoman-generator');

module.exports = class extends Generator {
    constructor(args, opts) {
        super(args, opts);

        this.option("aws_region", {type: String});
        this.option("aws_state_bucket_region", {type: String});
        this.option("platform", {type: String});
        this.option("framework", {
            type: String,
            description: "An optional field that defines the framework or language i.e. Spring, DotNET, Express etc. If defined, the framework is included the registry and Docker image names."
        });
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

        this.composeWith(require.resolve('@octopus-content-team/generator-octopus-java-microservice/generators/app'), args);
        this.composeWith(require.resolve('@octopus-content-team/generator-octopus-js-frontend/generators/app'), args);
        this.composeWith(require.resolve('@octopus-content-team/generator-github-shared-space/generators/app'), args);
        this.composeWith(require.resolve('@octopus-content-team/generator-github-shared-infrastructure/generators/app'), args);
        this.composeWith(require.resolve('@octopus-content-team/generator-github-ecs-deployment/generators/app'), args);
        this.composeWith(require.resolve('@octopus-content-team/generator-ecr-feed/generators/app'), args);
        this.composeWith(require.resolve('@octopus-content-team/generator-aws-ecr/generators/app'), {...args, repository: "products-service"});
        this.composeWith(require.resolve('@octopus-content-team/generator-aws-ecr/generators/app'), {...args, repository: "octopus-frontend"});
        this.composeWith(require.resolve('@octopus-content-team/generator-aws-ecr/generators/app'), {...args, repository: "postman-worker"});
        this.composeWith(require.resolve('@octopus-content-team/generator-aws-ecr/generators/app'), {...args, repository: "cypress-worker"});
    }

    writing() {
        const framework = this.options["framework"];
        const platform = this.options["platform"];
        const spaceName = platform + (framework ? " " + framework : "");
        const productsRepository = "products-service" + (framework ? "-" + framework : "");
        const frontendRepository = "octopus-frontend" + (framework ? "-" + framework : "");
        const postmanRepository = "postman-worker" + (framework ? "-" + framework : "");
        const cypressRepository = "cypress-worker" + (framework ? "-" + framework : "");

        this.fs.copyTpl(
            this.templatePath('.github/workflows/ecs-deployment.yaml'),
            this.destinationPath('.github/workflows/ecs-deployment.yaml'),
            {
                octopus_space: spaceName,
                s3_bucket_suffix: this.options["s3BucketSuffix"],
                aws_region: this.options["awsRegion"],
                framework,
                platform,
                products_repository: productsRepository,
                frontend_repository: frontendRepository,
                postman_repository: postmanRepository,
                cypress_repository: cypressRepository
            }
        );
    }
};
