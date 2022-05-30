import {FC, ReactElement, useContext, useEffect, useState} from "react";
import {Button, CircularProgress, Grid} from "@mui/material";
import {iconStyle, journeyContainer, openResourceStyle, progressStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import LinearProgress from "@mui/material/LinearProgress";
import {getJsonApi} from "../../utils/network";
import {AppContext} from "../../App";
import CheckCircleOutlineOutlinedIcon from '@mui/icons-material/CheckCircleOutlineOutlined';
import CancelIcon from '@mui/icons-material/Cancel';
import ErrorIcon from '@mui/icons-material/Error';
import {generateSpaceName, getOctopusServer} from "../../utils/naming";
import Cookies from "js-cookie";

/**
 * Allow up to 5 errors accessing the space proxy. Anything more than that, and we just assume
 * we won't get a good result.
 */
const MAX_SPACE_ERRORS = 5;

const Done: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

    const context = useContext(AppContext);
    const [repoCreated, setRepoCreated] = useState<boolean>(false);
    const [workflowUrl, setWorkflowUrl] = useState<string | null>(null);
    const [workflowCompleted, setWorkflowCompleted] = useState<boolean>(false);
    const [spaceId, setSpaceId] = useState<string | null>(null);
    const [spaceErrorCount, setSpaceErrorCount] = useState<number>(0);

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
            return;
        }

        getJsonApi(context.settings.githubRepoEndpoint + "/" + encodeURIComponent(props.machine.state.context.apiRepoUrl), context.settings, null, 0)
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
            return;
        }

        getJsonApi(context.settings.githubRepoEndpoint + "/" + encodeURIComponent(props.machine.state.context.apiRepoUrl), context.settings, null, 0)
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
        // No need to check once the space is found, or if we encountered too many errors to continue.
        if (spaceId || spaceErrorCount >= MAX_SPACE_ERRORS) {
            return;
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

        getJsonApi(url, context.settings, null, 0)
            .then(body => {
                // reset the error count
                setSpaceErrorCount(0);

                // Try and find the space
                const bodyObject = body as any;
                if (bodyObject.data.length !== 0) {
                    setSpaceId(bodyObject.data[0].attributes.Id);
                }

            })
            .catch(e => {
                // count the consecutive errors
                setSpaceErrorCount(spaceErrorCount + 1);
                console.log(e);
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
        return ev.returnValue = 'Are you sure you want to close? This page has important information regarding the new resources being created by the Octopus Builder.';
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
                        <h2>Your CI/CD pipeline is now being configured.</h2>
                        <p>
                            The progress of the various resources created by the Octopus Workflow Builder are shown below:
                        </p>
                        <table>
                            <tr>
                                <td>{repoUrlValid() &&
                                    <span>
                                        {repoCreated && <CheckCircleOutlineOutlinedIcon sx={iconStyle}/>}
                                        {!repoCreated && <CircularProgress sx={iconStyle} size={32}/>}
                                    </span>}
                                    {!repoUrlValid() && <CancelIcon sx={iconStyle}/>}
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
                            {spaceErrorCount < MAX_SPACE_ERRORS &&
                                <tr>
                                    <td>{!!spaceId && <CheckCircleOutlineOutlinedIcon sx={iconStyle}/>}
                                        {!spaceId && <CircularProgress size={32} sx={iconStyle}/>}</td>
                                    <td>
                                        <h3>{!!spaceId && <span>Created</span>}{!spaceId && <span>Creating</span>} the
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
                            }
                            {spaceErrorCount >= MAX_SPACE_ERRORS &&
                                <tr>
                                    <td><ErrorIcon sx={iconStyle}/></td>
                                    <td>
                                        <h3>Failed to detect Octopus space.</h3>
                                        <p>
                                            Something went wrong and we were unable to determine the state of the newly created Octopus space. The space
                                            may still be created, so look for a space with your GitHub username in it once the GitHub Actions workflow
                                            completes.
                                        </p>
                                    </td>
                                    <td>
                                    </td>
                                </tr>
                            }
                            <tr>
                                <td>{workflowCompleted &&
                                    <CheckCircleOutlineOutlinedIcon sx={iconStyle}/>}
                                    {!workflowCompleted && <CircularProgress size={32} sx={iconStyle}/>}</td>
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
                        <h2>Next steps.</h2>
                        <p>
                            When the GitHub Actions workflow is completed, your Octopus instance is fully populated and ready to perform your first deployment.
                        </p>
                        <p>
                            Watch the video below to learn how to make the most of your Octopus instance.
                        </p>
                        <iframe width="100%" height="522" src="https://oc.to/QoaFL7" title="YouTube video player" frameBorder="0" style={{marginTop: "16px"}}
                                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowFullScreen></iframe>
                        <iframe width="100%" height="522" src="https://oc.to/LdAw6T" title="YouTube video player" frameBorder="0" style={{marginTop: "16px"}}
                                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowFullScreen></iframe>
                        <iframe width="100%" height="522" src="https://oc.to/tOL83r" title="YouTube video player" frameBorder="0" style={{marginTop: "16px"}}
                                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowFullScreen></iframe>
                        <iframe width="100%" height="522" src="https://oc.to/JgsP6z" title="YouTube video player" frameBorder="0" style={{marginTop: "16px"}}
                                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowFullScreen></iframe>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>
        </>
    );
};

export default Done;