import {FC, ReactElement, useState} from "react";
import {Box, Button, Grid} from "@mui/material";
import {buttonStyle, journeyContainer, progressStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import LinearProgress from '@mui/material/LinearProgress';
import Typography from '@mui/material/Typography';

function LinearProgressWithLabel(props: {value: number}) {
    return (
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <Box sx={{ width: '100%', mr: 1 }}>
                <LinearProgress variant="determinate" {...props} />
            </Box>
            <Box sx={{ minWidth: 35 }}>
                <Typography variant="body2" color="text.secondary">{`${Math.round(
                    props.value,
                )}%`}</Typography>
            </Box>
        </Box>
    );
}

const TargetSelection: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

    const [buttonDisabled, setButtonDisabled] = useState<boolean>(false);

    const next = (state: string, platform: string) => {
        setButtonDisabled(true);
        if (props.machine.state) {
            props.machine.state.context.targetPlatform = platform;

            if (props.machine.state.context.targetPlatform === "EKS") {
                props.machine.state.context.generator = "@octopus-content-team/generator-github-complete-eks-deployment"
            }

            if (props.machine.state.context.targetPlatform === "ECS") {
                props.machine.state.context.generator = "@octopus-content-team/generator-github-complete-ecs-deployment"
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
                        <Button sx={buttonStyle} variant="outlined" disabled={buttonDisabled} onClick={() => next("EKS", "EKS")}>
                            {"AWS Elastic Kubernetes Engine (EKS)"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined" disabled={buttonDisabled} onClick={() => next("ECS", "ECS")}>
                            {"AWS Elastic Compute Service (ECS)"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined" disabled={true} onClick={() => next("LAMBDA", "LAM")}>
                            {"AWS Lambda (Coming soon)"}
                        </Button>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>

        </>
    );
};

export default TargetSelection;