resource "octopusdeploy_project" "deploy_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the app builder frontend."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = "Deploy App Builder Frontend"
  project_group_id                     = octopusdeploy_project_group.appbuilder_frontend_project_group.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  included_library_variable_sets       = [octopusdeploy_library_variable_set.frontend_library_variable_set.id]

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

output "deploy_project_id" {
  value = octopusdeploy_project.deploy_project.id
}

resource "octopusdeploy_deployment_process" "deploy_project" {
  project_id = octopusdeploy_project.deploy_project.id
  step {
    condition           = "Success"
    name                = "Deploy Frontend WebApp"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Create S3 Bucket"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : "[{\"key\":\"Environment\",\"value\":\"#{Octopus.Environment.Name}\"},{\"key\":\"Deployment Project\",\"value\":\"App Builder Frontend\"},{\"key\":\"Team\",\"value\":\"Content Marketing\"}]"
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
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
      }
    }
    action {
      action_type    = "Octopus.AwsUploadS3"
      name           = "Upload Frontend"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id

      primary_package {
        acquisition_location = "Server"
        feed_id = "Feeds-2301"
        package_id = "app-builder-frontend"
        properties = {
          SelectionMode = "immediate"
        }
      }

      properties = {
        "Octopus.Action.Aws.AssumeRole": "False"
        "Octopus.Action.Aws.Region": "#{AWS.Region}"
        "Octopus.Action.Aws.S3.BucketName": "#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[Bucket]}"
        "Octopus.Action.Aws.S3.FileSelections": "[{\"type\":\"MultipleFiles\",\"tags\":[],\"metadata\":[],\"cannedAcl\":\"private\",\"path\":\"\",\"storageClass\":\"STANDARD\",\"bucketKey\":\"\",\"bucketKeyPrefix\":\"#{Prefix}\",\"bucketKeyBehaviour\":\"Custom\",\"performVariableSubstitution\":\"False\",\"performStructuredVariableSubstitution\":\"False\",\"pattern\":\"**/*\",\"autoFocus\":true,\"structuredVariableSubstitutionPatterns\":\"config.json\"}]"
        "Octopus.Action.Aws.S3.TargetMode": "FileSelections"
        "Octopus.Action.AwsAccount.UseInstanceRole": "False"
        "Octopus.Action.AwsAccount.Variable": "AWS.Account"
        "Octopus.Action.Package.DownloadOnTentacle": "False"
      }
    }
  }
}