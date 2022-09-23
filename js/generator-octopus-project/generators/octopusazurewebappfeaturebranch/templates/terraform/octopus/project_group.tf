data "octopusdeploy_project_groups" "project_group" {
  partial_name = var.octopus_project_group_name
  skip         = 0
  take         = 1
}

resource "octopusdeploy_project_group" "project_group" {
  name  = var.octopus_project_group_name
  count = var.existing_project_group ? 0 : 1
}
