resource "octopusdeploy_project" "deploy_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the Octopus Template Generator. Don't edit this process directly - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = "Octopus Template Generator"
  project_group_id                     = octopusdeploy_project_group.appbuilder_github_oauth_project_group.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  versioning_strategy {
    template = "#{Octopus.Version.LastMajor}.#{Octopus.Version.LastMinor}.#{Octopus.Version.LastPatch}.#{Octopus.Version.NextRevision}"
  }
  included_library_variable_sets = [
    octopusdeploy_library_variable_set.library_variable_set.id, var.cognito_library_variable_set_id,
    var.content_team_library_variable_set_id
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
        "Octopus.Action.Aws.CloudFormation.Tags" : "[{\"key\":\"Environment\",\"value\":\"#{Octopus.Environment.Name}\"},{\"key\":\"Deployment Project\",\"value\":\"Deploy Octopus Service Account Creator\"},{\"key\":\"Team\",\"value\":\"Content Marketing\"}]"
        "Octopus.Action.Aws.CloudFormationStackName" : "#{CloudFormation.S3Bucket}"
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
        package_id           = "octopus-template-generator-lambda"
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
        "Octopus.Action.Package.PackageId" : "octopus-template-generator-lambda"
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
        package_id           = "com.octopus:reverse-proxy"
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
        "Octopus.Action.Package.PackageId" : "com.octopus:reverse-proxy"
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
              --stack-name #{CloudFormationName.AppBuilderApiGateway} \
              --query "Stacks[0].Outputs[?OutputKey=='Api'].OutputValue" \
              --output text)

          set_octopusvariable "Api" $${API_RESOURCE}

          echo "API Resource ID: $${API_RESOURCE}"

          if [[ -z "$${API_RESOURCE}" ]]; then
            echo "Run the App Builder shared infrastructure project first"
            exit 1
          fi

          REST_API=$(aws cloudformation \
              describe-stacks \
              --stack-name #{CloudFormationName.AppBuilderApiGateway} \
              --query "Stacks[0].Outputs[?OutputKey=='RestApi'].OutputValue" \
              --output text)

          set_octopusvariable "RestApi" $${REST_API}

          echo "Rest Api ID: $${REST_API}"

          if [[ -z "$${REST_API}" ]]; then
            echo "Run the App Builder shared infrastructure project first"
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
    name                = "Deploy Octopus Template Generator"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy Octopus Template Generator"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : "[{\"key\":\"Environment\",\"value\":\"#{Octopus.Environment.Name}\"},{\"key\":\"Deployment Project\",\"value\":\"GitHub OAuth Backend\"},{\"key\":\"Team\",\"value\":\"Content Marketing\"}]"
        "Octopus.Action.Aws.CloudFormationStackName" : "#{CloudFormation.OctopusTemplateGenerator}"
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
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
            OctopusDisableTemplateGeneration:
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
                    DEFAULT_LAMBDA: !Ref 'LambdaVersion#{Octopus.Deployment.Id | Replace -}'
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
                Timeout: 30
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
                    OCTOPUS_DISABLE_TEMPLATE_GENERATION: !Ref OctopusDisableTemplateGeneration
                FunctionName: !Sub '$${EnvironmentName}-$${LambdaName}'
                Handler: application/lambda/generate-template.lambdaHandler
                MemorySize: 1024
                PackageType: Zip
                Role: !GetAtt
                  - IamRoleLambdaExecution
                  - Arn
                Runtime: nodejs14.x
                Timeout: 30
            'LambdaVersion#{Octopus.Deployment.Id | Replace -}':
              Type: 'AWS::Lambda::Version'
              Properties:
                FunctionName: !Ref ApplicationLambda
                Description: !Ref LambdaDescription
            ApplicationLambdaPermissions:
              Type: 'AWS::Lambda::Permission'
              Properties:
                FunctionName: !Ref 'LambdaVersion#{Octopus.Deployment.Id | Replace -}'
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
            ApiGenerateTemplateResource:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !Ref ResourceId
                PathPart: generatetemplate
            ApiGenerateTemplateMethod:
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
                      - !GetAtt
                        - ProxyLambda
                        - Arn
                      - /invocations
                ResourceId: !Ref ApiGenerateTemplateResource
                RestApiId: !Ref RestApi
            'Deployment#{Octopus.Deployment.Id | Replace -}':
              Type: 'AWS::ApiGateway::Deployment'
              Properties:
                RestApiId: !Ref RestApi
              DependsOn:
                - ApiGenerateTemplateMethod
          Outputs:
            DeploymentId:
              Description: The deployment id
              Value: !Ref 'Deployment#{Octopus.Deployment.Id | Replace -}'
            LambdaVersion:
              Description: The name of the Lambda version resource deployed by this template
              Value: 'LambdaVersion#{Octopus.Deployment.Id | Replace -}'
            LambdaRef:
              Description: The Lambda reference
              Value: !Ref ApplicationLambda
            LambdaDescription:
              Description: The Lambda description
              Value: !Ref LambdaDescription
            EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : "#{Octopus.Environment.Name}"
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
            ParameterKey : "OctopusDisableTemplateGeneration"
            ParameterValue : "#{Service.Disable}"
          },
          {
            ParameterKey : "LambdaName"
            ParameterValue : "#{Lambda.Name}"
          },
          {
            ParameterKey : "LambdaDescription"
            ParameterValue : "#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}"
          },
          {
            ParameterKey : "CognitoPool"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.CognitoPoolId}"
          },
          {
            ParameterKey : "CognitoJwk"
            ParameterValue : "#{Cognito.JWK}"
          },
          {
            ParameterKey : "CognitoRequiredGroup"
            ParameterValue : "#{Cognito.RequiredGroup}"
          },
          {
            ParameterKey : "CognitoRegion"
            ParameterValue : "#{Cognito.Region}"
          },
          {
            ParameterKey : "ProxyLambdaS3Key"
            ParameterValue : "#{Octopus.Action[Upload Lambda Proxy].Package[].PackageId}.#{Octopus.Action[Upload Lambda Proxy].Package[].PackageVersion}.zip"
          }
        ])
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : jsonencode([
          {
            ParameterKey : "EnvironmentName"
            ParameterValue : "#{Octopus.Environment.Name}"
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
            ParameterKey : "OctopusDisableTemplateGeneration"
            ParameterValue : "#{Service.Disable}"
          },
          {
            ParameterKey : "LambdaName"
            ParameterValue : "#{Lambda.Name}"
          },
          {
            ParameterKey : "LambdaDescription"
            ParameterValue : "#{Octopus.Deployment.Id} v#{Octopus.Action[Upload Lambda].Package[].PackageVersion}"
          },
          {
            ParameterKey : "CognitoPool"
            ParameterValue : "#{Octopus.Action[Get Stack Outputs].Output.CognitoPoolId}"
          },
          {
            ParameterKey : "CognitoJwk"
            ParameterValue : "#{Cognito.JWK}"
          },
          {
            ParameterKey : "CognitoRequiredGroup"
            ParameterValue : "#{Cognito.RequiredGroup}"
          },
          {
            ParameterKey : "CognitoRegion"
            ParameterValue : "#{Cognito.Region}"
          },
          {
            ParameterKey : "ProxyLambdaS3Key"
            ParameterValue : "#{Octopus.Action[Upload Lambda Proxy].Package[].PackageId}.#{Octopus.Action[Upload Lambda Proxy].Package[].PackageVersion}.zip"
          }
        ])
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
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormationStackName" : "#{CloudFormationName.AppBuilderApiGatewayStage}"
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
                DeploymentId:
                  'Fn::Sub': '$${DeploymentId}'
                RestApiId:
                  'Fn::Sub': '$${ApiGatewayId}'
                StageName:
                  'Fn::Sub': '$${EnvironmentName}'
                Variables:
                  indexPage:
                    'Fn::Sub': '/$${EnvironmentName}/index.html'
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
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"DeploymentId\",\"ParameterValue\":\"#{Octopus.Action[Deploy Octopus Template Generator].Output.AwsOutputs[DeploymentId]}\"},{\"ParameterKey\":\"ApiGatewayId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"EnvironmentName\",\"ParameterValue\":\"#{Octopus.Environment.Name}\"},{\"ParameterKey\":\"DeploymentId\",\"ParameterValue\":\"#{Octopus.Action[Deploy Octopus Template Generator].Output.AwsOutputs[DeploymentId]}\"},{\"ParameterKey\":\"ApiGatewayId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.RestApi}\"}]"
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
              --stack-name #{CloudFormationName.AppBuilderApiGatewayStage} \
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
        name                      = "octopus-template-generator-lambda-sbom"
        package_id                = "octopus-template-generator-lambda-sbom"
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