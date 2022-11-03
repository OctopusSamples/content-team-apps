locals {
  versioning_strategy      = "#{Octopus.Version.LastMajor}.#{Octopus.Version.LastMinor}.#{Octopus.Version.LastPatch}.#{Octopus.Version.NextRevision}"
  worker_image             = "octopusdeploy/worker-tools:3-ubuntu.18.04"
  namespace                = "${var.namespace_prefix}onlineboutique-#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}"
  feature_branch_namespace = "${var.namespace_prefix}onlineboutique-development"
  deployment_step          = "Deploy App"
  deployment_role          = "demo-k8s-cluster"
  worker_pool_id           = data.octopusdeploy_worker_pools.vm_worker_pool.worker_pools[0].id
  # worker_pool_id      = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
}