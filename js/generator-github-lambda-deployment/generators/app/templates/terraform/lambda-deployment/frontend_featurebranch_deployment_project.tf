resource "octopusdeploy_project" "deploy_frontend_featurebranch_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys a frontend webapp feature branch to Lambda."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = "Frontend Feature Branch WebApp"
  project_group_id                     = octopusdeploy_project_group.frontend_project_group.id
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

resource "octopusdeploy_variable" "frontend_featurebranch_debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_featurebranch_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "frontend_featurebranch_debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_featurebranch_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "aws_account_deploy_frontend_featurebranch_project" {
  name     = "AWS Account"
  type     = "AmazonWebServicesAccount"
  value    = var.octopus_aws_account_id
  owner_id = octopusdeploy_project.deploy_frontend_featurebranch_project.id
}

resource "octopusdeploy_variable" "productEndpoint_deploy_frontend_featurebranch_project" {
  name     = "productEndpoint"
  type     = "String"
  value    = "/${local.fixed_environment_upper}/api/products"
  owner_id = octopusdeploy_project.deploy_frontend_featurebranch_project.id
}

resource "octopusdeploy_variable" "productHealthEndpoint_deploy_frontend_featurebranch_project" {
  name     = "productHealthEndpoint"
  type     = "String"
  value    = "/${local.fixed_environment_upper}/health/products"
  owner_id = octopusdeploy_project.deploy_frontend_featurebranch_project.id
}

resource "octopusdeploy_variable" "auditEndpoint_deploy_frontend_featurebranch_project" {
  name     = "auditEndpoint"
  type     = "String"
  value    = "/${local.fixed_environment_upper}/api/audits"
  owner_id = octopusdeploy_project.deploy_frontend_featurebranch_project.id
}

resource "octopusdeploy_variable" "auditHealthEndpoint_deploy_frontend_featurebranch_project" {
  name     = "auditHealthEndpoint"
  type     = "String"
  value    = "/${local.fixed_environment_upper}/health/audits"
  owner_id = octopusdeploy_project.deploy_frontend_featurebranch_project.id
}

resource "octopusdeploy_variable" "cypress_baseurl_variable_featurebranch" {
  name         = "baseUrl"
  type         = "String"
  description  = "A structured variable replacement for the Cypress test."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_featurebranch_project.id
  value        = "#{Octopus.Action[Get Stage Outputs].Output.StageURL}#{Octopus.Action[Get Stack Outputs].Output.BranchName}"
}

locals {
  featurebranch_frontend_s3_bucket_stack = "OctopusBuilder-WebApp-S3Bucket-${lower(var.github_repo_owner)}-${local.fixed_environment}-#{Octopus.Action[Get Stack Outputs].Output.BranchName}"
  featurebranch_frontend_stack           = "OctopusBuilder-WebApp-${lower(var.github_repo_owner)}-${local.fixed_environment}-#{Octopus.Action[Get Stack Outputs].Output.BranchName}"
}

