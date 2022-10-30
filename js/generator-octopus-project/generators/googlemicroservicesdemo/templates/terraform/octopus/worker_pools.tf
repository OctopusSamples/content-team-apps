data "octopusdeploy_worker_pools" "ubuntu_worker_pool" {
  partial_name = "Hosted Ubuntu"
  take = 1
}

data "octopusdeploy_worker_pools" "windows_worker_pool" {
  partial_name = "Hosted Windows"
  take = 1
}