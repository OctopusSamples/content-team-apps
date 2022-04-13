locals {
  frontend_package_id = "pipeline-builder-github-frontend"
  frontend_package_sbom_id = "pipeline-builder-github-frontend-sbom"
}

resource "octopusdeploy_project" "deploy_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the Jenkins Pipelines Generator Frontend. Don't edit this process directly - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = "Jenkins Pipelines Generator Frontend"
  project_group_id                     = octopusdeploy_project_group.project_group.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  versioning_strategy {
    template = "#{Octopus.Version.LastMajor}.#{Octopus.Version.LastMinor}.#{Octopus.Version.LastPatch}.#{Octopus.Version.NextRevision}"
  }
  included_library_variable_sets = [
    octopusdeploy_library_variable_set.frontend_library_variable_set.id,
    "LibraryVariableSets-1181", # JenkinsPipelineBuilder
    "LibraryVariableSets-1223", # JenkinsPipelineShared
    "LibraryVariableSets-1243", # AWS Access
    "LibraryVariableSets-1282", # Content Team Apps
    "LibraryVariableSets-1262"  # Cognito
  ]

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
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
    name                = "Create S3 Bucket"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Create S3 Bucket"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]
      properties     = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : "[{\"key\":\"Environment\",\"value\":\"#{Octopus.Environment.Name}\"},{\"key\":\"Deployment Project\",\"value\":\"Deploy App Builder Frontend\"},{\"key\":\"Team\",\"value\":\"Content Marketing\"}]"
        "Octopus.Action.Aws.CloudFormationStackName" : "#{CloudFormation.S3Bucket}"
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
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"Hostname\",\"ParameterValue\":\"#{WebApp.Hostname}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"Hostname\",\"ParameterValue\":\"#{WebApp.Hostname}\"}]"
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
    name                = "Upload Frontend"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsUploadS3"
      name           = "Upload Frontend"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
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
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.Aws.S3.BucketName" : "#{Octopus.Action[Create S3 Bucket].Output.AwsOutputs[Bucket]}"
        "Octopus.Action.Aws.S3.FileSelections" : "[{\"type\":\"MultipleFiles\",\"tags\":[],\"metadata\":[],\"cannedAcl\":\"private\",\"path\":\"\",\"storageClass\":\"STANDARD\",\"bucketKey\":\"\",\"bucketKeyPrefix\":\"#{S3.Directory}/\",\"bucketKeyBehaviour\":\"Custom\",\"performVariableSubstitution\":\"False\",\"performStructuredVariableSubstitution\":\"False\",\"pattern\":\"**/*\",\"autoFocus\":true,\"structuredVariableSubstitutionPatterns\":\"config.json\"}]"
        "Octopus.Action.Aws.S3.TargetMode" : "FileSelections"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS"
        "Octopus.Action.Package.DownloadOnTentacle" : "False"
        "Octopus.Action.Package.FeedId" : var.octopus_built_in_feed_id
        "Octopus.Action.Package.PackageId" : local.frontend_package_id
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
        "Octopus.Action.AwsAccount.Variable" : "AWS"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          WEB_RESOURCE_ID=$(aws cloudformation \
              describe-stacks \
              --stack-name #{CloudFormationName.ApiGateway} \
              --query "Stacks[0].Outputs[?OutputKey=='Web'].OutputValue" \
              --output text)

          set_octopusvariable "Web" $${WEB_RESOURCE_ID}
          echo "We Resource ID: $WEB_RESOURCE_ID"

          REST_API=$(aws cloudformation \
              describe-stacks \
              --stack-name #{CloudFormationName.ApiGateway} \
              --query "Stacks[0].Outputs[?OutputKey=='RestApi'].OutputValue" \
              --output text)

          set_octopusvariable "RestApi" $${REST_API}
          echo "Rest API ID: $REST_API"

          ROOT_RESOURCE_ID=$(aws cloudformation \
              describe-stacks \
              --stack-name #{CloudFormationName.ApiGateway} \
              --query "Stacks[0].Outputs[?OutputKey=='RootResourceId'].OutputValue" \
              --output text)

          set_octopusvariable "RootResourceId" $${ROOT_RESOURCE_ID}
          echo "Root resource ID: $ROOT_RESOURCE_ID"
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
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
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : "[{\"key\":\"Environment\",\"value\":\"#{Octopus.Environment.Name}\"},{\"key\":\"Deployment Project\",\"value\":\"Deploy App Builder Frontend\"},{\"key\":\"Team\",\"value\":\"Content Marketing\"},{\"key\":\"Branch\",\"value\":\"#{if WebApp.SubPath}#{WebApp.SubPath}#{/if}#{unless WebApp.SubPath}main#{/unless}\"}]"
        "Octopus.Action.Aws.CloudFormationStackName" : "#{CloudFormation.Frontend}"
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          Parameters:
            EnvironmentName:
              Type: String
              Default: '#{Octopus.Environment.Name}'
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
            IsFeatureBranch: !Not
              - !Equals
                - !Ref SubPath
                - ''
          Resources:
            BranchResource:
              Type: 'AWS::ApiGateway::Resource'
              Condition: IsFeatureBranch
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !Ref RootResourceId
                PathPart: !Ref SubPath
            BranchResourceProxy:
              Type: 'AWS::ApiGateway::Resource'
              Condition: IsFeatureBranch
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !Ref BranchResource
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
                  Uri: !Join
                    - ''
                    - - 'http://'
                      - !Ref BucketName
                      - .s3-website-us-west-1.amazonaws.com/
                      - !Ref PackageId
                      - .
                      - !Ref PackageVersion
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
                        method.response.header.Permissions-Policy: >-
                          'accelerometer=(), ambient-light-sensor=(), autoplay=(),
                          battery=(), camera=(), cross-origin-isolated=(),
                          display-capture=(), document-domain=(), encrypted-media=(),
                          execution-while-not-rendered=(),
                          execution-while-out-of-viewport=(), fullscreen=(),
                          geolocation=(), gyroscope=(), keyboard-map=(), magnetometer=(),
                          microphone=(), midi=(), navigation-override=(), payment=(),
                          picture-in-picture=(), publickey-credentials-get=(),
                          screen-wake-lock=(), sync-xhr=(), usb=(), web-share=(),
                          xr-spatial-tracking=(), clipboard-read=(), clipboard-write=*,
                          gamepad=(), speaker-selection=(), conversion-measurement=(),
                          focus-without-user-activation=(), hid=(), idle-detection=(),
                          interest-cohort=(), serial=(), sync-script=(),
                          trust-token-redemption=(), window-placement=(),
                          vertical-scroll=()'
                        method.response.header.Content-Security-Policy: >-
                          'frame-ancestors 'none'; form-action 'none'; base-uri 'none';
                          object-src 'none'; default-src 'self' 'unsafe-inline'
                          *.google-analytics.com *.amazonaws.com; script-src 'self'
                          'unsafe-inline' *.google-analytics.com *.googletagmanager.com;
                          style-src * 'unsafe-inline'; img-src *; font-src *'
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
                ResourceId: !If
                  - IsFeatureBranch
                  - !Ref BranchResource
                  - !Ref RootResourceId
                RestApiId: !Ref RestApi
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
                  Uri: !Join
                    - ''
                    - - 'http://'
                      - !Ref BucketName
                      - .s3-website-us-west-1.amazonaws.com/
                      - !Ref PackageId
                      - .
                      - !Ref PackageVersion
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
                        method.response.header.Permissions-Policy: >-
                          'accelerometer=(), ambient-light-sensor=(), autoplay=(),
                          battery=(), camera=(), cross-origin-isolated=(),
                          display-capture=(), document-domain=(), encrypted-media=(),
                          execution-while-not-rendered=(),
                          execution-while-out-of-viewport=(), fullscreen=(),
                          geolocation=(), gyroscope=(), keyboard-map=(), magnetometer=(),
                          microphone=(), midi=(), navigation-override=(), payment=(),
                          picture-in-picture=(), publickey-credentials-get=(),
                          screen-wake-lock=(), sync-xhr=(), usb=(), web-share=(),
                          xr-spatial-tracking=(), clipboard-read=(), clipboard-write=*,
                          gamepad=(), speaker-selection=(), conversion-measurement=(),
                          focus-without-user-activation=(), hid=(), idle-detection=(),
                          interest-cohort=(), serial=(), sync-script=(),
                          trust-token-redemption=(), window-placement=(),
                          vertical-scroll=()'
                        method.response.header.Content-Security-Policy: >-
                          'frame-ancestors 'none'; form-action 'none'; base-uri 'none';
                          object-src 'none'; default-src 'self' 'unsafe-inline'
                          *.google-analytics.com *.amazonaws.com; script-src 'self'
                          'unsafe-inline' *.google-analytics.com *.googletagmanager.com;
                          style-src * 'unsafe-inline'; img-src *; font-src *'
                        method.response.header.Strict-Transport-Security: '''max-age=15768000'''
                    - StatusCode: '301'
                      SelectionPattern: '301'
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
                    StatusCode: '301'
                    ResponseParameters:
                      method.response.header.Location: true
                ResourceId: !If
                  - IsFeatureBranch
                  - !Ref BranchResourceProxy
                  - !Ref ResourceId
                RestApiId: !Ref RestApi
            'Deployment#{Octopus.Deployment.Id | Replace -}':
              Type: 'AWS::ApiGateway::Deployment'
              Properties:
                RestApiId: !Ref RestApi
              DependsOn:
                - FrontendMethodOne
                - FrontendMethodTwo
          Outputs:
            DeploymentId:
              Description: The deployment id
              Value: !Ref 'Deployment#{Octopus.Deployment.Id | Replace -}'
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"RestApi\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"},{\"ParameterKey\":\"RootResourceId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RootResourceId}\"},{\"ParameterKey\":\"ResourceId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.Web}\"},{\"ParameterKey\":\"PackageVersion\",\"ParameterValue\":\"#{Octopus.Action[Upload Frontend].Package[].PackageVersion}\"},{\"ParameterKey\":\"PackageId\",\"ParameterValue\":\"#{Octopus.Action[Upload Frontend].Package[].PackageId}\"},{\"ParameterKey\":\"BucketName\",\"ParameterValue\":\"#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[Bucket]}\"},{\"ParameterKey\":\"SubPath\",\"ParameterValue\":\"#{Frontend.SubPath}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"RestApi\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"},{\"ParameterKey\":\"RootResourceId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RootResourceId}\"},{\"ParameterKey\":\"ResourceId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.Web}\"},{\"ParameterKey\":\"PackageVersion\",\"ParameterValue\":\"#{Octopus.Action[Upload Frontend].Package[].PackageVersion}\"},{\"ParameterKey\":\"PackageId\",\"ParameterValue\":\"#{Octopus.Action[Upload Frontend].Package[].PackageId}\"},{\"ParameterKey\":\"BucketName\",\"ParameterValue\":\"#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[Bucket]}\"},{\"ParameterKey\":\"SubPath\",\"ParameterValue\":\"#{Frontend.SubPath}\"}]"
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
    name                = "Update Stage"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Update Stage"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : "[{\"key\":\"Environment\",\"value\":\"#{Octopus.Environment.Name}\"},{\"key\":\"Deployment Project\",\"value\":\"Deploy App Builder Frontend\"},{\"key\":\"Team\",\"value\":\"Content Marketing\"}]"
        "Octopus.Action.Aws.CloudFormationStackName" : "#{CloudFormationName.ApiGatewayStage}"
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
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
                DeploymentId: !Sub '$${DeploymentId}'
                RestApiId: !Sub '$${ApiGatewayId}'
                StageName: !Sub '$${EnvironmentName}'
                Variables:
                  indexPage: !Sub /index.html
          Outputs:
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
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"DeploymentId\",\"ParameterValue\":\"#{Octopus.Action[Proxy with API Gateway].Output.AwsOutputs[DeploymentId]}\"},{\"ParameterKey\":\"ApiGatewayId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"DeploymentId\",\"ParameterValue\":\"#{Octopus.Action[Proxy with API Gateway].Output.AwsOutputs[DeploymentId]}\"},{\"ParameterKey\":\"ApiGatewayId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"}]"
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
        "Octopus.Action.AwsAccount.Variable" : "AWS"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          STARGE_URL=$(aws cloudformation \
              describe-stacks \
              --stack-name #{CloudFormationName.ApiGatewayStage} \
              --query "Stacks[0].Outputs[?OutputKey=='StageURL'].OutputValue" \
              --output text)

          set_octopusvariable "StageURL" $${STARGE_URL}

          echo "Stage URL: $${STARGE_URL}"
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
        name                      = local.frontend_package_sbom_id
        package_id                = local.frontend_package_sbom_id
        extract_during_deployment = true
        properties                = {
          SelectionMode = "immediate"
        }
      }

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          TIMESTAMP=$(date +%s%3N)
          SUCCESS=0
          for x in $(find . -name bom.xml -type f -print); do
              # Delete any existing report file
              if [[ -f "$PWD/depscan-bom.json" ]]; then
                rm "$PWD/depscan-bom.json"
              fi

              # Generate the report, capturing the output, and ensuring $? is set to the exit code
              OUTPUT=$(bash -c "docker run --rm -v \"$PWD:/app\" appthreat/dep-scan scan --bom \"/app/$${x}\" --type bom --report_file /app/depscan.json; exit \$?" 2>&1)

              # Success is set to 1 if the exit code is not zero
              if [[ $? -ne 0 ]]; then
                  SUCCESS=1
              fi

              # Print the output stripped of ANSI colour codes
              echo -e "$${OUTPUT}" | sed 's/\x1b\[[0-9;]*m//g'
          done

          set_octopusvariable "VerificationResult" $SUCCESS

          if [[  $SUCCESS -ne 0 ]]; then
            >&2 echo "Vulnerabilities were detected"
          fi

          exit 0
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
}