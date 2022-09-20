The `githubsecretinitialize` project is used to bootstrap a GitHub repo with the secrets other terraform templates
will need to populate an Octopus space. This standardised set of variables is reused with each new specialised
Terraform templates.

See [this documentation](https://github.com/OctopusSamples/content-team-apps/tree/main/js/generator-octopus-project)
for explanations of the secrets requested by this project.

This project is expected to be run locally, either by answering all the questions, or renaming `secrets.tfvars.template`
to `secrets.tfvars`, adding the values, and running `terraform apply -var-file="secrets.tfvars"`.

This project is configured to use local state, which is excluded from the git repo.