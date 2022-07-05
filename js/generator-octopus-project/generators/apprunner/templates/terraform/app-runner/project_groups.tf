resource "octopusdeploy_project_group" "apprunner_project_group" {
  description  = "The App Runner service."
  name         = "App Runner"
}

output "apprunner_project_group_id" {
  value = octopusdeploy_project_group.apprunner_project_group.id
}