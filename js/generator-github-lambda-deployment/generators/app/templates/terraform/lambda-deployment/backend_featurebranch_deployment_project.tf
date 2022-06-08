resource "octopusdeploy_project" "deploy_backend_featurebranch_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the backend feature branch service to Lambda."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = "Backend Feature Branch Service"
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

resource "octopusdeploy_variable" "debug_featurebranch_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_featurebranch_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "debug_featurebranch_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_featurebranch_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "aws_account_deploy_backend_featurebranch_project" {
  name     = "AWS Account"
  type     = "AmazonWebServicesAccount"
  value    = var.octopus_aws_account_id
  owner_id = octopusdeploy_project.deploy_backend_featurebranch_project.id
}

locals {
  # Subnet group names are lowercase
  featurebranch_subnetgroup_product_name = "product-${lower(var.github_repo_owner)}-${local.fixed_environment}-#{Octopus.Action[Get Stack Outputs].Output.BranchName}"
  featurebranch_s3_bucket_stack          = "OctopusBuilder-Lambda-S3Bucket-${lower(var.github_repo_owner)}-${local.fixed_environment}-#{Octopus.Action[Get Stack Outputs].Output.BranchName}"
  featurebranch_product_stack            = "OctopusBuilder-Product-${lower(var.github_repo_owner)}-${local.fixed_environment}-#{Octopus.Action[Get Stack Outputs].Output.BranchName}"
}

resource "octopusdeploy_deployment_process" "deploy_backend_featurebranch" {
  project_id = octopusdeploy_project.deploy_backend_featurebranch_project.id

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

          BRANCH_NAME="#{Octopus.Action[Upload Lambda].Package[].PackageVersion | VersionPreRelease | Replace "\..*" "" | ToLower}"

          set_octopusvariable "BranchName" $${BRANCH_NAME}

          STAGE_URL=$(aws cloudformation \
              describe-stacks \
              --stack-name "${local.api_gateway_stage_stack}" \
              --query "Stacks[0].Outputs[?OutputKey=='StageURL'].OutputValue" \
              --output text)

          set_octopusvariable "StageURL" $${STAGE_URL}

          if [[ -z "$${STAGE_URL}" ]]; then
            echo "Run the API Gateway project first"
            exit 1
          fi

          DNS_NAME=$(aws cloudformation \
              describe-stacks \
              --stack-name "${local.api_gateway_stage_stack}" \
              --query "Stacks[0].Outputs[?OutputKey=='DnsName'].OutputValue" \
              --output text)

          set_octopusvariable "DNSName" $${DNS_NAME}

          if [[ -z "$${DNS_NAME}" ]]; then
            echo "Run the API Gateway project first"
            exit 1
          fi

          echo "Branch Name: $${BRANCH_NAME}"
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
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
        "Octopus.Action.Aws.CloudFormationStackName" : local.featurebranch_s3_bucket_stack
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
    name                = "Deploy Application Lambda"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy Application Lambda"
      notes          = "Deploy the feature branch Lambda. Unlike mainline deployments, which use Lambda versions, feature branches only use the $LATEST version."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.product_cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.featurebranch_product_stack
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
            SubnetGroupName:
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
                DBSubnetGroupName: !Sub '$${SubnetGroupName}'
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
              DependsOn:
                - SubnetGroup
            AppLogGroup:
              Type: 'AWS::Logs::LogGroup'
              Properties:
                LogGroupName: !Sub '/aws/lambda/$${LambdaName}'
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
                  - PolicyName: !Sub '$${LambdaName}-policy'
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
                              arn:$${AWS::Partition}:logs:$${AWS::Region}:$${AWS::AccountId}:log-group:/aws/lambda/$${LambdaName}*:*
                        - Effect: Allow
                          Action:
                            - 'ec2:DescribeInstances'
                            - 'ec2:CreateNetworkInterface'
                            - 'ec2:AttachNetworkInterface'
                            - 'ec2:DeleteNetworkInterface'
                            - 'ec2:DescribeNetworkInterfaces'
                          Resource: "*"
                Path: /
                RoleName: !Sub '$${LambdaName}-role'
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
                FunctionName: !Sub '$${LambdaName}-DBMigration'
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
                FunctionName: !Sub '$${LambdaName}'
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
            ParameterValue : "#{Octopus.Environment.Name | Replace \" .*\" \"\"}"
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
            ParameterValue : "${local.product_lambda_name}-#{Octopus.Action[Get Stack Outputs].Output.BranchName}"
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
          },
          {
            ParameterKey : "SubnetGroupName"
            ParameterValue : local.featurebranch_subnetgroup_product_name
          }
        ])
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : "#{Octopus.Environment.Name | Replace \" .*\" \"\"}"
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
            ParameterValue : "${local.product_lambda_name}-#{Octopus.Action[Get Stack Outputs].Output.BranchName}"
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
          },
          {
            ParameterKey : "SubnetGroupName"
            ParameterValue : local.featurebranch_subnetgroup_product_name
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
            --function-name '${local.product_lambda_name}-#{Octopus.Action[Get Stack Outputs].Output.BranchName}-DBMigration' \
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
    name                = "Print Routing Details"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Print Routing Details"
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
          echo "Access this feature branch by setting the Routing header to:"
          echo "route[/api/products:GET]=lambda[${local.product_lambda_name}-#{Octopus.Action[Get Stack Outputs].Output.BranchName}];route[/api/products/**:GET]=path[/api/products:GET]"
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