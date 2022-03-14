resource "octopusdeploy_project_group" "appbuilder_github_oauth_project_group" {
  description  = "The Octopus Service Account Creator."
  name         = "Octopus Service Account Creator"
}

output "appbuilder_github_oauth_project_group_id" {
  value = octopusdeploy_project_group.appbuilder_github_oauth_project_group.id
}