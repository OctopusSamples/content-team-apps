resource "octopusdeploy_project_group" "appbuilder_github_oauth_project_group" {
  description  = "The GitHub Repo Creator."
  name         = "GitHub Repo Creator"
}

output "appbuilder_github_oauth_project_group_id" {
  value = octopusdeploy_project_group.appbuilder_github_oauth_project_group.id
}