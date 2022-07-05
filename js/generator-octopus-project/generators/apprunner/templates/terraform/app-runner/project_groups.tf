data "octopusdeploy_project_groups" "apprunner_project_group" {
  partial_name = var.octopus_project_group_name
  skip         = 0
  take         = 1
}