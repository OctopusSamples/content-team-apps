import {FC, ReactElement, useContext, useEffect, useState} from "react";
import {Button, CircularProgress, Grid} from "@mui/material";
import {journeyContainer, openResourceStyle, progressStyle, styles} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import LinearProgress from "@mui/material/LinearProgress";
import {getJsonApi} from "../../utils/network";
import {AppContext} from "../../App";
import CheckCircleOutlineOutlinedIcon from '@mui/icons-material/CheckCircleOutlineOutlined';
import CancelIcon from '@mui/icons-material/Cancel';
import {generateSpaceName, getOctopusServer} from "../../utils/naming";
import Cookies from "js-cookie";

const Done: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();
    const moreClasses = styles();

    const context = useContext(AppContext);
    const [repoCreated, setRepoCreated] = useState<boolean>(false);
    const [workflowUrl, setWorkflowUrl] = useState<string | null>(null);
    const [workflowCompleted, setWorkflowCompleted] = useState<boolean>(false);
    const [spaceId, setSpaceId] = useState<string | null>(null);

    const repoUrlValid = () => {
        return !!props.machine.state.context.apiRepoUrl;
    }

    const checkRepoExists = () => {
        // If for some reason the api url was not returned, don't attempt to query it
        if (!repoUrlValid()) {
            return;
        }

        // No need to check once the repo is found
        if (repoCreated) {
            return true;
        }

        getJsonApi(context.settings.githubRepoEndpoint + "/" + encodeURIComponent(props.machine.state.context.apiRepoUrl), context.settings, null)
            .then(body => {
                const bodyObject = body as any;
                if (bodyObject.data.id) {
                    setRepoCreated(true);
                }
            })
            .catch(() => {
                setRepoCreated(false);
            });
    }

    const checkWorkflowComplete = () => {
        // If for some reason the api url was not returned, don't attempt to query it
        if (!repoUrlValid()) {
            return;
        }

        // No need to check once the repo is found
        if (workflowCompleted) {
            return true;
        }

        getJsonApi(context.settings.githubRepoEndpoint + "/" + encodeURIComponent(props.machine.state.context.apiRepoUrl), context.settings, null)
            .then(body => {
                const bodyObject = body as any;
                if (bodyObject?.data?.id) {

                    const latestWorkflow = bodyObject?.included
                        ?.filter((i: any) => i.type === "workflowruns")
                        ?.sort((a: any, b: any) => a.attributes?.runNumber > b.attributes?.runNumber)
                        ?.pop();

                    if (latestWorkflow) {
                        setWorkflowUrl(latestWorkflow.attributes?.htmlUrl);
                        setWorkflowCompleted(latestWorkflow?.attributes?.status === "completed");
                    }
                }
            })
            .catch(() => {
                setWorkflowUrl(null);
                setWorkflowCompleted(false);
            });
    }

    const checkSpaceExists = () => {
        // No need to check once the space is found
        if (spaceId) {
            return true;
        }

        const spaceName = generateSpaceName(
            props.machine.state.context.targetPlatform,
            props.machine.state.context.developmentFramework,
            props.machine.state.context.owner);

        const manuallyEnteredApiKey = Cookies.get("octopusApiKey") || "";

        const url = context.settings.octoSpaceEndpoint
            + "?filter="
            + "name==\"" + encodeURIComponent(spaceName) + "\""
            + encodeURIComponent(";")
            + "instance==" + encodeURIComponent(getOctopusServer(props.machine.state.context))
            + "&apiKey=" + encodeURIComponent(manuallyEnteredApiKey);

        getJsonApi(url, context.settings, null)
            .then(body => {
                const bodyObject = body as any;
                if (bodyObject.data.length !== 0) {
                    setSpaceId(bodyObject.data[0].attributes.Id);
                }
            })
            .catch(() => {
                setSpaceId(null);
            });
    }

    useEffect(() => {
        const timer = setInterval(() => {
            if (!context.settings.disableExternalCalls) {
                checkRepoExists();
                checkSpaceExists();
                checkWorkflowComplete();
            } else {
                // show a mock change after 1 second
                setRepoCreated(true);
                setSpaceId(getOctopusServer(props.machine.state.context));
                setWorkflowCompleted(true);
                setWorkflowUrl(props.machine.state.context.browsableRepoUrl + "/actions")
            }
        }, 10000);
        return () => clearInterval(timer);
    });

    // Make sure people don't exit away unexpectedly
    window.addEventListener("beforeunload", (ev) => {
        ev.preventDefault();
        return ev.returnValue = 'Are you sure you want to close? This page has important information regarding the new resources being created by the App Builder.';
    });

    return (
        <>
            <Grid
                container={true}
                className={classes.root}
                spacing={2}
            >
                <Grid item md={3} xs={0}/>
                <Grid item md={6} xs={12}>
                    <Grid
                        container={true}
                        className={classes.column}
                    >
                        <LinearProgress variant="determinate" value={100} sx={progressStyle}/>
                        <h2>You're all done.</h2>
                        <p>
                            In the background a GitHub repository is being populated with a sample application and
                            Terraform templates.
                        </p>
                        <p>
                            The application code and Terraform templates are processed by
                            a GitHub Actions workflow. The code is compiled into deployable artifacts (ZIP files or
                            Docker images depending on the platform), while the Terraform templates are used to create
                            and populate a new Octopus space. This is the CI half of the CI/CD pipeline.
                        </p>
                        <p>
                            Once the Octopus space is populated, the projects it contains are used to deploy the sample
                            application to the cloud. This is the CD half of the CI/CD pipeline.
                        </p>
                        <p>
                            The progress of the various resources that are created by the App Builder is shown below:
                        </p>
                        <table>
                            <tr>
                                <td>{repoUrlValid() &&
                                    <span>
                                        {repoCreated && <CheckCircleOutlineOutlinedIcon className={moreClasses.icon}/>}
                                        {!repoCreated && <CircularProgress size={32}/>}
                                    </span>}
                                    {!repoUrlValid() && <CancelIcon className={moreClasses.icon}/>}
                                </td>
                                <td>{repoUrlValid() && <span><h3>
                                        {repoCreated && <span>Created </span>}
                                    {!repoCreated && <span>Creating </span>}
                                    the GitHub repo.
                                    </h3>
                                    <p>
                                        This repo holds the Terraform templates used to create and populate the Octopus space, as well as the sample
                                        application source code.
                                    </p></span>}
                                    {!repoUrlValid() && <span>There was an error querying the GitHub repo. Please report
                                        this issue <a href={"https://github.com/OctopusSamples/content-team-apps/issues"}>here</a>.</span>}
                                </td>
                                <td>{repoUrlValid() && repoCreated &&
                                    <Button sx={openResourceStyle} variant="outlined"
                                            onClick={() => window.open(props.machine.state.context.browsableRepoUrl, "_blank")}>
                                        {"Open GitHub >"}
                                    </Button>}
                                </td>
                            </tr>
                            <tr>
                                <td>{!!spaceId && <CheckCircleOutlineOutlinedIcon className={moreClasses.icon}/>}
                                    {!spaceId && <CircularProgress size={32}/>}</td>
                                <td><h3>{!!spaceId && <span>Created</span>}{!spaceId && <span>Creating</span>} the
                                    Octopus space.</h3>
                                    <p>This is the space that will host the deployment project and other resources required to deploy the sample
                                        application.</p>
                                    <p>It can take a minute or so for the GitHub Actions workflow to populate this space after it has been created.</p>
                                </td>
                                <td>{!!spaceId &&
                                    <Button sx={openResourceStyle} variant="outlined"
                                            onClick={() => window.open(getOctopusServer(props.machine.state.context) + "/app#/" + spaceId, "_blank")}>
                                        {"Open Space >"}
                                    </Button>}
                                </td>
                            </tr>
                            <tr>
                                <td>{workflowCompleted &&
                                    <CheckCircleOutlineOutlinedIcon className={moreClasses.icon}/>}
                                    {!workflowCompleted && <CircularProgress size={32}/>}</td>
                                <td>
                                    <h3>{workflowCompleted && <span>Completed</span>}
                                        {!workflowCompleted && <span>Running</span>} the GitHub Actions workflow.</h3>
                                    <p>
                                        When this workflow is finished, the Octopus space will be populated, the sample application will be built and published,
                                        and all other supporting resources (like Docker repositories) will be created.
                                    </p>
                                </td>
                                <td>{!!workflowUrl && <Button sx={openResourceStyle} variant="outlined"
                                                              onClick={() => window.open(workflowUrl, "_blank")}>
                                    {"Open Workflows >"}
                                </Button>}
                                </td>
                            </tr>
                        </table>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>
        </>
    );
};

export default Done;