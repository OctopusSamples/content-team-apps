import {FC, ReactElement} from "react";
import {Button, Grid} from "@mui/material";
import {journeyContainer, nextButtonStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";

const Done: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

    // Make sure people don't exit away unexpectedly
    window.addEventListener("beforeunload", (ev) =>
    {
        ev.preventDefault();
        return ev.returnValue = 'Are you sure you want to close? This page has important information regarding the new resources being created by the App Builder.';
    });

    function getOctopusServer() {
        if (props.machine.state.context.octopusServer) {
            try {
                const url = new URL(props.machine.state.context.octopusServer);
                return "https://" + url.hostname;
            } catch {
                return "https://" + props.machine.state.context.octopusServer.split("/")[0];
            }
        }
        // Let the service return an error in its response code, and handle the response as usual.
        return "";
    }

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
                        <h2>You're all done.</h2>
                        <p>
                            The sample application is being pushed to <a href={"https://github.com/?q=" + props.machine.state.context.githubRepo} target={"_blank"} rel={"noreferrer"}>GitHub</a> in a
                            repository called <strong>{props.machine.state.context.githubRepo}</strong>. This repository
                            will take a few seconds to be created.
                        </p>
                        <p>
                            In a minute or so the sample project files will be uploaded and the initial GitHub Actions
                            workflow will start automatically.
                        </p>
                        <p>
                            <img src={"repo.png"} alt={"GitHub Repo"}/>
                        </p>
                        <p>
                            Once the GitHub Actions workflow has completed, the new space named <strong>{props.machine.state.context.targetPlatform}</strong> and
                            ending with your GitHub account name in your <a href={getOctopusServer() + "/app#/configuration/spaces"} target={"_blank"} rel={"noreferrer"}>Octopus instance</a> will
                            be populated with a sample project and all other associated resources to complete a deployment.
                        </p>
                        <p>
                            <img src={"octopus-space.png"} alt={"Octopus Space"}/>
                        </p>
                        <p>
                            If you would like to share some feedback about the App Builder, feel free to leave a comment
                            on <a href={"https://github.com/OctopusSamples/content-team-apps/issues/13"} target={"_blank"} rel={"noreferrer"}>this GitHub issue</a>.
                        </p>
                        <Button
                            sx={nextButtonStyle}
                            variant="outlined"
                            onClick={() => window.open(getOctopusServer() + "/app#/configuration/spaces", "_blank")}>
                            {"Open Octopus >"}
                        </Button>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>
        </>
    );
};

export default Done;