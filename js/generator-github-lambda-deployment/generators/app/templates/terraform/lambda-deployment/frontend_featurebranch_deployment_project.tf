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

locals {

}

resource "octopusdeploy_deployment_process" "deploy_frontend_featurebranch" {
  project_id = octopusdeploy_project.deploy_frontend_featurebranch_project.id

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
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
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
        package_id           = local.frontend_package_name
        properties           = {
          "SelectionMode" : "immediate"
        }
      }

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.Aws.S3.BucketName" : "#{Octopus.Action[Create S3 bucket].Output.AwsOutputs[Bucket]}"
        "Octopus.Action.Aws.S3.FileSelections" : "[{\"type\":\"MultipleFiles\",\"tags\":[],\"metadata\":[],\"cannedAcl\":\"private\",\"path\":\"\",\"storageClass\":\"STANDARD\",\"bucketKey\":\"\",\"bucketKeyPrefix\":\"#{S3.Directory}/\",\"bucketKeyBehaviour\":\"Custom\",\"performVariableSubstitution\":\"False\",\"performStructuredVariableSubstitution\":\"False\",\"pattern\":\"**/*\",\"autoFocus\":true,\"structuredVariableSubstitutionPatterns\":\"config.json\"}]"
        "Octopus.Action.Aws.S3.TargetMode" : "FileSelections"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
        "Octopus.Action.Package.DownloadOnTentacle" : "False"
        "Octopus.Action.Package.FeedId" : var.octopus_built_in_feed_id
        "Octopus.Action.Package.PackageId" : local.frontend_package_name
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