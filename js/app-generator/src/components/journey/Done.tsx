import {FC, ReactElement} from "react";
import {Button, Grid, Link} from "@mui/material";
import {buttonStyle, journeyContainer, nextButtonStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import {saveStateMachineStateContext} from "../../utils/statemachineutils";

const Done: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

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
                            The sample application has been pushed to GitHub. The CI workflow can be viewed here.
                        </p>
                        <p>
                            It will take a few minutes for the initial deployment to complete. In addition to building
                            the application source code, the GitHub Actions workflow also configures the following
                            resources in your Octopus instance:
                        </p>
                        <ul>
                            <li>Environments</li>
                            <li>Feeds</li>
                            <li>Targets</li>
                            <li>Deployment projects</li>
                            <li>Supporting runbooks</li>
                        </ul>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>
        </>
    );
};

export default Done;