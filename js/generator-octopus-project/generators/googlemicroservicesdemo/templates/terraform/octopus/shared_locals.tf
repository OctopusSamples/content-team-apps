locals {
  versioning_strategy = "#{Octopus.Version.LastMajor}.#{Octopus.Version.LastMinor}.#{Octopus.Version.LastPatch}.#{Octopus.Version.NextRevision}"
  worker_image        = "octopusdeploy/worker-tools:3-ubuntu.18.04"
  namespace           = "matthew-casperson-onlineboutique"
  deployment_step     = "Deploy App"
  deployment_role     = "demo-k8s-cluster"
}