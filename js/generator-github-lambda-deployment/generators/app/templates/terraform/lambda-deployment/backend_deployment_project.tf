resource "octopusdeploy_project" "deploy_backend_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the backend service to Lambda."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = "Backend Service"
  project_group_id                     = octopusdeploy_project_group.backend_project_group.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  included_library_variable_sets       = []
  versioning_strategy {
    template = "#{Octopus.Version.LastMajor}.#{Octopus.Version.LastMinor}.#{Octopus.Version.LastPatch}.#{Octopus.Version.NextRevision}"
  }

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

output "deploy_backend_project_id" {
  value = octopusdeploy_project.deploy_backend_project.id
}

resource "octopusdeploy_variable" "debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "aws_account_deploy_backend_project" {
  name     = "AWS Account"
  type     = "AmazonWebServicesAccount"
  value    = var.octopus_aws_account_id
  owner_id = octopusdeploy_project.deploy_backend_project.id
}

locals {
  # The environment name up to the first space and lowercase
  fixed_environment           = "#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}"
  mainline_s3_bucket_stack    = "OctopusBuilder-Lambda-S3Bucket-${lower(var.github_repo_owner)}-${local.fixed_environment}"
  api_gateway_stage_stack     = "OctopusBuilder-APIGateway-Stage-${lower(var.github_repo_owner)}-${local.fixed_environment}"
  product_api_gateway_stack   = "OctopusBuilder-Product-APIGateway-${lower(var.github_repo_owner)}-${local.fixed_environment}"
  product_stack               = "OctopusBuilder-Product-${lower(var.github_repo_owner)}-${local.fixed_environment}"
  product_proxy_stack         = "OctopusBuilder-Product-Proxy-${lower(var.github_repo_owner)}-${local.fixed_environment}"
  product_lambda_name         = "Product-${lower(var.github_repo_owner)}-${local.fixed_environment}"
  product_version_stack       = "${local.product_stack}-#{Octopus.Deployment.Id | Replace -}"
  product_proxy_version_stack = "${local.product_version_stack}-#{Octopus.Deployment.Id | Replace -}"
  products_package            = "products-microservice-lambda"
  products_sbom_package       = "products-microservice-sbom"
  reverse_proxy_package       = "reverse-proxy-lambda"
  product_cloudformation_tags = jsonencode([
    {
      key : "Environment"
      value : "#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}"
    },
    {
      key : "DeploymentProject"
      value : "Backend_Service"
    }
  ])
  product_cloudformation_transient_tags = jsonencode([
    {
      key : "OctopusTransient"
      value : "True"
    },
    {
      key : "OctopusTenantId"
      value : "#{if Octopus.Deployment.Tenant.Id}#{Octopus.Deployment.Tenant.Id}#{/if}#{unless Octopus.Deployment.Tenant.Id}untenanted#{/unless}"
    },
    {
      key : "OctopusStepId"
      value : "#{Octopus.Step.Id}"
    },
    {
      key : "OctopusRunbookRunId"
      value : "#{if Octopus.RunBookRun.Id}#{Octopus.RunBookRun.Id}#{/if}#{unless Octopus.RunBookRun.Id}none#{/unless}"
    },
    {
      key : "OctopusDeploymentId"
      value : "#{if Octopus.Deployment.Id}#{Octopus.Deployment.Id}#{/if}#{unless Octopus.Deployment.Id}none#{/unless}"
    },
    {
      key : "OctopusProjectId"
      value : "#{Octopus.Project.Id}"
    },
    {
      key : "OctopusEnvironmentId"
      value : "#{Octopus.Environment.Id}"
    },
    {
      key : "Environment"
      value : "#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}"
    },
    {
      key : "DeploymentProject"
      value : "Backend_Service"
    }
  ])
}

