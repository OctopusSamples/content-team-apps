data "octopusdeploy_projects" "project" {
  name                   = var.channel_project_name
  skip                   = 0
  take                   = 1
}