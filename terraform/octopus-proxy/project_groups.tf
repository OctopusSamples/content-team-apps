resource "octopusdeploy_project_group" "appbuilder_github_oauth_project_group" {
  description  = "The ${local.project_name}."
  name         = local.project_name
}

output "appbuilder_github_oauth_project_group_id" {
  value = octopusdeploy_project_group.appbuilder_github_oauth_project_group.id
}