resource "octopusdeploy_space" "app_builder_space" {
  description                 = "A space created by app builder."
  name                        = var.octopus_space
  is_default                  = false
  is_task_queue_stopped       = false
  space_managers_team_members = [var.octopus_user_id]
}