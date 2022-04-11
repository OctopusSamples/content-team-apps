locals {
  # These change with every project
  project_name = "GitHub Proxy"
  project_description = "which exposes a small subset of the GitHub API (just enough to know if a repo is created and if workflows have completed) accepting the encrypted token stored as a frontend client cookie. It is exposed under /api/${local.api_endpoint_name}.}"
  lambda_package = "github-repo-proxy-lambda"
  lambda_sbom_package = "github-repo-proxy-lambda-sbom"
  api_endpoint_name = "githubrepo"

  # These should be relatively stable
  reverse_proxy_package = "com.octopus:reverse-proxy"
  s3_bucket_cloudformation_name = "#{CloudFormation.S3Bucket}"
  lambda_cloudformation_name = "#{CloudFormation.ApplicationLambda}"
  lambda_name = "#{Lambda.Name}"
  lambda_proxy_cloudformation_name = "#{CloudFormation.ApplicationLambdaReverseProxy}"
  lambda_proxy_version_cloudformation_name = "#{CloudFormation.ApplicationLambdaReverseProxyVersion}-#{Octopus.Deployment.Id | Replace -}"
  lambda_version_cloudformation_name = "#{CloudFormation.ApplicationLambdaVersion}-#{Octopus.Deployment.Id | Replace -}"
  api_gateway_cloudformation_name = "#{CloudFormation.Application}"
  cloudformation_lambda_version_tags = "[{\"key\":\"OctopusTransient\",\"value\":\"True\"},{\"key\":\"OctopusTenantId\",\"value\":\"#{if Octopus.Deployment.Tenant.Id}#{Octopus.Deployment.Tenant.Id}#{/if}#{unless Octopus.Deployment.Tenant.Id}untenanted#{/unless}\"},{\"key\":\"OctopusStepId\",\"value\":\"#{Octopus.Step.Id}\"},{\"key\":\"OctopusRunbookRunId\",\"value\":\"#{if Octopus.RunBookRun.Id}#{Octopus.RunBookRun.Id}#{/if}#{unless Octopus.RunBookRun.Id}none#{/unless}\"},{\"key\":\"OctopusDeploymentId\",\"value\":\"#{if Octopus.Deployment.Id}#{Octopus.Deployment.Id}#{/if}#{unless Octopus.Deployment.Id}none#{/unless}\"},{\"key\":\"OctopusProjectId\",\"value\":\"#{Octopus.Project.Id}\"},{\"key\":\"OctopusEnvironmentId\",\"value\":\"#{Octopus.Environment.Id}\"},{\"key\":\"Environment\",\"value\":\"#{Octopus.Environment.Name}\"},{\"key\":\"Deployment Project\",\"value\":\"${octopusdeploy_project.deploy_project.name}\"},{\"key\":\"Team\",\"value\":\"Content Marketing\"}]"
  cloudformation_tags = "[{\"key\":\"Environment\",\"value\":\"#{Octopus.Environment.Name}\"},{\"key\":\"Deployment Project\",\"value\":\"${octopusdeploy_project.deploy_project.name}\"},{\"key\":\"Team\",\"value\":\"Content Marketing\"}]"
}

resource "octopusdeploy_project" "deploy_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the ${local.project_name}, ${local.project_description} Don't edit this process directly - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = local.project_name
  project_group_id                     = octopusdeploy_project_group.appbuilder_github_oauth_project_group.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  versioning_strategy {
    template = "#{Octopus.Version.LastMajor}.#{Octopus.Version.LastMinor}.#{Octopus.Version.LastPatch}.#{Octopus.Version.NextRevision}"
  }
  included_library_variable_sets       = [
    octopusdeploy_library_variable_set.library_variable_set.id, var.cognito_library_variable_set_id,
    var.content_team_library_variable_set_id, var.github_actions_library_variable_set_id
  ]

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

output "deploy_project_id" {
  value = octopusdeploy_project.deploy_project.id
}

resource "octopusdeploy_variable" "debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_project.id
  value        = "False"
}

