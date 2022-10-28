locals {
  vulnerability_scan = <<-EOT
      echo "##octopus[stdout-verbose]"
      docker pull appthreat/dep-scan
      echo "##octopus[stdout-default]"

      TIMESTAMP=$(date +%s%3N)
      SUCCESS=0
      for x in $(find . -name bom.xml -type f -print); do
          echo "Scanning $${x}"

          # Delete any existing report file
          if [[ -f "$PWD/depscan-bom.json" ]]; then
            rm "$PWD/depscan-bom.json"
          fi

          # Generate the report, capturing the output, and ensuring $? is set to the exit code
          OUTPUT=$(bash -c "docker run --rm -v \"$PWD:/app\" appthreat/dep-scan scan --bom \"/app/$${x}\" --type bom --report_file /app/depscan.json; exit \$?" 2>&1)

          # Success is set to 1 if the exit code is not zero
          if [[ $? -ne 0 ]]; then
              SUCCESS=1
          fi

          # Print the output stripped of ANSI colour codes
          echo -e "$${OUTPUT}" | sed 's/\x1b\[[0-9;]*m//g'
      done

      set_octopusvariable "VerificationResult" $SUCCESS

      if [[ $SUCCESS -ne 0 ]]; then
        >&2 echo "Critical vulnerabilities were detected"
      else
        echo "No critical vulnerabilities were detected"
      fi

      exit 0
    EOT
}