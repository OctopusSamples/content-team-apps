import {FC, ReactElement, useState} from "react";
import {Button, Grid, Link} from "@mui/material";
import {buttonStyle, journeyContainer, progressStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import LinearProgress from "@mui/material/LinearProgress";

const DoYouHaveCloudOctopus: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

    const [buttonDisabled, setButtonDisabled] = useState<boolean>(false);

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
                        <LinearProgress variant="determinate" value={20} sx={progressStyle}/>
                        <Link onClick={() => props.machine.send("BACK")}>&lt; Back</Link>
                        <h2>Do you have an existing cloud Octopus instance?</h2>
                        <p>
                            The app builder configures cloud Octopus instances for you. You can identify a cloud Octopus
                            instance by the URL you use to access it, which will be something like
                            myinstance.octopus.app.
                        </p>
                        <p>
                            The app builder can not configure self hosted Octopus instances.
                        </p>
                        <p>
                            If you do not have an existing cloud Octopus instance, select the No option, and you will
                            be able to create a free trial in the next screen.
                        </p>
                        <Button sx={buttonStyle} variant="outlined" disabled={buttonDisabled} onClick={() => {
                            setButtonDisabled(true);
                            props.machine.send("YES");
                        }}>
                            {"Yes"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined" disabled={buttonDisabled} onClick={() => {
                            setButtonDisabled(true);
                            props.machine.send("NO");
                        }}>
                            {"No"}
                        </Button>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>
        </>
    );
};

export default DoYouHaveCloudOctopus;