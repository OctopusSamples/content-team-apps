resource "octopusdeploy_channel" "backend_feature_branch" {
  name        = "Feature Branches"
  project_id  = octopusdeploy_project.deploy_backend_featurebranch_project.id
  description = "The channel through which feature branches are deployed"
  depends_on  = [
    octopusdeploy_project.deploy_backend_featurebranch_project,
    octopusdeploy_deployment_process.deploy_backend_featurebranch
  ]
  is_default  = true
  rule {
    tag = ".+"
    action_package {
      deployment_action = "Backend Service"
      package_reference = local.backend_package_name
    }
    action_package {
      deployment_action = "Check for Vulnerabilities"
      package_reference = "products-microservice-sbom"
    }
  }
}

resource "octopusdeploy_channel" "backend_mainline" {
  name        = "Mainline"
  project_id  = octopusdeploy_project.deploy_backend_project.id
  description = "The channel through which mainline releases are deployed"
  depends_on  = [octopusdeploy_project.deploy_backend_project, octopusdeploy_deployment_process.deploy_backend]
  is_default  = true
  rule {
    tag = "^$"
    action_package {
      deployment_action = "Backend Service"
      package_reference = local.backend_package_name
    }
    action_package {
      deployment_action = "Check for Vulnerabilities"
      package_reference = "products-microservice-sbom"
    }
  }
}