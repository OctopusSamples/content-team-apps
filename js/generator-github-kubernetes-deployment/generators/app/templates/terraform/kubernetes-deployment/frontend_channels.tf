resource "octopusdeploy_channel" "frontend_feature_branch" {
  name        = "Feature Branches"
  project_id  = octopusdeploy_project.deploy_frontend_project.id
  description = "The channel through which feature branches are deployed"
  depends_on = [octopusdeploy_project.deploy_frontend_project, octopusdeploy_deployment_process.deploy_frontend]
  rule {
    tag = ".+"
    action_package {
      deployment_action = "Deploy Frontend WebApp"
      package_reference = local.frontend_package_name
    }
  }
}

resource "octopusdeploy_channel" "frontend_mainline" {
  name        = "Mainline"
  project_id  = octopusdeploy_project.deploy_frontend_project.id
  description = "The channel through which mainline releases are deployed"
  depends_on = [octopusdeploy_project.deploy_frontend_project, octopusdeploy_deployment_process.deploy_frontend]
  is_default  = true
  rule {
    tag = "^$"
    action_package {
      deployment_action = "Deploy Frontend WebApp"
      package_reference = local.frontend_package_name
    }
  }
}