resource "octopusdeploy_project_group" "appbuilder_github_oauth_project_group" {
  description  = "The Octopus Template Generator."
  name         = "Octopus Template Generator"
}

output "appbuilder_github_oauth_project_group_id" {
  value = octopusdeploy_project_group.appbuilder_github_oauth_project_group.id
}