resource "octopusdeploy_deployment_process" "deploy_project" {
  project_id = octopusdeploy_project.deploy_project.id
  step {
    condition           = "Success"
    name                = "Create S3 bucket"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Create S3 bucket"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]
      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.s3_bucket_cloudformation_name
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
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
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
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      primary_package {
        acquisition_location = "Server"
        feed_id              = var.octopus_built_in_feed_id
        package_id           = local.lambda_package
        properties           = {
          "SelectionMode" : "immediate"
        }
      }

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.Aws.S3.BucketName" : "#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}"
        "Octopus.Action.Aws.S3.PackageOptions" : "{\"bucketKey\":\"\",\"bucketKeyBehaviour\":\"Filename\",\"bucketKeyPrefix\":\"\",\"storageClass\":\"STANDARD\",\"cannedAcl\":\"private\",\"metadata\":[],\"tags\":[]}"
        "Octopus.Action.Aws.S3.TargetMode" : "EntirePackage"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
        "Octopus.Action.Package.DownloadOnTentacle" : "False"
        "Octopus.Action.Package.FeedId" : var.octopus_built_in_feed_id
        "Octopus.Action.Package.PackageId" : local.lambda_package
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
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      primary_package {
        acquisition_location = "Server"
        feed_id              = var.octopus_content_team_maven_feed_id
        package_id           = local.reverse_proxy_package
        properties           = {
          "SelectionMode" : "immediate"
        }
      }

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.Aws.S3.BucketName" : "#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}"
        "Octopus.Action.Aws.S3.PackageOptions" : "{\"bucketKey\":\"\",\"bucketKeyBehaviour\":\"Filename\",\"bucketKeyPrefix\":\"\",\"storageClass\":\"STANDARD\",\"cannedAcl\":\"private\",\"metadata\":[],\"tags\":[]}"
        "Octopus.Action.Aws.S3.TargetMode" : "EntirePackage"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
        "Octopus.Action.Package.DownloadOnTentacle" : "False"
        "Octopus.Action.Package.FeedId" : var.octopus_content_team_maven_feed_id
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
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          API_RESOURCE=$(aws cloudformation \
              describe-stacks \
              --stack-name #{CloudFormationName.ApiGateway} \
              --query "Stacks[0].Outputs[?OutputKey=='Api'].OutputValue" \
              --output text)

          set_octopusvariable "Api" $${API_RESOURCE}

          echo "API Resource ID: $${API_RESOURCE}"

          if [[ -z "$${API_RESOURCE}" ]]; then
            echo "Run the Jenkins Pipeline Shared Network Infrastructure project first"
            exit 1
          fi

          REST_API=$(aws cloudformation \
              describe-stacks \
              --stack-name #{CloudFormationName.ApiGateway} \
              --query "Stacks[0].Outputs[?OutputKey=='RestApi'].OutputValue" \
              --output text)

          set_octopusvariable "RestApi" $${REST_API}

          echo "Rest Api ID: $${REST_API}"

          if [[ -z "$${REST_API}" ]]; then
            echo "Run the Jenkins Pipeline Shared Network Infrastructure project first"
            exit 1
          fi

          COGNITO_POOL_ID=$(aws cloudformation \
              describe-stacks \
              --stack-name #{CloudFormation.Cognito} \
              --query "Stacks[0].Outputs[?OutputKey=='CognitoUserPoolID'].OutputValue" \
              --output text)
          echo "Cognito Pool ID: $${COGNITO_POOL_ID}"
          set_octopusvariable "CognitoPoolId" $${COGNITO_POOL_ID}

          if [[ -z "$${COGNITO_POOL_ID}" ]]; then
            echo "Run the Cognito project first"
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
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.lambda_cloudformation_name
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
            CognitoJwk:
              Type: String
            CognitoClientId:
              Type: String
          Resources:
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
                Path: /
                RoleName: !Sub '$${EnvironmentName}-$${LambdaName}-role'
            ApplicationLambda:
              Type: 'AWS::Lambda::Function'
              Properties:
                Description: !Ref LambdaDescription
                Code:
                  S3Bucket: !Ref LambdaS3Bucket
                  S3Key: !Ref LambdaS3Key
                Environment:
                  Variables:
                    SERVICEBUS_SECRET: !Ref ServiceBusSecret
                    SERVICEBUS_TENANT: !Ref ServiceBusTenant
                    SERVICEBUS_APPID: !Ref ServiceBusAppId
                    SERVICEBUS_NAMESPACE: !Ref ServiceBusNamespace
                    COGNITO_JWK: !Ref CognitoJwk
                    COGNITO_CLIENT_ID: !Ref CognitoClientId
                FunctionName: !Sub '$${EnvironmentName}-$${LambdaName}'
                Handler: io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest
                MemorySize: 512
                PackageType: Zip
                Role: !GetAtt
                  - IamRoleLambdaExecution
                  - Arn
                Runtime: java11
                Timeout: 600
          Outputs:
            ApplicationLambda:
              Description: The Lambda ref
              Value: !Ref ApplicationLambda
            EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"RestApi\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"},{\"ParameterKey\":\"ResourceId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.Api}\"},{\"ParameterKey\":\"LambdaS3Key\",\"ParameterValue\":\"#{Octopus.Action[Upload Lambda].Package[].PackageId}.#{Octopus.Action[Upload Lambda].Package[].PackageVersion}.zip\"},{\"ParameterKey\":\"LambdaS3Bucket\",\"ParameterValue\":\"#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}\"},{\"ParameterKey\":\"LambdaName\",\"ParameterValue\":\"${local.lambda_name}\"},{\"ParameterKey\":\"LambdaDescription\",\"ParameterValue\":\"#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"RestApi\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"},{\"ParameterKey\":\"ResourceId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.Api}\"},{\"ParameterKey\":\"LambdaS3Key\",\"ParameterValue\":\"#{Octopus.Action[Upload Lambda].Package[].PackageId}.#{Octopus.Action[Upload Lambda].Package[].PackageVersion}.zip\"},{\"ParameterKey\":\"LambdaS3Bucket\",\"ParameterValue\":\"#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}\"},{\"ParameterKey\":\"LambdaName\",\"ParameterValue\":\"${local.lambda_name}\"},{\"ParameterKey\":\"LambdaDescription\",\"ParameterValue\":\"#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}\"}]"
        "Octopus.Action.Aws.IamCapabilities" : "[\"CAPABILITY_AUTO_EXPAND\",\"CAPABILITY_IAM\",\"CAPABILITY_NAMED_IAM\"]"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
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
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.cloudformation_lambda_version_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.lambda_version_cloudformation_name
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
                ProvisionedConcurrencyConfig:
                  ProvisionedConcurrentExecutions: 20
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
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"RestApi\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"},{\"ParameterKey\":\"LambdaDescription\",\"ParameterValue\":\"#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}\"},{\"ParameterKey\":\"ApplicationLambda\",\"ParameterValue\":\"#{Octopus.Action[Deploy Application Lambda].Output.AwsOutputs[ApplicationLambda]}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"RestApi\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"},{\"ParameterKey\":\"LambdaDescription\",\"ParameterValue\":\"#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}\"},{\"ParameterKey\":\"ApplicationLambda\",\"ParameterValue\":\"#{Octopus.Action[Deploy Application Lambda].Output.AwsOutputs[ApplicationLambda]}\"}]"
        "Octopus.Action.Aws.IamCapabilities" : "[\"CAPABILITY_AUTO_EXPAND\",\"CAPABILITY_IAM\",\"CAPABILITY_NAMED_IAM\"]"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
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
      notes          = "To allow us to debug applications locally and deploy feature branches, each Lambda is exposed by a reverse proxy that can redirect requests to anoter Lambda or URL. This step deploys the reverse proxy."
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.lambda_proxy_cloudformation_name
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
            CognitoRegion:
              Type: String
            CognitoPool:
              Type: String
            CognitoJwk:
              Type: String
            CognitoRequiredGroup:
              Type: String
            CognitoClientId:
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
                    COGNITO_REGION: !Ref CognitoRegion
                    COGNITO_POOL: !Ref CognitoPool
                    COGNITO_JWK: !Ref CognitoJwk
                    COGNITO_REQUIRED_GROUP: !Ref CognitoRequiredGroup
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
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"RestApi\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"},{\"ParameterKey\":\"ProxyLambdaS3Key\",\"ParameterValue\":\"#{Octopus.Action[Upload Lambda Proxy].Package[].PackageId}.#{Octopus.Action[Upload Lambda Proxy].Package[].PackageVersion}.zip\"},{\"ParameterKey\":\"LambdaS3Bucket\",\"ParameterValue\":\"#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}\"},{\"ParameterKey\":\"LambdaName\",\"ParameterValue\":\"${local.lambda_name}\"},{\"ParameterKey\":\"LambdaDescription\",\"ParameterValue\":\"#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}\"},{\"ParameterKey\":\"CognitoRegion\",\"ParameterValue\":\"#{Cognito.Region}\"},{\"ParameterKey\":\"CognitoPool\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.CognitoPoolId}\"},{\"ParameterKey\":\"CognitoJwk\",\"ParameterValue\":\"#{Cognito.JWK}\"},{\"ParameterKey\":\"CognitoRequiredGroup\",\"ParameterValue\":\"#{Cognito.RequiredGroup}\"},{\"ParameterKey\":\"CognitoClientId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.CognitoClientId}\"},{\"ParameterKey\":\"LambdaVersion\",\"ParameterValue\":\"#{Octopus.Action[Deploy Application Lambda Version].Output.AwsOutputs[LambdaVersion]}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"RestApi\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"},{\"ParameterKey\":\"ProxyLambdaS3Key\",\"ParameterValue\":\"#{Octopus.Action[Upload Lambda Proxy].Package[].PackageId}.#{Octopus.Action[Upload Lambda Proxy].Package[].PackageVersion}.zip\"},{\"ParameterKey\":\"LambdaS3Bucket\",\"ParameterValue\":\"#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}\"},{\"ParameterKey\":\"LambdaName\",\"ParameterValue\":\"${local.lambda_name}\"},{\"ParameterKey\":\"LambdaDescription\",\"ParameterValue\":\"#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}\"},{\"ParameterKey\":\"CognitoRegion\",\"ParameterValue\":\"#{Cognito.Region}\"},{\"ParameterKey\":\"CognitoPool\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.CognitoPoolId}\"},{\"ParameterKey\":\"CognitoJwk\",\"ParameterValue\":\"#{Cognito.JWK}\"},{\"ParameterKey\":\"CognitoRequiredGroup\",\"ParameterValue\":\"#{Cognito.RequiredGroup}\"},{\"ParameterKey\":\"CognitoClientId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.CognitoClientId}\"},{\"ParameterKey\":\"LambdaVersion\",\"ParameterValue\":\"#{Octopus.Action[Deploy Application Lambda Version].Output.AwsOutputs[LambdaVersion]}\"}]"
        "Octopus.Action.Aws.IamCapabilities" : "[\"CAPABILITY_AUTO_EXPAND\",\"CAPABILITY_IAM\",\"CAPABILITY_NAMED_IAM\"]"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
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
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.cloudformation_lambda_version_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.lambda_proxy_version_cloudformation_name
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
                ProvisionedConcurrencyConfig:
                  ProvisionedConcurrentExecutions: 20
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
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"RestApi\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"},{\"ParameterKey\":\"LambdaDescription\",\"ParameterValue\":\"#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}\"},{\"ParameterKey\":\"ProxyLambda\",\"ParameterValue\":\"#{Octopus.Action[Deploy Reverse Proxy Lambda].Output.AwsOutputs[ProxyLambda]}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"RestApi\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"},{\"ParameterKey\":\"LambdaDescription\",\"ParameterValue\":\"#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}\"},{\"ParameterKey\":\"ProxyLambda\",\"ParameterValue\":\"#{Octopus.Action[Deploy Reverse Proxy Lambda].Output.AwsOutputs[ProxyLambda]}\"}]"
        "Octopus.Action.Aws.IamCapabilities" : "[\"CAPABILITY_AUTO_EXPAND\",\"CAPABILITY_IAM\",\"CAPABILITY_NAMED_IAM\"]"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
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
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.api_gateway_cloudformation_name
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          # This template links the reverse proxy to the API Gateway. Once this linking is done,
          # the API Gateway is ready to be deployed to a stage. But the old Lambda versions are
          # still referenced by the existing stage, so no changes have been exposed to the
          # end user.
          Parameters:
            EnvironmentName:
              Type: String
              Default: '#{Octopus.Environment.Name}'
            RestApi:
              Type: String
            ResourceId:
              Type: String
            ProxyLambdaVersion:
              Type: String
          Resources:
            ApiResource:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !Ref ResourceId
                PathPart: ${local.api_endpoint_name}
            ApiMethod:
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
                ResourceId: !Ref ApiResource
                RestApiId: !Ref RestApi
            'Deployment#{Octopus.Deployment.Id | Replace -}':
              Type: 'AWS::ApiGateway::Deployment'
              Properties:
                RestApiId: !Ref RestApi
              DependsOn:
                - ApiMethod
          Outputs:
            DeploymentId:
              Description: The deployment id
              Value: !Ref 'Deployment#{Octopus.Deployment.Id | Replace -}'
            EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"RestApi\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"},{\"ParameterKey\":\"ResourceId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.Api}\"},{\"ParameterKey\":\"ProxyLambdaVersion\",\"ParameterValue\":\"#{Octopus.Action[Deploy Reverse Proxy Lambda Version].Output.AwsOutputs[ProxyLambdaVersion]}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"RestApi\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"},{\"ParameterKey\":\"ResourceId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.Api}\"},{\"ParameterKey\":\"ProxyLambdaVersion\",\"ParameterValue\":\"#{Octopus.Action[Deploy Reverse Proxy Lambda Version].Output.AwsOutputs[ProxyLambdaVersion]}\"}]"
        "Octopus.Action.Aws.IamCapabilities" : "[\"CAPABILITY_AUTO_EXPAND\",\"CAPABILITY_IAM\",\"CAPABILITY_NAMED_IAM\"]"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
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
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormationStackName" : "#{CloudFormationName.ApiGatewayStage}"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.cloudformation_tags
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          # This template updates the stage with the deployment created in the previous step.
          # It is here that the new Lambda versions are exposed to the end user.
          Parameters:
            EnvironmentName:
              Type: String
              Default: '#{Octopus.Environment.Name}'
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
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"DeploymentId\",\"ParameterValue\":\"#{Octopus.Action[Update API Gateway].Output.AwsOutputs[DeploymentId]}\"},{\"ParameterKey\":\"ApiGatewayId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"DeploymentId\",\"ParameterValue\":\"#{Octopus.Action[Update API Gateway].Output.AwsOutputs[DeploymentId]}\"},{\"ParameterKey\":\"ApiGatewayId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"}]"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
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
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
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
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          STAGE_URL=$(aws cloudformation \
              describe-stacks \
              --stack-name #{CloudFormationName.ApiGatewayStage} \
              --query "Stacks[0].Outputs[?OutputKey=='StageURL'].OutputValue" \
              --output text)

          set_octopusvariable "StageURL" $${STAGE_URL}

          echo "Stage URL: $${STAGE_URL}"
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Check for vulnerabilities"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Check for vulnerabilities"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_security_environment_id,
        var.octopus_development_security_environment_id
      ]

      package {
        acquisition_location      = "Server"
        feed_id                   = var.octopus_built_in_feed_id
        name                      = local.lambda_sbom_package
        package_id                = local.lambda_sbom_package
        extract_during_deployment = true
        properties                = {
          SelectionMode = "immediate"
        }
      }

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          TIMESTAMP=$(date +%s%3N)
          SUCCESS=0
          for x in **/bom.xml; do
              # Delete any existing report file
              if [[ -f "$PWD/depscan-bom.json" ]]; then
                rm "$PWD/depscan-bom.json"
              fi

              # Generate the report, capturing the output, and ensuring $? is set to the exit code
              OUTPUT=$(bash -c "docker run --rm -v \"$PWD:/app\" appthreat/dep-scan scan --bom \"/app/bom.xml\" --type bom --report_file /app/depscan.json; exit \$?" 2>&1)

              # Success is set to 1 if the exit code is not zero
              if [[ $? -ne 0 ]]; then
                  SUCCESS=1
              fi

              # Report file is not generated if no threats found
              # https://github.com/ShiftLeftSecurity/sast-scan/issues/168
              if [[ -f "$PWD/depscan-bom.json" ]]; then
                new_octopusartifact "$PWD/depscan-bom.json"
                # The number of lines in the report file equals the number of vulnerabilities found
                COUNT=$(wc -l < "$PWD/depscan-bom.json")
              else
                COUNT=0
              fi

              # Print the output stripped of ANSI colour codes
              echo -e "$${OUTPUT}" | sed 's/\x1b\[[0-9;]*m//g'
          done

          set_octopusvariable "VerificationResult" $SUCCESS

          exit 0
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
}