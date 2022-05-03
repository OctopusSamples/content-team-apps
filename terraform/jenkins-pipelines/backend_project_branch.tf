locals {
  # These change with every project
  project_name_featurebranch        = "Jenkins Pipelines Generator Feature Branch"
  project_description_featurebranch = "which exposes an endpoint for creating Jenkins pipelines."

  lambda_cloudformation_name_featurebranch = "#{CloudFormation.Backend}-#{Octopus.Action[Upload Lambda].Package[].PackageVersion | VersionPreRelease}"
  lambda_name_featurebranch                = "#{Lambda.Name}-#{Octopus.Action[Upload Lambda].Package[].PackageVersion | VersionPreRelease}"
}

resource "octopusdeploy_project" "backend_project_featurebranch" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the ${local.project_name_featurebranch}, ${local.project_description_featurebranch} Don't edit this process directly - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = local.project_name_featurebranch
  project_group_id                     = octopusdeploy_project_group.project_group.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  versioning_strategy {
    template = "#{Octopus.Version.LastMajor}.#{Octopus.Version.LastMinor}.#{Octopus.Version.LastPatch}.#{Octopus.Version.NextRevision}"
  }
  included_library_variable_sets = [
    "LibraryVariableSets-1183", # GitHubActionBuilder
    "LibraryVariableSets-1222", # GitHubActionShared
    "LibraryVariableSets-1243", # AWS Access
    "LibraryVariableSets-1282", # Content Team Apps
    "LibraryVariableSets-1262", # Cognito
    "LibraryVariableSets-1261" # Google
  ]

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

resource "octopusdeploy_variable" "backend_featurebranch_debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.backend_project_featurebranch.id
  value        = "False"
}

resource "octopusdeploy_variable" "backend_featurebranch_debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.backend_project_featurebranch.id
  value        = "False"
}

