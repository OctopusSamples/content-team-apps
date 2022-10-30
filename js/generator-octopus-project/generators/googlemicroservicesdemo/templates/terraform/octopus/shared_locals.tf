locals {
  versioning_strategy = "#{Octopus.Version.LastMajor}.#{Octopus.Version.LastMinor}.#{Octopus.Version.LastPatch}.#{Octopus.Version.NextRevision}"
  worker_image = "octopusdeploy/worker-tools:3-ubuntu.18.04"
}