resource "octopusdeploy_deployment_process" "deploy_backend" {
  project_id = octopusdeploy_project.deploy_backend_project.id

  step {
    condition           = "Success"
    name                = "Create S3 bucket"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Create S3 bucket"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.product_cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.mainline_s3_bucket_stack
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          Resources:
            LambdaS3Bucket:
              Type: 'AWS::S3::Bucket'
          Outputs:
            LambdaS3Bucket:
              Description: The S3 Bucket
              Value:
                Ref: LambdaS3Bucket
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[]"
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Upload Lambda"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsUploadS3"
      name           = "Upload Lambda"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      primary_package {
        acquisition_location = "Server"
        feed_id              = var.octopus_built_in_feed_id
        package_id           = local.products_package
        properties           = {
          "SelectionMode" : "immediate"
        }
      }

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.S3.BucketName" : "#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}"
        "Octopus.Action.Aws.S3.PackageOptions" : "{\"bucketKey\":\"\",\"bucketKeyBehaviour\":\"Filename\",\"bucketKeyPrefix\":\"\",\"storageClass\":\"STANDARD\",\"cannedAcl\":\"private\",\"metadata\":[],\"tags\":[]}"
        "Octopus.Action.Aws.S3.TargetMode" : "EntirePackage"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
        "Octopus.Action.Package.DownloadOnTentacle" : "False"
        "Octopus.Action.Package.FeedId" : var.octopus_built_in_feed_id
        "Octopus.Action.Package.PackageId" : local.products_package
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Upload Lambda Proxy"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsUploadS3"
      name           = "Upload Lambda Proxy"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      primary_package {
        acquisition_location = "Server"
        feed_id              = var.octopus_built_in_feed_id
        package_id           = local.reverse_proxy_package
        properties           = {
          "SelectionMode" : "immediate"
        }
      }

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.S3.BucketName" : "#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}"
        "Octopus.Action.Aws.S3.PackageOptions" : "{\"bucketKey\":\"\",\"bucketKeyBehaviour\":\"Filename\",\"bucketKeyPrefix\":\"\",\"storageClass\":\"STANDARD\",\"cannedAcl\":\"private\",\"metadata\":[],\"tags\":[]}"
        "Octopus.Action.Aws.S3.TargetMode" : "EntirePackage"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
        "Octopus.Action.Package.DownloadOnTentacle" : "False"
        "Octopus.Action.Package.FeedId" : var.octopus_built_in_feed_id
        "Octopus.Action.Package.PackageId" : local.reverse_proxy_package
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Get Stack Outputs"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Get Stack Outputs"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          echo "Downloading Docker images"

          echo "##octopus[stdout-verbose]"

          docker pull amazon/aws-cli 2>&1

          # Alias the docker run commands
          shopt -s expand_aliases
          alias aws="docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli"

          echo "##octopus[stdout-default]"

          API_RESOURCE=$(aws cloudformation \
              describe-stacks \
              --stack-name ${local.api_gateway_stack} \
              --query "Stacks[0].Outputs[?OutputKey=='Api'].OutputValue" \
              --output text)

          set_octopusvariable "Api" $${API_RESOURCE}

          echo "API Resource ID: $${API_RESOURCE}"

          if [[ -z "$${API_RESOURCE}" ]]; then
            echo "Run the API Gateway project first"
            exit 1
          fi

          REST_API=$(aws cloudformation \
              describe-stacks \
              --stack-name ${local.api_gateway_stack} \
              --query "Stacks[0].Outputs[?OutputKey=='RestApi'].OutputValue" \
              --output text)

          set_octopusvariable "RestApi" $${REST_API}

          echo "Rest Api ID: $${REST_API}"

          if [[ -z "$${REST_API}" ]]; then
            echo "Run the API Gateway project first"
            exit 1
          fi
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Deploy Application Lambda"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy Application Lambda"
      notes          = "To achieve zero downtime deployments, we must deploy Lambdas and their versions in separate stacks. This stack deploys the main application Lambda."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.product_cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.product_stack
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          # This stack creates a new application lambda.
          Parameters:
            EnvironmentName:
              Type: String
              Default: '#{Octopus.Environment.Name}'
            RestApi:
              Type: String
            ResourceId:
              Type: String
            LambdaS3Key:
              Type: String
            LambdaS3Bucket:
              Type: String
            LambdaName:
              Type: String
            LambdaDescription:
              Type: String
            DBUsername:
              Type: String
            DBPassword:
              Type: String
          Resources:
            VPC:
              Type: "AWS::EC2::VPC"
              Properties:
                CidrBlock: "10.0.0.0/16"
                Tags:
                - Key: "Name"
                  Value: !Ref LambdaName
            SubnetA:
              Type: "AWS::EC2::Subnet"
              Properties:
                AvailabilityZone: !Select
                  - 0
                  - !GetAZs
                    Ref: 'AWS::Region'
                VpcId: !Ref "VPC"
                CidrBlock: "10.0.0.0/24"
            SubnetB:
              Type: "AWS::EC2::Subnet"
              Properties:
                AvailabilityZone: !Select
                  - 1
                  - !GetAZs
                    Ref: 'AWS::Region'
                VpcId: !Ref "VPC"
                CidrBlock: "10.0.1.0/24"
            RouteTable:
              Type: "AWS::EC2::RouteTable"
              Properties:
                VpcId: !Ref "VPC"
            SubnetGroup:
              Type: "AWS::RDS::DBSubnetGroup"
              Properties:
                DBSubnetGroupName: "subnetgroup"
                DBSubnetGroupDescription: "Subnet Group"
                SubnetIds:
                - !Ref "SubnetA"
                - !Ref "SubnetB"
            InstanceSecurityGroup:
              Type: "AWS::EC2::SecurityGroup"
              Properties:
                GroupName: "Example Security Group"
                GroupDescription: "RDS traffic"
                VpcId: !Ref "VPC"
                SecurityGroupEgress:
                - IpProtocol: "-1"
                  CidrIp: "0.0.0.0/0"
            InstanceSecurityGroupIngress:
              Type: "AWS::EC2::SecurityGroupIngress"
              DependsOn: "InstanceSecurityGroup"
              Properties:
                GroupId: !Ref "InstanceSecurityGroup"
                IpProtocol: "tcp"
                FromPort: "0"
                ToPort: "65535"
                SourceSecurityGroupId: !Ref "InstanceSecurityGroup"
            RDSCluster:
              Type: "AWS::RDS::DBCluster"
              Properties:
                DBSubnetGroupName: !Ref "SubnetGroup"
                MasterUsername: !Ref "DBUsername"
                MasterUserPassword: !Ref "DBPassword"
                DatabaseName: "products"
                Engine: "aurora-mysql"
                EngineMode: "serverless"
                VpcSecurityGroupIds:
                - !Ref "InstanceSecurityGroup"
                ScalingConfiguration:
                  AutoPause: true
                  MaxCapacity: 1
                  MinCapacity: 1
                  SecondsUntilAutoPause: 300
            AppLogGroup:
              Type: 'AWS::Logs::LogGroup'
              Properties:
                LogGroupName: !Sub '/aws/lambda/$${EnvironmentName}-$${LambdaName}'
                RetentionInDays: 14
            IamRoleLambdaExecution:
              Type: 'AWS::IAM::Role'
              Properties:
                AssumeRolePolicyDocument:
                  Version: 2012-10-17
                  Statement:
                    - Effect: Allow
                      Principal:
                        Service:
                          - lambda.amazonaws.com
                      Action:
                        - 'sts:AssumeRole'
                Policies:
                  - PolicyName: !Sub '$${EnvironmentName}-$${LambdaName}-policy'
                    PolicyDocument:
                      Version: 2012-10-17
                      Statement:
                        - Effect: Allow
                          Action:
                            - 'logs:CreateLogStream'
                            - 'logs:CreateLogGroup'
                            - 'logs:PutLogEvents'
                          Resource:
                            - !Sub >-
                              arn:$${AWS::Partition}:logs:$${AWS::Region}:$${AWS::AccountId}:log-group:/aws/lambda/$${EnvironmentName}-$${LambdaName}*:*
                        - Effect: Allow
                          Action:
                            - 'ec2:DescribeInstances'
                            - 'ec2:CreateNetworkInterface'
                            - 'ec2:AttachNetworkInterface'
                            - 'ec2:DeleteNetworkInterface'
                            - 'ec2:DescribeNetworkInterfaces'
                          Resource: "*"
                Path: /
                RoleName: !Sub '$${EnvironmentName}-$${LambdaName}-role'
            MigrationLambda:
              Type: 'AWS::Lambda::Function'
              Properties:
                Description: !Ref LambdaDescription
                Code:
                  S3Bucket: !Ref LambdaS3Bucket
                  S3Key: !Ref LambdaS3Key
                Environment:
                  Variables:
                    DATABASE_HOSTNAME: !GetAtt
                    - RDSCluster
                    - Endpoint.Address
                    DATABASE_USERNAME: !Ref "DBUsername"
                    DATABASE_PASSWORD: !Ref "DBPassword"
                    MIGRATE_AT_START: !!str "false"
                    LAMBDA_NAME: "DatabaseInit"
                    QUARKUS_PROFILE: "faas"
                FunctionName: !Sub '$${EnvironmentName}-$${LambdaName}-DBMigration'
                Handler: not.used.in.provided.runtime
                MemorySize: 256
                PackageType: Zip
                Role: !GetAtt
                  - IamRoleLambdaExecution
                  - Arn
                Runtime: provided
                Timeout: 600
                VpcConfig:
                  SecurityGroupIds:
                    - !Ref "InstanceSecurityGroup"
                  SubnetIds:
                    - !Ref "SubnetA"
                    - !Ref "SubnetB"
            ApplicationLambda:
              Type: 'AWS::Lambda::Function'
              Properties:
                Description: !Ref LambdaDescription
                Code:
                  S3Bucket: !Ref LambdaS3Bucket
                  S3Key: !Ref LambdaS3Key
                Environment:
                  Variables:
                    DATABASE_HOSTNAME: !GetAtt
                    - RDSCluster
                    - Endpoint.Address
                    DATABASE_USERNAME: !Ref "DBUsername"
                    DATABASE_PASSWORD: !Ref "DBPassword"
                    MIGRATE_AT_START: !!str "false"
                    QUARKUS_PROFILE: "faas"
                FunctionName: !Sub '$${EnvironmentName}-$${LambdaName}'
                Handler: not.used.in.provided.runtime
                MemorySize: 256
                PackageType: Zip
                Role: !GetAtt
                  - IamRoleLambdaExecution
                  - Arn
                Runtime: provided
                Timeout: 600
                VpcConfig:
                  SecurityGroupIds:
                    - !Ref "InstanceSecurityGroup"
                  SubnetIds:
                    - !Ref "SubnetA"
                    - !Ref "SubnetB"
          Outputs:
            ApplicationLambda:
              Description: The Lambda ref
              Value: !Ref ApplicationLambda
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : "#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}"
          },
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "ResourceId"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.Api}"
          },
          {
            ParameterKey : "LambdaS3Key"
            ParameterValue : "#{Octopus.Action[Upload Lambda].Package[].PackageId}.#{Octopus.Action[Upload Lambda].Package[].PackageVersion}.zip"
          },
          {
            ParameterKey : "LambdaS3Bucket"
            ParameterValue : "#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}"
          },
          {
            ParameterKey : "LambdaName"
            ParameterValue : local.product_lambda_name
          },
          {
            ParameterKey : "LambdaDescription"
            ParameterValue : "#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}"
          },
          {
            ParameterKey : "DBUsername"
            ParameterValue : "productadmin"
          },
          {
            ParameterKey : "DBPassword"
            ParameterValue : "Password01!"
          }
        ])
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : "#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}"
          },
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "ResourceId"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.Api}"
          },
          {
            ParameterKey : "LambdaS3Key"
            ParameterValue : "#{Octopus.Action[Upload Lambda].Package[].PackageId}.#{Octopus.Action[Upload Lambda].Package[].PackageVersion}.zip"
          },
          {
            ParameterKey : "LambdaS3Bucket"
            ParameterValue : "#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}"
          },
          {
            ParameterKey : "LambdaName"
            ParameterValue : local.product_lambda_name
          },
          {
            ParameterKey : "LambdaDescription"
            ParameterValue : "#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}"
          },
          {
            ParameterKey : "DBUsername"
            ParameterValue : "productadmin"
          },
          {
            ParameterKey : "DBPassword"
            ParameterValue : "Password01!"
          }
        ])
        "Octopus.Action.Aws.IamCapabilities" : jsonencode([
          "CAPABILITY_AUTO_EXPAND", "CAPABILITY_IAM", "CAPABILITY_NAMED_IAM"
        ])
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Run Database Migrations"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Run Database Migrations"
      notes          = "Run the Lambda that performs database migrations."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          echo "Downloading Docker images"

          echo "##octopus[stdout-verbose]"

          docker pull amazon/aws-cli 2>&1

          # Alias the docker run commands
          shopt -s expand_aliases
          alias aws="docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli"

          echo "##octopus[stdout-default]"

          aws lambda invoke \
            --function-name '#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}-${local.product_lambda_name}-DBMigration' \
            --payload '{}' \
            response.json
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Deploy Application Lambda Version"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy Application Lambda Version"
      notes          = "Stacks deploying Lambda versions must have unique names to ensure a new version is created each time. This step deploys a uniquely names stack creating a version of the Lambda deployed in the last step."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.product_cloudformation_transient_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.product_version_stack
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          # This template creates a new lambda version for the application lambda created in the
          # previous step. This template is created in a unique stack each time, and is cleaned
          # up by Octopus once the API gateway no longer points to this version.
          Parameters:
            RestApi:
              Type: String
            LambdaDescription:
              Type: String
            ApplicationLambda:
              Type: String
          Resources:
            LambdaVersion:
              Type: 'AWS::Lambda::Version'
              Properties:
                FunctionName: !Ref ApplicationLambda
                Description: !Ref LambdaDescription
            ApplicationLambdaPermissions:
              Type: 'AWS::Lambda::Permission'
              Properties:
                FunctionName: !Ref LambdaVersion
                Action: 'lambda:InvokeFunction'
                Principal: apigateway.amazonaws.com
                SourceArn: !Join
                  - ''
                  - - 'arn:'
                    - !Ref 'AWS::Partition'
                    - ':execute-api:'
                    - !Ref 'AWS::Region'
                    - ':'
                    - !Ref 'AWS::AccountId'
                    - ':'
                    - !Ref RestApi
                    - /*/*
          Outputs:
            LambdaVersion:
              Description: The name of the Lambda version resource deployed by this template
              Value: !Ref LambdaVersion
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : jsonencode([
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "LambdaDescription"
            ParameterValue : "#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}"
          },
          {
            ParameterKey : "ApplicationLambda"
            ParameterValue : "#{Octopus.Action[Deploy Application Lambda].Output.AwsOutputs[ApplicationLambda]}"
          }
        ])
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : jsonencode([
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "LambdaDescription"
            ParameterValue : "#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}"
          },
          {
            ParameterKey : "ApplicationLambda"
            ParameterValue : "#{Octopus.Action[Deploy Application Lambda].Output.AwsOutputs[ApplicationLambda]}"
          }
        ])
        "Octopus.Action.Aws.IamCapabilities" : jsonencode([
          "CAPABILITY_AUTO_EXPAND", "CAPABILITY_IAM", "CAPABILITY_NAMED_IAM"
        ])
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Deploy Reverse Proxy Lambda"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy Reverse Proxy Lambda"
      notes          = "To allow us to debug applications locally and deploy feature branches, each Lambda is exposed by a reverse proxy that can redirect requests to another Lambda or URL. This step deploys the reverse proxy."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.product_cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.product_proxy_stack
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          # This template creates the reverse proxy lambda, pointing it to the application lambda
          # created in previous steps.
          Parameters:
            EnvironmentName:
              Type: String
              Default: '#{Octopus.Environment.Name}'
            RestApi:
              Type: String
            ProxyLambdaS3Key:
              Type: String
            LambdaS3Bucket:
              Type: String
            LambdaName:
              Type: String
            LambdaDescription:
              Type: String
            LambdaVersion:
              Type: String
          Resources:
            AppLogGroupProxy:
              Type: 'AWS::Logs::LogGroup'
              Properties:
                LogGroupName: !Sub '/aws/lambda/$${EnvironmentName}-$${LambdaName}-Proxy'
                RetentionInDays: 14
            IamRoleProxyLambdaExecution:
              Type: 'AWS::IAM::Role'
              Properties:
                AssumeRolePolicyDocument:
                  Version: 2012-10-17
                  Statement:
                    - Effect: Allow
                      Principal:
                        Service:
                          - lambda.amazonaws.com
                      Action:
                        - 'sts:AssumeRole'
                Policies:
                  - PolicyName: !Sub '$${EnvironmentName}-$${LambdaName}-Proxy-policy'
                    PolicyDocument:
                      Version: 2012-10-17
                      Statement:
                        - Effect: Allow
                          Action:
                            - 'logs:CreateLogStream'
                            - 'logs:CreateLogGroup'
                            - 'logs:PutLogEvents'
                          Resource:
                            - !Sub >-
                              arn:$${AWS::Partition}:logs:$${AWS::Region}:$${AWS::AccountId}:log-group:/aws/lambda/$${EnvironmentName}-$${LambdaName}-Proxy*:*
                        - Effect: Allow
                          Action:
                            - 'lambda:InvokeFunction'
                          Resource:
                            - !Sub >-
                              arn:aws:lambda:$${AWS::Region}:$${AWS::AccountId}:function:$${EnvironmentName}-$${LambdaName}*
                        - Effect: Allow
                          Action:
                            - 'lambda:InvokeFunction'
                          Resource:
                            - !Sub >-
                              arn:aws:lambda:$${AWS::Region}:$${AWS::AccountId}:function:$${EnvironmentName}-$${LambdaName}*:*
                Path: /
                RoleName: !Sub '$${EnvironmentName}-$${LambdaName}-Proxy-role'
            ProxyLambdaPermissions:
              Type: 'AWS::Lambda::Permission'
              Properties:
                FunctionName: !GetAtt
                  - ProxyLambda
                  - Arn
                Action: 'lambda:InvokeFunction'
                Principal: apigateway.amazonaws.com
                SourceArn: !Join
                  - ''
                  - - 'arn:'
                    - !Ref 'AWS::Partition'
                    - ':execute-api:'
                    - !Ref 'AWS::Region'
                    - ':'
                    - !Ref 'AWS::AccountId'
                    - ':'
                    - !Ref RestApi
                    - /*/*
            ProxyLambda:
              Type: 'AWS::Lambda::Function'
              Properties:
                Code:
                  S3Bucket: !Ref LambdaS3Bucket
                  S3Key: !Ref ProxyLambdaS3Key
                Environment:
                  Variables:
                    DEFAULT_LAMBDA: !Ref LambdaVersion
                    COGNITO_AUTHORIZATION_REQUIRED: !!str "false"
                Description: !Sub '$${LambdaDescription} Proxy'
                FunctionName: !Sub '$${EnvironmentName}-$${LambdaName}-Proxy'
                Handler: main
                MemorySize: 128
                PackageType: Zip
                Role: !GetAtt
                  - IamRoleProxyLambdaExecution
                  - Arn
                Runtime: go1.x
                Timeout: 600
          Outputs:
            ProxyLambda:
              Description: The proxy lambda reference
              Value: !Ref ProxyLambda
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : "#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}"
          },
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "ProxyLambdaS3Key"
            ParameterValue : "#{Octopus.Action[Upload Lambda Proxy].Package[].PackageId}.#{Octopus.Action[Upload Lambda Proxy].Package[].PackageVersion}.zip"
          },
          {
            ParameterKey : "LambdaS3Bucket"
            ParameterValue : "#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}"
          },
          {
            ParameterKey : "LambdaName"
            ParameterValue : local.product_lambda_name
          },
          {
            ParameterKey : "LambdaDescription"
            ParameterValue : "#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}"
          },
          {
            ParameterKey : "LambdaVersion"
            ParameterValue : "#{Octopus.Action[Deploy Application Lambda Version].Output.AwsOutputs[LambdaVersion]}"
          }
        ])
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : "#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}"
          },
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "ProxyLambdaS3Key"
            ParameterValue : "#{Octopus.Action[Upload Lambda Proxy].Package[].PackageId}.#{Octopus.Action[Upload Lambda Proxy].Package[].PackageVersion}.zip"
          },
          {
            ParameterKey : "LambdaS3Bucket"
            ParameterValue : "#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}"
          },
          {
            ParameterKey : "LambdaName"
            ParameterValue : local.product_lambda_name
          },
          {
            ParameterKey : "LambdaDescription"
            ParameterValue : "#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}"
          },
          {
            ParameterKey : "LambdaVersion"
            ParameterValue : "#{Octopus.Action[Deploy Application Lambda Version].Output.AwsOutputs[LambdaVersion]}"
          }
        ])
        "Octopus.Action.Aws.IamCapabilities" : jsonencode([
          "CAPABILITY_AUTO_EXPAND", "CAPABILITY_IAM", "CAPABILITY_NAMED_IAM"
        ])
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Deploy Reverse Proxy Lambda Version"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy Reverse Proxy Lambda Version"
      notes          = "This step deploys a uniquely named CloudFormation stack that creates a version of the reverse proxy created in the previous step."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.product_cloudformation_transient_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.product_proxy_version_stack
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          # This template creates a new version of the reverse proxy lambda. The stack created by
          # this step must have a unique name, and must be tagged in such a way as to indicate
          # which Octopus deployment created it. Subsequent deployments will clean up this
          # stack once the API Gateway stage no longer points to it, thus cleaning up old lambda versions.
          Parameters:
            RestApi:
              Type: String
            LambdaDescription:
              Type: String
            ProxyLambda:
              Type: String
          Resources:
            LambdaVersion:
              Type: 'AWS::Lambda::Version'
              Properties:
                FunctionName: !Ref ProxyLambda
                Description: !Ref LambdaDescription
            ApplicationLambdaPermissions:
              Type: 'AWS::Lambda::Permission'
              Properties:
                FunctionName: !Ref LambdaVersion
                Action: 'lambda:InvokeFunction'
                Principal: apigateway.amazonaws.com
                SourceArn: !Join
                  - ''
                  - - 'arn:'
                    - !Ref 'AWS::Partition'
                    - ':execute-api:'
                    - !Ref 'AWS::Region'
                    - ':'
                    - !Ref 'AWS::AccountId'
                    - ':'
                    - !Ref RestApi
                    - /*/*
          Outputs:
            ProxyLambdaVersion:
              Description: The name of the Lambda version resource deployed by this template
              Value: !Ref LambdaVersion
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : jsonencode([
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "LambdaDescription"
            ParameterValue : "#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}"
          },
          {
            ParameterKey : "ProxyLambda"
            ParameterValue : "#{Octopus.Action[Deploy Reverse Proxy Lambda].Output.AwsOutputs[ProxyLambda]}"
          }
        ])
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : jsonencode([
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "LambdaDescription"
            ParameterValue : "#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}"
          },
          {
            ParameterKey : "ProxyLambda"
            ParameterValue : "#{Octopus.Action[Deploy Reverse Proxy Lambda].Output.AwsOutputs[ProxyLambda]}"
          }
        ])
        "Octopus.Action.Aws.IamCapabilities" : jsonencode([
          "CAPABILITY_AUTO_EXPAND", "CAPABILITY_IAM", "CAPABILITY_NAMED_IAM"
        ])
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Update API Gateway"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Update API Gateway"
      notes          = "This step attaches the reverse proxy version created in the previous step to the API Gateway, and creates an API Gateway deployment."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.product_cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.product_api_gateway_stack
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          # This template links the reverse proxy to the API Gateway. Once this linking is done,
          # the API Gateway is ready to be deployed to a stage. But the old Lambda versions are
          # still referenced by the existing stage, so no changes have been exposed to the
          # end user.
          Parameters:
            EnvironmentName:
              Type: String
              Default: '#{Octopus.Environment.Name | Replace " .*" "" | ToLower}'
            RestApi:
              Type: String
            ResourceId:
              Type: String
            ProxyLambdaVersion:
              Type: String
          Resources:
            ApiServiceAccountsResource:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !Ref ResourceId
                PathPart: products
            ApiServiceAccountsMethod:
              Type: 'AWS::ApiGateway::Method'
              Properties:
                AuthorizationType: NONE
                HttpMethod: ANY
                Integration:
                  IntegrationHttpMethod: POST
                  TimeoutInMillis: 20000
                  Type: AWS_PROXY
                  Uri: !Join
                    - ''
                    - - 'arn:'
                      - !Ref 'AWS::Partition'
                      - ':apigateway:'
                      - !Ref 'AWS::Region'
                      - ':lambda:path/2015-03-31/functions/'
                      - !Ref ProxyLambdaVersion
                      - /invocations
                ResourceId: !Ref ApiServiceAccountsResource
                RestApiId: !Ref RestApi
            'Deployment#{Octopus.Deployment.Id | Replace -}':
              Type: 'AWS::ApiGateway::Deployment'
              Properties:
                RestApiId: !Ref RestApi
              DependsOn:
                - ApiServiceAccountsMethod
          Outputs:
            DeploymentId:
              Description: The deployment id
              Value: !Ref 'Deployment#{Octopus.Deployment.Id | Replace -}'
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : "#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}"
          },
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "ResourceId"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.Api}"
          },
          {
            ParameterKey : "ProxyLambdaVersion"
            ParameterValue : "#{Octopus.Action[Deploy Reverse Proxy Lambda Version].Output.AwsOutputs[ProxyLambdaVersion]}"
          }
        ])
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : "#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}"
          },
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "ResourceId"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.Api}"
          },
          {
            ParameterKey : "ProxyLambdaVersion"
            ParameterValue : "#{Octopus.Action[Deploy Reverse Proxy Lambda Version].Output.AwsOutputs[ProxyLambdaVersion]}"
          }
        ])
        "Octopus.Action.Aws.IamCapabilities" : jsonencode([
          "CAPABILITY_AUTO_EXPAND", "CAPABILITY_IAM", "CAPABILITY_NAMED_IAM"
        ])
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Update Stage"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Update Stage"
      notes          = "This step deploys the deployment created in the previous step, effectively exposing the new Lambdas to the public."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.product_cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.api_gateway_stage_stack
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          # This template updates the stage with the deployment created in the previous step.
          # It is here that the new Lambda versions are exposed to the end user.
          Parameters:
            EnvironmentName:
              Type: String
              Default: '#{Octopus.Environment.Name | Replace " .*" "" | ToLower}'
            DeploymentId:
              Type: String
              Default: 'Deployment#{DeploymentId}'
            ApiGatewayId:
              Type: String
          Resources:
            Stage:
              Type: 'AWS::ApiGateway::Stage'
              Properties:
                DeploymentId:
                  'Fn::Sub': '$${DeploymentId}'
                RestApiId:
                  'Fn::Sub': '$${ApiGatewayId}'
                StageName:
                  'Fn::Sub': '$${EnvironmentName}'
          Outputs:
            StageURL:
              Description: The url of the stage
              Value:
                'Fn::Join':
                  - ''
                  - - 'https://'
                    - Ref: ApiGatewayId
                    - .execute-api.
                    - Ref: 'AWS::Region'
                    - .amazonaws.com/
                    - Ref: Stage
                    - /
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : "#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}"
          },
          {
            ParameterKey : "DeploymentId"
            ParameterValue : "#{Octopus.Action[Update API Gateway].Output.AwsOutputs[DeploymentId]}"
          },
          {
            ParameterKey : "ApiGatewayId"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          }
        ])
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : "#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}"
          },
          {
            ParameterKey : "DeploymentId"
            ParameterValue : "#{Octopus.Action[Update API Gateway].Output.AwsOutputs[DeploymentId]}"
          },
          {
            ParameterKey : "ApiGatewayId"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          }
        ])
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Clean up Lambda Versions"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Clean up Lambda Versions"
      notes          = "Now that the API Gateway is pointing to the new Lambda versions, the old Lambda versions can be cleaned up."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          echo "Downloading Docker images"

          echo "##octopus[stdout-verbose]"

          docker pull amazon/aws-cli 2>&1

          # Alias the docker run commands
          shopt -s expand_aliases
          alias aws="docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli"

          echo "##octopus[stdout-default]"

          OLD_STACKS=$(aws cloudformation describe-stacks --query 'Stacks[?Tags[?Key == `OctopusTransient` && Value == `True`] && Tags[?Key == `OctopusEnvironmentId` && Value == `#{Octopus.Environment.Id}`] && Tags[?Key == `OctopusProjectId` && Value == `#{Octopus.Project.Id}`] && Tags[?Key == `OctopusDeploymentId` && Value != `#{Octopus.Deployment.Id}`] && Tags[?Key == `OctopusTenantId` && Value == `#{if Octopus.Deployment.Tenant.Id}#{Octopus.Deployment.Tenant.Id}#{/if}#{unless Octopus.Deployment.Tenant.Id}untenanted#{/unless}`]].{StackName: StackName}' --output text)

          if [[ -n "$${OLD_STACKS}" ]]; then
            echo "Cleaning up the following stacks:"
            echo "$${OLD_STACKS}"
            echo "$${OLD_STACKS}" | xargs -n1 aws cloudformation delete-stack --stack-name $1
          fi
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Get Stage URL"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Get Stage URL"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          echo "Downloading Docker images"

          echo "##octopus[stdout-verbose]"

          docker pull amazon/aws-cli 2>&1

          # Alias the docker run commands
          shopt -s expand_aliases
          alias aws="docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli"

          echo "##octopus[stdout-default]"

          STAGE_URL=$(aws cloudformation \
              describe-stacks \
              --stack-name "${local.api_gateway_stage_stack}" \
              --query "Stacks[0].Outputs[?OutputKey=='StageURL'].OutputValue" \
              --output text)

          set_octopusvariable "StageURL" $${STAGE_URL}

          write_highlight "Open [$${STAGE_URL}/api/products]($${STAGE_URL}/api/products) to view the backend API."
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Check for Vulnerabilities"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    run_script_action {
      can_be_used_for_project_versioning = false
      condition                          = "Success"
      is_disabled                        = false
      is_required                        = true
      script_syntax                      = "Bash"
      script_source                      = "Inline"
      run_on_server                      = true
      worker_pool_id                     = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      name                               = "Check for Vulnerabilities"
      environments                       = [
        data.octopusdeploy_environments.development_security.environments[0].id,
        data.octopusdeploy_environments.production_security.environments[0].id
      ]
      package {
        name                      = local.products_sbom_package
        package_id                = local.products_sbom_package
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      script_body = local.vulnerability_scan
    }
  }
}