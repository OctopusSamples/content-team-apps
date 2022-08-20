import {FC, ReactElement, useContext, useEffect, useState} from "react";
import {getJsonApi, isBranchingEnabled} from "../../utils/network";
import {AppContext} from "../../App";
import {AuditsCollection} from "./Audits";
import {Chart, ChartConfiguration, registerables} from "chart.js";
import {chartColors} from "../../utils/charts";
import {KeyboardDatePicker, MuiPickersUtilsProvider} from "@material-ui/pickers";
import {Grid} from "@material-ui/core";
import MomentUtils from '@date-io/moment';

Chart.register(...registerables);

const Reports: FC = (): ReactElement => {
    const context = useContext(AppContext);
    const [error, setError] = useState<string | null>(null);
    const [startDate, setStartDate] = useState<Date | null>(new Date(new Date().getTime() - (28 * 24 * 60 * 60 * 1000)));
    const [endDate, setEndDate] = useState<Date | null>(new Date());

    /*
        Delete and recreate the canvas before recreating the chart.
        See https://stackoverflow.com/a/25064035
    */
    const deleteAndRecreateCanvas = (parentId: string, childId: string) => {
        document.getElementById(childId)?.remove();
        const parentElement = document.getElementById(parentId);
        if (parentElement) {
            parentElement.innerHTML = '<canvas id="' + childId + '"></canvas>';
        }
    }

    useEffect(() => {
        if (!(startDate && endDate)) {
            return;
        }

        const buildProgressionReport = (audits: AuditsCollection) => {
            const data = {
                labels: [
                    'selectTarget',
                    'eksTargetSelected',
                    'ecsTargetSelected',
                    'lamTargetSelected',
                    'selectedTargetNotAvailable',
                    'doYouHaveCloudOctopus',
                    'signUpForCloudOctopus',
                    'externalOctopusSignup',
                    'logIntoOctopus',
                    'enterOctopusCredentials',
                    'loggedIntoOctopus',
                    'logIntoGitHub',
                    'loggedIntoGithub',
                    'enterAwsCredentials',
                    'pushPackage',
                    'done',
                    'externalGitHubRepo',
                    'externalOctopusSpace',
                    'externalGitHubWorkflow',
                    'error'
                ],
                datasets: [
                    {
                        data: [
                            // Ignore gradle as it is the default template returned, which skews the results
                            audits?.data?.filter(a => a.attributes.object === "selectTarget").length,
                            audits?.data?.filter(a => a.attributes.object === "eksTargetSelected").length,
                            audits?.data?.filter(a => a.attributes.object === "ecsTargetSelected").length,
                            audits?.data?.filter(a => a.attributes.object === "lamTargetSelected").length,
                            audits?.data?.filter(a => a.attributes.object === "selectedTargetNotAvailable").length,
                            audits?.data?.filter(a => a.attributes.object === "doYouHaveCloudOctopus").length,
                            audits?.data?.filter(a => a.attributes.object === "signUpForCloudOctopus").length,
                            audits?.data?.filter(a => a.attributes.object === "externalOctopusSignup").length,
                            audits?.data?.filter(a => a.attributes.object === "logIntoOctopus").length,
                            audits?.data?.filter(a => a.attributes.object === "enterOctopusCredentials").length,
                            audits?.data?.filter(a => a.attributes.object === "loggedIntoOctopus").length,
                            audits?.data?.filter(a => a.attributes.object === "logIntoGitHub").length,
                            audits?.data?.filter(a => a.attributes.object === "loggedIntoGithub").length,
                            audits?.data?.filter(a => a.attributes.object === "enterAwsCredentials").length,
                            audits?.data?.filter(a => a.attributes.object === "pushPackage").length,
                            audits?.data?.filter(a => a.attributes.object === "done").length,
                            audits?.data?.filter(a => a.attributes.object === "externalGitHubRepo").length,
                            audits?.data?.filter(a => a.attributes.object === "externalOctopusSpace").length,
                            audits?.data?.filter(a => a.attributes.object === "externalGitHubWorkflow").length,
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
                            display: false,
                            position: 'top',
                        },
                        title: {
                            display: true,
                            text: 'Progression Report'
                        }
                    }
                },
            };

            deleteAndRecreateCanvas('progressionReportParent', 'progressionReport');
            const progressionReport = document.getElementById('progressionReport') as HTMLCanvasElement;
            if (progressionReport) {
                new Chart(progressionReport, config);
            }
        }

        getJsonApi<AuditsCollection>(
            context.settings.auditEndpoint + "?page[limit]=10000&page[offset]=0&filter=action==VisitedPage"
            + "%3Btime>=" + startDate.toISOString()
            + "%3Btime<=" + endDate.toISOString(),
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
    }, [context.settings.auditEndpoint, context.settings, startDate, endDate]);

    return <div>
        {error && <span>{error}</span>}
        <h2>Date Range</h2>
        <MuiPickersUtilsProvider utils={MomentUtils}>
            <Grid container justifyContent="flex-start">
                <KeyboardDatePicker
                    disableToolbar
                    variant="inline"
                    format="DD/MM/yyyy"
                    margin="normal"
                    id="start-date"
                    label="Start Date"
                    KeyboardButtonProps={{
                        'aria-label': 'change date',
                    }}
                    onChange={date => setStartDate(date?.toDate() || null)}
                    value={startDate}
                />
                <KeyboardDatePicker
                    disableToolbar
                    variant="inline"
                    format="DD/MM/yyyy"
                    margin="normal"
                    id="end-date"
                    label="End Date"
                    KeyboardButtonProps={{
                        'aria-label': 'change date',
                    }}
                    onChange={date => setEndDate(date?.toDate() || null)}
                    value={endDate}
                />
            </Grid>
        </MuiPickersUtilsProvider>
        <div id="progressionReportParent" style={{width: "1024px"}}>
            <canvas id="progressionReport"></canvas>
        </div>
    </div>
}

export default Reports;