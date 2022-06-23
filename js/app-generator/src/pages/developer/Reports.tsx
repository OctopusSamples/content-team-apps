import {FC, ReactElement, useContext, useEffect, useState} from "react";
import {getJsonApi, isBranchingEnabled} from "../../utils/network";
import {AppContext} from "../../App";
import {AuditsCollection} from "./Audits";
import {Chart, ChartConfiguration, registerables} from "chart.js";
import {chartColors} from "../../utils/charts";

Chart.register(...registerables);

const Reports: FC = (): ReactElement => {
    const context = useContext(AppContext);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fourWeeksAgo = new Date(new Date().getTime() - (28 * 24 * 60 * 60 * 1000));

        const buildProgressionReport = (audits: AuditsCollection) => {
            const data = {
                labels: [
                    'selectTarget',
                    'selectedTargetNotAvailable',
                    'doYouHaveCloudOctopus',
                    'signUpForCloudOctopus',
                    'logIntoOctopus',
                    'enterOctopusCredentials',
                    'loggedIntoOctopus',
                    'logIntoGitHub',
                    'loggedIntoGithub',
                    'enterAwsCredentials',
                    'pushPackage',
                    'done',
                    'error'
                ],
                datasets: [
                    {
                        data: [
                            // Ignore gradle as it is the default template returned, which skews the results
                            audits?.data?.filter(a => a.attributes.object === "selectTarget").length,
                            audits?.data?.filter(a => a.attributes.object === "selectedTargetNotAvailable").length,
                            audits?.data?.filter(a => a.attributes.object === "doYouHaveCloudOctopus").length,
                            audits?.data?.filter(a => a.attributes.object === "signUpForCloudOctopus").length,
                            audits?.data?.filter(a => a.attributes.object === "logIntoOctopus").length,
                            audits?.data?.filter(a => a.attributes.object === "enterOctopusCredentials").length,
                            audits?.data?.filter(a => a.attributes.object === "loggedIntoOctopus").length,
                            audits?.data?.filter(a => a.attributes.object === "logIntoGitHub").length,
                            audits?.data?.filter(a => a.attributes.object === "loggedIntoGithub").length,
                            audits?.data?.filter(a => a.attributes.object === "enterAwsCredentials").length,
                            audits?.data?.filter(a => a.attributes.object === "pushPackage").length,
                            audits?.data?.filter(a => a.attributes.object === "done").length,
                            audits?.data?.filter(a => a.attributes.object === "error").length
                        ],
                        backgroundColor: chartColors
                    }
                ]
            };

            const config: ChartConfiguration = {
                type: 'bar',
                data: data,
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'top',
                        },
                        title: {
                            display: true,
                            text: 'Progression Report (28 days)'
                        }
                    }
                },
            };

            const languageReport = document.getElementById('progressionReport') as HTMLCanvasElement;
            if (languageReport) {
                new Chart(languageReport, config);
            }
        }

        getJsonApi<AuditsCollection>(
            context.settings.auditEndpoint + "?page[limit]=10000&page[offset]=0&filter=action==GitHubRepoFrontend%3Btime>=" + fourWeeksAgo.toISOString(),
            context.settings,
            "main")
            .then(data => {
                buildProgressionReport(data);
            })
            .catch(err => {
                setError("Failed to retrieve audit resources. Make sure you are logged in. "
                    + (isBranchingEnabled() ? "Branching rules are enabled - double check they are valid, or disable them." : ""));
                console.log(err);
            })
    }, [context.settings.auditEndpoint, context.settings]);

    return <div>
        {error && <span>{error}</span>}
        <canvas id="progressionReport" style={{width: "1024px"}}></canvas>
    </div>
}

export default Reports;