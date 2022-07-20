import {FC, ReactElement, useContext, useState} from "react";
import {Button, Grid} from "@mui/material";
import {buttonStyle, journeyContainer, nextButtonStyle, progressStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import LinearProgress from '@mui/material/LinearProgress';
import {AppContext} from "../../App";
import {loginRequired} from "../../utils/security";

const Welcome: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();
    const context = useContext(AppContext);
    const loginIsRequired = loginRequired(context.settings, context.partition);

    const [buttonDisabled, setButtonDisabled] = useState<boolean>(loginIsRequired);

    const next = () => {
        setButtonDisabled(true);
        props.machine.send("NEXT");
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
                        <h2>Welcome</h2>
                        <p>
                            Welcome to the Octopus Workflow Builder. This tool provides you with a functional
                            CI/CD pipeline to cloud platforms like EKS, ECS, and Lambdas using Octopus and GitHub.
                            Completing this wizard will:
                        </p>
                        <ul>
                            <li>Create a GitHub repository with a sample microservice application.</li>
                            <li>Create GitHub Actions Workflows to build the sample application and push the artifacts to an Octopus instance.</li>
                            <li>
                                Populate an Octopus instance with:
                                <ul>
                                    <li>Environments</li>
                                    <li>Lifecycles</li>
                                    <li>Accounts</li>
                                    <li>Feeds</li>
                                    <li>Deployment Projects</li>
                                </ul>
                            </li>
                        </ul>
                        <p>
                            You then simply click the "Deploy" button in Octopus to complete the process!
                        </p>
                        <Button sx={nextButtonStyle} variant="outlined" disabled={buttonDisabled} onClick={() => {
                            setButtonDisabled(true);
                            props.machine.send("NEXT");
                        }}>
                            {"Next >"}
                        </Button>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>

        </>
    );
};

export default Welcome;