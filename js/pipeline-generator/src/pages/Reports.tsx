import {FC, ReactElement, useContext, useEffect, useState} from "react";
import {getJsonApi, isBranchingEnabled} from "../utils/network";
import {AppContext} from "../App";
import {AuditsCollection} from "./Audits";
import {JSEncrypt} from "jsencrypt";
import {Chart, ChartConfiguration, registerables} from "chart.js";
import {chartColors} from "../utils/charts";
import {KeyboardDatePicker, MuiPickersUtilsProvider} from "@material-ui/pickers";
import {Grid} from "@material-ui/core";
import MomentUtils from '@date-io/moment';

Chart.register(...registerables);

const Reports: FC<{}> = (): ReactElement => {
    const context = useContext(AppContext);
    const [emailAuditsFourWeeks, setEmailAuditsFourWeeks] = useState<AuditsCollection | null>(null);
    const [templateAuditsFourWeeks, setTemplateAuditsFourWeeks] = useState<AuditsCollection | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [privateKey, setPrivateKey] = useState<string | null>(null);
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

        const decrypt = (email: string): string => {
            if (!privateKey) {
                return email;
            }

            try {
                const decrypt = new JSEncrypt();
                decrypt.setPrivateKey(privateKey);
                return decrypt.decrypt(email).toString();
            } catch {
                // If the wrong key is used, fail silently
                return email;
            }
        }

        const processAudits = (audits: AuditsCollection): AuditsCollection => {
            return {
                ...audits, data: audits.data
                    ?.map(a => {
                        a.attributes.object = decrypt(a.attributes.object);
                        return a;
                    })
                    ?.filter(a => !a.attributes.object.endsWith("users.noreply.github.com"))
            }
        }

        getJsonApi<AuditsCollection>(context.settings.auditEndpoint + "?page[limit]=10000&page[offset]=0&filter=action==CreateTemplateFor"
            + "%3Btime>=" + startDate.toISOString()
            + "%3Btime<=" + endDate.toISOString(),
            "main")
            .then(data => {
                setEmailAuditsFourWeeks(processAudits(data));
            })
            .catch(err => {
                setError("Failed to retrieve audit resources. Make sure you are logged in. "
                    + (isBranchingEnabled() ? "Branching rules are enabled - double check they are valid, or disable them." : ""));
                console.log(err);
            })
    }, [setEmailAuditsFourWeeks, startDate, endDate, context.settings.auditEndpoint, privateKey]);

    useEffect(() => {
        if (!(startDate && endDate)) {
            return;
        }

        const buildLanguageReport = (audits: AuditsCollection) => {
            const data = {
                labels: ['Java', '.NET Core', 'Node.js', 'Go', 'Python', 'PHP', 'Ruby'],
                datasets: [
                    {
                        data: [
                            // Ignore gradle as it is the default template returned, which skews the results
                            audits?.data?.filter(a => a.attributes.object === "Java Maven").length,
                            audits?.data?.filter(a => a.attributes.object === "DotNET Core").length,
                            audits?.data?.filter(a => a.attributes.object === "Node.js").length,
                            audits?.data?.filter(a => a.attributes.object === "Go").length,
                            audits?.data?.filter(a => a.attributes.object === "Python").length,
                            audits?.data?.filter(a => a.attributes.object === "PHP").length,
                            audits?.data?.filter(a => a.attributes.object === "Ruby").length
                        ],
                        backgroundColor: chartColors
                    }
                ]
            };

            const config: ChartConfiguration = {
                type: 'pie',
                data: data,
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'top',
                        },
                        title: {
                            display: true,
                            text: 'Language Report'
                        }
                    }
                },
            };

            deleteAndRecreateCanvas('languageReportParent', 'languageReport');
            const canvas = document.getElementById('languageReport') as HTMLCanvasElement;
            if (canvas) {
                new Chart(canvas, config);
            }
        }
        const buildPlatformReport = (audits: AuditsCollection) => {
            const data = {
                labels: ['Jenkins', 'GitHub Actions'],
                datasets: [
                    {
                        data: [
                            audits?.data?.filter(a => a.attributes.subject === "JenkinsPipelineBuilder").length,
                            audits?.data?.filter(a => a.attributes.subject === "GithubActionWorkflowBuilder").length
                        ],
                        backgroundColor: chartColors
                    }
                ]
            };

            const config: ChartConfiguration = {
                type: 'pie',
                data: data,
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'top',
                        },
                        title: {
                            display: true,
                            text: 'Platform Report'
                        }
                    }
                },
            };

            deleteAndRecreateCanvas('platformReportParent', 'platformReport');
            const canvas = document.getElementById('platformReport') as HTMLCanvasElement;
            if (canvas) {
                new Chart(canvas, config);
            }
        }

        getJsonApi<AuditsCollection>(context.settings.auditEndpoint
            + "?page[limit]=10000&page[offset]=0&filter=action==CreateTemplateUsing"
            + "%3Btime>=" + startDate.toISOString()
            + "%3Btime<=" + endDate.toISOString(),
            "main")
            .then(data => {
                setTemplateAuditsFourWeeks(data);
                buildLanguageReport(data);
                buildPlatformReport(data);
            })
            .catch(err => {
                setError("Failed to retrieve audit resources. Make sure you are logged in. "
                    + (isBranchingEnabled() ? "Branching rules are enabled - double check they are valid, or disable them." : ""));
                console.log(err);
            })
    }, [setEmailAuditsFourWeeks, startDate, endDate, context.settings.auditEndpoint, privateKey]);

    return <div>
        {error && <span>{error}</span>}
        <h2>Decryption</h2>
        <p>Note some of these email addresses (usually around 50%) are unusable "no-reply" addresses.</p>
        <p>Upload the private key using the button below to allow the report to filter no-reply email addresses.</p>
        <form encType="multipart/form-data">
            <input id="upload" type="file" accept=".pem" name="files[]" size={30} onChange={(evt) => {
                const files = evt.target.files || [];
                const f = files[0];
                const reader = new FileReader();

                // Closure to capture the file information.
                reader.onload = function (e) {
                    setPrivateKey(e.target?.result?.toString() || null);
                };

                reader.readAsText(f);
            }}/>
        </form>
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
        <h2>Report</h2>
        <table>
            <tr>
                <td>
                    <p>Emails collected: {emailAuditsFourWeeks?.data?.length}
                        {!privateKey && <span> (estimated {Math.round(emailAuditsFourWeeks?.data?.length ? emailAuditsFourWeeks.data.length / 2 : 0)} public emails)</span>}</p>
                    <p>Jenkins
                        templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.subject === "JenkinsPipelineBuilder").length}</p>
                    <p>GitHub Actions
                        templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.subject === "GithubActionWorkflowBuilder").length}</p>
                    <p>Node.js
                        templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Node.js").length}</p>
                    <p>DotNET Core
                        templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "DotNET Core").length}</p>
                    <p>Generic
                        templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Generic").length}</p>
                    <p>Go
                        templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Go").length}</p>
                    <p>Java Gradle
                        templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Java Gradle").length}</p>
                    <p>Java Maven
                        templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Java Maven").length}</p>
                    <p>PHP
                        templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "PHP").length}</p>
                    <p>Python
                        templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Python").length}</p>
                    <p>Ruby
                        templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Ruby").length}</p>
                </td>
                <td style={{paddingLeft: "32px"}} id="languageReportParent">

                </td>
                <td style={{paddingLeft: "32px"}} id="platformReportParent">

                </td>
            </tr>
        </table>
    </div>
}

export default Reports;