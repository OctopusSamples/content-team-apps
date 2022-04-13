resource "octopusdeploy_project_group" "appbuilder_frontend_project_group" {
  description  = "The GitHub Actions Workflows Generator."
  name         = "GitHub Actions Workflows Generator"
}

output "appbuilder_frontend_project_group_id" {
  value = octopusdeploy_project_group.appbuilder_frontend_project_group.id
}