resource "octopusdeploy_deployment_process" "deploy_frontend_featurebranch" {
  project_id = octopusdeploy_project.deploy_frontend_featurebranch_project.id

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

          WEB_RESOURCE_ID=$(aws cloudformation \
              describe-stacks \
              --stack-name ${local.api_gateway_stack} \
              --query "Stacks[0].Outputs[?OutputKey=='Web'].OutputValue" \
              --output text)

          set_octopusvariable "Web" $${WEB_RESOURCE_ID}
          echo "Web Resource ID: $WEB_RESOURCE_ID"

          if [[ -z "$${WEB_RESOURCE_ID}" ]]; then
            echo "Run the API Gateway project first"
            exit 1
          fi

          REST_API=$(aws cloudformation \
              describe-stacks \
              --stack-name ${local.api_gateway_stack} \
              --query "Stacks[0].Outputs[?OutputKey=='RestApi'].OutputValue" \
              --output text)

          set_octopusvariable "RestApi" $${REST_API}
          echo "Rest API ID: $REST_API"

          if [[ -z "$${REST_API}" ]]; then
            echo "Run the API Gateway project first"
            exit 1
          fi

          ROOT_RESOURCE_ID=$(aws cloudformation \
              describe-stacks \
              --stack-name ${local.api_gateway_stack} \
              --query "Stacks[0].Outputs[?OutputKey=='RootResourceId'].OutputValue" \
              --output text)

          set_octopusvariable "RootResourceId" $${ROOT_RESOURCE_ID}
          echo "Root resource ID: $ROOT_RESOURCE_ID"

          if [[ -z "$${ROOT_RESOURCE_ID}" ]]; then
            echo "Run the API Gateway project first"
            exit 1
          fi

          set_octopusvariable "BranchName" "#{Octopus.Action[Upload Frontend].Package[].PackageVersion | VersionPreRelease | Replace "\..*" "" | ToLower}"
          echo "Branch Name: #{Octopus.Action[Upload Frontend].Package[].PackageVersion | VersionPreRelease | Replace "\..*" "" | ToLower}"
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Create S3 Bucket"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Create S3 Bucket"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.frontend_cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.featurebranch_frontend_s3_bucket_stack
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          AWSTemplateFormatVersion: 2010-09-09
          Parameters:
            Hostname:
              Type: String
          Resources:
            S3Bucket:
              Type: AWS::S3::Bucket
              Properties:
                AccessControl: PublicRead
                WebsiteConfiguration:
                  IndexDocument: index.html
                  ErrorDocument: error.html
                  RoutingRules:
                  - RoutingRuleCondition:
                     HttpErrorCodeReturnedEquals: '404'
                    RedirectRule:
                      ReplaceKeyWith: index.html
                      HostName: !Ref Hostname
                      Protocol: https
              DeletionPolicy: Retain
            BucketPolicy:
              Type: AWS::S3::BucketPolicy
              Properties:
                PolicyDocument:
                  Id: MyPolicy
                  Version: 2012-10-17
                  Statement:
                    - Sid: PublicReadForGetBucketObjects
                      Effect: Allow
                      Principal: '*'
                      Action: 's3:GetObject'
                      Resource: !Join
                        - ''
                        - - 'arn:aws:s3:::'
                          - !Ref S3Bucket
                          - /*
                Bucket: !Ref S3Bucket
          Outputs:
            Bucket:
              Value: !Ref S3Bucket
              Description: URL for website hosted on S3
            WebsiteURL:
              Value: !GetAtt
                - S3Bucket
                - WebsiteURL
              Description: URL for website hosted on S3
            S3BucketSecureURL:
              Value: !Join
                - ''
                - - 'https://'
                  - !GetAtt
                    - S3Bucket
                    - DomainName
              Description: Name of S3 bucket to hold website content
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : jsonencode([
          {
            ParameterKey : "Hostname"
            ParameterValue : "#{WebApp.Hostname}"
          }
        ])
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : jsonencode([
          {
            ParameterKey : "Hostname"
            ParameterValue : "#{WebApp.Hostname}"
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
    name                = "Upload Frontend"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsUploadS3"
      name           = "Upload Frontend"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      primary_package {
        acquisition_location = "Server"
        feed_id              = var.octopus_built_in_feed_id
        package_id           = local.frontend_package_id
        properties           = {
          "SelectionMode" : "immediate"
        }
      }

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.S3.BucketName" : "#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[Bucket]}"
        "Octopus.Action.Aws.S3.FileSelections" : jsonencode([
          {
            type : "MultipleFiles"
            tags : []
            metadata : []
            cannedAcl : "private"
            path : ""
            storageClass : "STANDARD"
            bucketKey : ""
            bucketKeyPrefix : "#{Octopus.Action[Upload Frontend].Package[].PackageId}.#{Octopus.Action[Upload Frontend].Package[].PackageVersion}/"
            bucketKeyBehaviour : "Custom"
            performVariableSubstitution : "False"
            performStructuredVariableSubstitution : "False"
            pattern : "**/*"
            autoFocus : true
            structuredVariableSubstitutionPatterns : "config.json"
          }
        ])
        "Octopus.Action.Aws.S3.TargetMode" : "FileSelections"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
        "Octopus.Action.Package.DownloadOnTentacle" : "False"
        "Octopus.Action.Package.FeedId" : var.octopus_built_in_feed_id
        "Octopus.Action.Package.PackageId" : local.frontend_package_id
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Proxy with API Gateway"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Proxy with API Gateway"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.frontend_cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.featurebranch_frontend_stack
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          Parameters:
            EnvironmentName:
              Type: String
              Default: '${local.fixed_environment_upper}'
            RestApi:
              Type: String
            RootResourceId:
              Type: String
            ResourceId:
              Type: String
            PackageVersion:
              Type: String
            PackageId:
              Type: String
            BucketName:
              Type: String
            SubPath:
              Type: String
          Conditions:
            IsFeatureBranch:
              'Fn::Not':
                - 'Fn::Equals':
                    - Ref: SubPath
                    - ''
          Resources:
            BranchResource:
              Type: 'AWS::ApiGateway::Resource'
              Condition: IsFeatureBranch
              Properties:
                RestApiId:
                  Ref: RestApi
                ParentId:
                  Ref: RootResourceId
                PathPart:
                  Ref: SubPath
            BranchResourceProxy:
              Type: 'AWS::ApiGateway::Resource'
              Condition: IsFeatureBranch
              Properties:
                RestApiId:
                  Ref: RestApi
                ParentId:
                  Ref: BranchResource
                PathPart: '{proxy+}'
            FrontendMethodOne:
              Type: 'AWS::ApiGateway::Method'
              Properties:
                AuthorizationType: NONE
                HttpMethod: ANY
                Integration:
                  ContentHandling: CONVERT_TO_TEXT
                  IntegrationHttpMethod: GET
                  TimeoutInMillis: 20000
                  Type: HTTP
                  Uri:
                    'Fn::Join':
                      - ''
                      - - 'http://'
                        - Ref: BucketName
                        - .s3-website-us-west-1.amazonaws.com/
                        - Ref: PackageId
                        - .
                        - Ref: PackageVersion
                        - /index.html
                  PassthroughBehavior: WHEN_NO_MATCH
                  RequestTemplates:
                    image/png: ''
                  IntegrationResponses:
                    - StatusCode: '200'
                      ResponseParameters:
                        method.response.header.Content-Type: integration.response.header.Content-Type
                        method.response.header.X-Content-Type-Options: '''nosniff'''
                        method.response.header.X-Frame-Options: '''DENY'''
                        method.response.header.X-XSS-Protection: '''1; mode=block'''
                        method.response.header.Referrer-Policy: '''no-referrer'''
                        method.response.header.Permissions-Policy: "${local.permissions_policy}"
                        method.response.header.Content-Security-Policy: "${local.security_policy}"
                        method.response.header.Strict-Transport-Security: '''max-age=15768000'''
                MethodResponses:
                  - ResponseModels:
                      text/html: Empty
                      text/css: Empty
                    StatusCode: '200'
                    ResponseParameters:
                      method.response.header.Content-Type: true
                      method.response.header.Content-Security-Policy: true
                      method.response.header.X-Content-Type-Options: true
                      method.response.header.X-Frame-Options: true
                      method.response.header.X-XSS-Protection: true
                      method.response.header.Referrer-Policy: true
                      method.response.header.Permissions-Policy: true
                      method.response.header.Strict-Transport-Security: true
                ResourceId:
                  'Fn::If':
                    - IsFeatureBranch
                    - Ref: BranchResource
                    - Ref: RootResourceId
                RestApiId:
                  Ref: RestApi
            FrontendMethodTwo:
              Type: 'AWS::ApiGateway::Method'
              Properties:
                AuthorizationType: NONE
                HttpMethod: ANY
                RequestParameters:
                  method.request.path.proxy: true
                Integration:
                  ContentHandling: CONVERT_TO_TEXT
                  IntegrationHttpMethod: GET
                  TimeoutInMillis: 20000
                  Type: HTTP
                  Uri:
                    'Fn::Join':
                      - ''
                      - - 'http://'
                        - Ref: BucketName
                        - .s3-website-us-west-1.amazonaws.com/
                        - Ref: PackageId
                        - .
                        - Ref: PackageVersion
                        - '/{proxy}'
                  PassthroughBehavior: WHEN_NO_MATCH
                  RequestTemplates:
                    image/png: ''
                  IntegrationResponses:
                    - StatusCode: '200'
                      ResponseParameters:
                        method.response.header.Content-Type: integration.response.header.Content-Type
                        method.response.header.X-Content-Type-Options: '''nosniff'''
                        method.response.header.X-Frame-Options: '''DENY'''
                        method.response.header.X-XSS-Protection: '''1; mode=block'''
                        method.response.header.Referrer-Policy: '''no-referrer'''
                        method.response.header.Permissions-Policy: "${local.permissions_policy}"
                        method.response.header.Content-Security-Policy: "${local.security_policy}"
                        method.response.header.Strict-Transport-Security: '''max-age=15768000'''
                    - StatusCode: '307'
                      SelectionPattern: '307'
                      ResponseParameters:
                        method.response.header.Location: integration.response.header.Location
                  RequestParameters:
                    integration.request.path.proxy: method.request.path.proxy
                MethodResponses:
                  - ResponseModels:
                      text/html: Empty
                      text/css: Empty
                    StatusCode: '200'
                    ResponseParameters:
                      method.response.header.Content-Type: true
                      method.response.header.Content-Security-Policy: true
                      method.response.header.X-Content-Type-Options: true
                      method.response.header.X-Frame-Options: true
                      method.response.header.X-XSS-Protection: true
                      method.response.header.Referrer-Policy: true
                      method.response.header.Permissions-Policy: true
                      method.response.header.Strict-Transport-Security: true
                  - ResponseModels:
                      text/html: Empty
                      text/css: Empty
                    StatusCode: '307'
                    ResponseParameters:
                      method.response.header.Location: true
                ResourceId:
                  'Fn::If':
                    - IsFeatureBranch
                    - Ref: BranchResourceProxy
                    - Ref: ResourceId
                RestApiId:
                  Ref: RestApi
            'Deployment#{Octopus.Deployment.Id | Replace -}':
              Type: 'AWS::ApiGateway::Deployment'
              Properties:
                RestApiId:
                  Ref: RestApi
              DependsOn:
                - FrontendMethodOne
                - FrontendMethodTwo
          Outputs:
            DeploymentId:
              Description: The deployment id
              Value:
                Ref: 'Deployment#{Octopus.Deployment.Id | Replace -}'
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : local.fixed_environment_upper
          },
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "RootResourceId"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RootResourceId}"
          },
          {
            ParameterKey : "ResourceId"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.Web}"
          },
          {
            ParameterKey : "PackageVersion"
            ParameterValue : "#{Octopus.Action[Upload Frontend].Package[].PackageVersion}"
          },
          {
            ParameterKey : "PackageId"
            ParameterValue : "#{Octopus.Action[Upload Frontend].Package[].PackageId}"
          },
          {
            ParameterKey : "BucketName"
            ParameterValue : "#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[Bucket]}"
          },
          {
            ParameterKey : "SubPath"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.BranchName}"
          }
        ])
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : local.fixed_environment_upper
          },
          {
            ParameterKey : "RestApi"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          },
          {
            ParameterKey : "RootResourceId"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RootResourceId}"
          },
          {
            ParameterKey : "ResourceId"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.Web}"
          },
          {
            ParameterKey : "PackageVersion"
            ParameterValue : "#{Octopus.Action[Upload Frontend].Package[].PackageVersion}"
          },
          {
            ParameterKey : "PackageId"
            ParameterValue : "#{Octopus.Action[Upload Frontend].Package[].PackageId}"
          },
          {
            ParameterKey : "BucketName"
            ParameterValue : "#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[Bucket]}"
          },
          {
            ParameterKey : "SubPath"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.BranchName}"
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
    name                = "Update Stage"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Update Stage"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.frontend_cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.api_gateway_stage_stack
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          Parameters:
            EnvironmentName:
              Type: String
              Default: '${local.fixed_environment_upper}'
            DeploymentId:
              Type: String
            ApiGatewayId:
              Type: String
          Resources:
            Stage:
              Type: 'AWS::ApiGateway::Stage'
              Properties:
                DeploymentId: !Sub '$${DeploymentId}'
                RestApiId: !Sub '$${ApiGatewayId}'
                StageName: !Sub '$${EnvironmentName}'
                Variables:
                  indexPage: !Sub /index.html
          Outputs:
            DnsName:
              Value:
                'Fn::Join':
                  - ''
                  - - Ref: ApiGatewayId
                    - .execute-api.
                    - Ref: 'AWS::Region'
                    - .amazonaws.com
            StageURL:
              Description: The url of the stage
              Value: !Join
                - ''
                - - 'https://'
                  - !Ref ApiGatewayId
                  - .execute-api.
                  - !Ref 'AWS::Region'
                  - .amazonaws.com/
                  - !Ref Stage
                  - /
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : local.fixed_environment_upper
          },
          {
            ParameterKey : "DeploymentId"
            ParameterValue : "#{Octopus.Action[Proxy with API Gateway].Output.AwsOutputs[DeploymentId]}"
          },
          {
            ParameterKey : "ApiGatewayId"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.RestApi}"
          }
        ])
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : local.fixed_environment_upper
          },
          {
            ParameterKey : "DeploymentId"
            ParameterValue : "#{Octopus.Action[Proxy with API Gateway].Output.AwsOutputs[DeploymentId]}"
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
    name                = "Get Stage Outputs"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Get Stage Outputs"
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
          echo "Stage URL: $STAGE_URL"

          DNS_NAME=$(aws cloudformation \
              describe-stacks \
              --stack-name "${local.api_gateway_stage_stack}" \
              --query "Stacks[0].Outputs[?OutputKey=='DnsName'].OutputValue" \
              --output text)

          set_octopusvariable "DNSName" $${DNS_NAME}
          echo "DNS Name: $DNS_NAME"

          write_highlight "Open [$${STAGE_URL}#{Octopus.Action[Get Stack Outputs].Output.BranchName}/index.html]($${STAGE_URL}#{Octopus.Action[Get Stack Outputs].Output.BranchName}/index.html) to view the frontend web app."
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "HTTP Smoke Test"
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
      name                               = "HTTP Smoke Test"
      notes                              = "Use curl to perform a smoke test of a HTTP endpoint."
      environments                       = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      script_body = <<-EOT
          # Load balancers can take a minute or so before their DNS is propagated.
          # A status code of 000 means curl could not resolve the DNS name, so we wait for a bit until DNS is updated.
          echo "Waiting for DNS to propagate. This can take a while for a new load balancer."
          for i in {1..30}
          do
              CODE=$(curl -o /dev/null -s -w "%%{http_code}\n" #{Octopus.Action[Get Stage Outputs].Output.StageURL}#{Octopus.Action[Get Stack Outputs].Output.BranchName}/index.html)
              if [[ "$${CODE}" == "200" ]]
              then
                break
              fi
              echo "Waiting for DNS name to be resolvable and for service to respond"
              sleep 10
          done

          echo "response code: $${CODE}"
          if [[ "$${CODE}" == "200" ]]
          then
            echo "success"
            exit 0;
          else
            echo "error"
            exit 1;
          fi
        EOT
    }
  }
  step {
    condition           = "Success"
    name                = "Cypress E2E Test"
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
      name                               = "Cypress E2E Test"
      notes                              = "Use cypress to perform an end to end test of the frontend web app."
      environments                       = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      features = ["Octopus.Features.JsonConfigurationVariables"]
      container {
        feed_id = var.octopus_k8s_feed_id
        image   = var.cypress_docker_image
      }
      package {
        name                      = "octopub-frontend-cypress"
        package_id                = "octopub-frontend-cypress"
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      properties = {
        "Octopus.Action.Package.JsonConfigurationVariablesTargets" : "**/cypress.json"
      }
      script_body = <<-EOT
          echo "##octopus[stdout-verbose]"
          cd octopub-frontend-cypress
          cat cypress.json
          OUTPUT=$(cypress run 2>&1)
          RESULT=$?
          echo "##octopus[stdout-default]"

          # Print the output stripped of ANSI colour codes
          echo -e "$${OUTPUT}" | sed 's/\x1b\[[0-9;]*m//g'

          if [[ -f mochawesome.html ]]
          then
            inline-assets mochawesome.html selfcontained.html
            new_octopusartifact "$${PWD}/selfcontained.html" "html-report.html"
          fi
          if [[ -d cypress/screenshots/sample_spec.js ]]
          then
            zip -r screenshots.zip cypress/screenshots/sample_spec.js
            new_octopusartifact "$${PWD}/screenshots.zip" "screenshots.zip"
          fi
          exit $${RESULT}
        EOT
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
        name                      = local.frontend_sbom_package_name
        package_id                = local.frontend_sbom_package_name
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      script_body = local.vulnerability_scan
    }
  }
}