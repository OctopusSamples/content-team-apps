import {FC, ReactElement, useContext, useState} from "react";
import {Button, Grid} from "@mui/material";
import {buttonStyle, journeyContainer, progressStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import LinearProgress from '@mui/material/LinearProgress';
import {AppContext} from "../../App";
import {loginRequired} from "../../utils/security";

const TargetSelection: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();
    const context = useContext(AppContext);
    const loginIsRequired = loginRequired(context.settings, context.partition);

    const [buttonDisabled, setButtonDisabled] = useState<boolean>(loginIsRequired);

    const next = (state: string, platform: string) => {
        setButtonDisabled(true);
        if (props.machine.state) {
            props.machine.state.context.targetPlatform = platform;

            // Match the platform to the Yeoman generator
            if (props.machine.state.context.targetPlatform === "EKS") {
                props.machine.state.context.generator = "@octopus-content-team/generator-github-complete-eks-deployment"
            }

            if (props.machine.state.context.targetPlatform === "ECS") {
                props.machine.state.context.generator = "@octopus-content-team/generator-github-complete-ecs-deployment"
            }

            if (props.machine.state.context.targetPlatform === "LAM") {
                props.machine.state.context.generator = "@octopus-content-team/generator-github-complete-lambda-deployment"
            }
        }
        props.machine.send(state);
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
                        <LinearProgress variant="determinate" value={10} sx={progressStyle}/>
                        <h2>Where are you deploying the microservice?</h2>
                        <p>
                            Select the platform that you wish to deploy the sample microservice application to using
                            Octopus.
                        </p>
                        {loginIsRequired && <>
                            <p>YOU HAVE ENABLED THE "REQUIRE TESTING PARTITION" OPTION.</p>
                            <p>LOGIN FROM THE SETTINGS PAGE, AND DEFINE A PARTITION OTHER THAN MAIN.</p>
                        </>}
                        <Button sx={buttonStyle} variant="outlined" disabled={buttonDisabled}
                                onClick={() => next("EKS", "EKS")}>
                            {"AWS Elastic Kubernetes Engine (EKS)"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined" disabled={buttonDisabled}
                                onClick={() => next("ECS", "ECS")}>
                            {"AWS Elastic Compute Service (ECS)"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined" disabled={buttonDisabled}
                                onClick={() => next("LAMBDA", "LAM")}>
                            {"AWS Lambda"}
                        </Button>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>

        </>
    );
};

export default TargetSelection;