resource "octopusdeploy_deployment_process" "backend_project_featurebranch" {
  project_id = octopusdeploy_project.backend_project_featurebranch.id
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
        "Octopus.Action.AwsAccount.Variable" : "AWS"
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
        "Octopus.Action.AwsAccount.Variable" : "AWS"
        "Octopus.Action.Package.DownloadOnTentacle" : "False"
        "Octopus.Action.Package.FeedId" : var.octopus_built_in_feed_id
        "Octopus.Action.Package.PackageId" : local.lambda_package
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
      notes          = "Feature branch Lambdas deploy over each other and expose the default latest alias. We don't use the same versioning strategy as the mainline deployment, as feature branches don't need the same zero-downtime deployments."
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : local.lambda_cloudformation_name_featurebranch
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          # This stack creates a new application lambda.
          Parameters:
            EnvironmentName:
              Type: String
              Default: '#{Octopus.Environment.Name}'
            LambdaS3Key:
              Type: String
            LambdaS3Bucket:
              Type: String
            GithubClientId:
              Type: String
            GithubClientSecret:
              Type: String
            LambdaName:
              Type: String
            LambdaDescription:
              Type: String
            GithubEncryption:
              Type: String
            GithubSalt:
              Type: String
            GithubLoginPage:
              Type: String
            CognitoAuditClientId:
              Type: String
            CognitoAuditClientSecret:
              Type: String
            CognitoService:
              Type: String
            AuditService:
              Type: String
            CognitoRegion:
              Type: String
            CognitoJwk:
              Type: String
            CognitoRequiredGroup:
              Type: String
            ServiceBusProxyClientId:
              Type: String
            ServiceBusProxyClientSecret:
              Type: String
            ServiceBusUrl:
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
                    GITHUB_CLIENT_ID: !Ref GithubClientId
                    GITHUB_CLIENT_SECRET: !Ref GithubClientSecret
                    GITHUB_ENCRYPTION: !Ref GithubEncryption
                    GITHUB_SALT: !Ref GithubSalt
                    GITHUB_LOGIN_PAGE: !Ref GithubLoginPage
                    COGNITO_AUDIT_CLIENT_ID: !Ref CognitoAuditClientId
                    COGNITO_AUDIT_CLIENT_SECRET: !Ref CognitoAuditClientSecret
                    COGNITO_JWK: !Ref CognitoJwk
                    COGNITO_SERVICE: !Ref CognitoService
                    AUDIT_SERVICE: !Ref AuditService
                    COGNITO_AZURE_SERVICE_BUS_PROXY_CLIENT_ID: !Ref ServiceBusProxyClientId
                    COGNITO_AZURE_SERVICE_BUS_PROXY_CLIENT_SECRET: !Ref ServiceBusProxyClientSecret
                    SERVICE_BUS_SERVICE: !Ref ServiceBusUrl
                FunctionName: !Sub '$${EnvironmentName}-$${LambdaName}'
                Handler: not.used.in.provided.runtime
                MemorySize: 128
                PackageType: Zip
                Role: !GetAtt
                  - IamRoleLambdaExecution
                  - Arn
                Runtime: provided
                Timeout: 600
          Outputs:
            ApplicationLambda:
              Description: The Lambda ref
              Value: !Ref ApplicationLambda
            EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"LambdaS3Key\",\"ParameterValue\":\"#{Octopus.Action[Upload Lambda].Package[].PackageId}.#{Octopus.Action[Upload Lambda].Package[].PackageVersion}.zip\"},{\"ParameterKey\":\"LambdaS3Bucket\",\"ParameterValue\":\"#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}\"},{\"ParameterKey\":\"GithubClientId\",\"ParameterValue\":\"#{GitHub.GitHubAppClientId}\"},{\"ParameterKey\":\"GithubClientSecret\",\"ParameterValue\":\"#{GitHub.GitHubAppClientSecret}\"},{\"ParameterKey\":\"LambdaName\",\"ParameterValue\":\"${local.lambda_name_featurebranch}\"},{\"ParameterKey\":\"LambdaDescription\",\"ParameterValue\":\"#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion} #{Octopus.Deployment.Id}\"},{\"ParameterKey\":\"GithubEncryption\",\"ParameterValue\":\"#{Client.EncryptionKey}\"},{\"ParameterKey\":\"GithubSalt\",\"ParameterValue\":\"#{Client.EncryptionSalt}\"},{\"ParameterKey\":\"GithubLoginPage\",\"ParameterValue\":\"/oauth/github/login\"},{\"ParameterKey\":\"CognitoAuditClientId\",\"ParameterValue\":\"#{Cognito.AuditClientId}\"},{\"ParameterKey\":\"CognitoAuditClientSecret\",\"ParameterValue\":\"#{Cognito.AuditClientSecret}\"},{\"ParameterKey\":\"CognitoService\",\"ParameterValue\":\"#{Cognito.Service}\"},{\"ParameterKey\":\"AuditService\",\"ParameterValue\":\"#{ApiGateway.ContentTeamUrl}\"},{\"ParameterKey\":\"CognitoRegion\",\"ParameterValue\":\"#{Cognito.Region}\"},{\"ParameterKey\":\"CognitoJwk\",\"ParameterValue\":\"#{Cognito.JWK}\"},{\"ParameterKey\":\"CognitoRequiredGroup\",\"ParameterValue\":\"#{Cognito.RequiredGroup}\"},{\"ParameterKey\":\"ServiceBusProxyClientId\",\"ParameterValue\":\"#{Cognito.ServiceBusProxyClientId}\"},{\"ParameterKey\":\"ServiceBusProxyClientSecret\",\"ParameterValue\":\"#{Cognito.ServiceBusProxyClientSecret}\"},{\"ParameterKey\":\"ServiceBusUrl\",\"ParameterValue\":\"#{ServiceBusProxy.Url}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"LambdaS3Key\",\"ParameterValue\":\"#{Octopus.Action[Upload Lambda].Package[].PackageId}.#{Octopus.Action[Upload Lambda].Package[].PackageVersion}.zip\"},{\"ParameterKey\":\"LambdaS3Bucket\",\"ParameterValue\":\"#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[LambdaS3Bucket]}\"},{\"ParameterKey\":\"GithubClientId\",\"ParameterValue\":\"#{GitHub.GitHubAppClientId}\"},{\"ParameterKey\":\"GithubClientSecret\",\"ParameterValue\":\"#{GitHub.GitHubAppClientSecret}\"},{\"ParameterKey\":\"LambdaName\",\"ParameterValue\":\"${local.lambda_name_featurebranch}\"},{\"ParameterKey\":\"LambdaDescription\",\"ParameterValue\":\"#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion} #{Octopus.Deployment.Id}\"},{\"ParameterKey\":\"GithubEncryption\",\"ParameterValue\":\"#{Client.EncryptionKey}\"},{\"ParameterKey\":\"GithubSalt\",\"ParameterValue\":\"#{Client.EncryptionSalt}\"},{\"ParameterKey\":\"GithubLoginPage\",\"ParameterValue\":\"/oauth/github/login\"},{\"ParameterKey\":\"CognitoAuditClientId\",\"ParameterValue\":\"#{Cognito.AuditClientId}\"},{\"ParameterKey\":\"CognitoAuditClientSecret\",\"ParameterValue\":\"#{Cognito.AuditClientSecret}\"},{\"ParameterKey\":\"CognitoService\",\"ParameterValue\":\"#{Cognito.Service}\"},{\"ParameterKey\":\"AuditService\",\"ParameterValue\":\"#{ApiGateway.ContentTeamUrl}\"},{\"ParameterKey\":\"CognitoRegion\",\"ParameterValue\":\"#{Cognito.Region}\"},{\"ParameterKey\":\"CognitoJwk\",\"ParameterValue\":\"#{Cognito.JWK}\"},{\"ParameterKey\":\"CognitoRequiredGroup\",\"ParameterValue\":\"#{Cognito.RequiredGroup}\"},{\"ParameterKey\":\"ServiceBusProxyClientId\",\"ParameterValue\":\"#{Cognito.ServiceBusProxyClientId}\"},{\"ParameterKey\":\"ServiceBusProxyClientSecret\",\"ParameterValue\":\"#{Cognito.ServiceBusProxyClientSecret}\"},{\"ParameterKey\":\"ServiceBusUrl\",\"ParameterValue\":\"#{ServiceBusProxy.Url}\"}]"
        "Octopus.Action.Aws.IamCapabilities" : "[\"CAPABILITY_AUTO_EXPAND\",\"CAPABILITY_IAM\",\"CAPABILITY_NAMED_IAM\"]"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Print Routing Info"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Print Routing Info"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          LAMBDA_NAME=$(aws cloudformation \
              describe-stacks \
              --stack-name ${local.lambda_cloudformation_name_featurebranch} \
              --query "Stacks[0].Outputs[?OutputKey=='ApplicationLambda'].OutputValue" \
              --output text)

          echo "Lambda Name: $${LAMBDA_NAME}"
          echo "To call this Lambda, use a routing header like:"
          echo "route[/api/pipeline/jenkins/generate:GET]=lambda[$${LAMBDA_NAME}]"
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
}