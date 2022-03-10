import {FC, ReactElement} from "react";
import {Button, Grid, Link} from "@mui/material";
import {buttonStyle, journeyContainer, nextButtonStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";

const SignUpForCloudOctopus: FC<JourneyProps> = (props): ReactElement => {
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
                        <Link onClick={() => props.machine.send("BACK")}>&lt; Back</Link>
                        <h2>Complete the cloud Octopus trail sign up.</h2>
                        <p>
                            Click the signup button to open a new tab where you can sign up for a free Octopus cloud trial.
                        </p>
                        <p>
                            Once you have completed the signup for the cloud trial, return to this page, and click the next button.
                        </p>
                        <Button sx={buttonStyle} variant="outlined" onClick={() => window.open("https://octopus.com/start/cloud")}>
                            {"Signup"}
                        </Button>
                        <Button sx={nextButtonStyle} variant="outlined" onClick={() => props.machine.send("NEXT")}>
                            {"Next >"}
                        </Button>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>
        </>
    );
};

export default SignUpForCloudOctopus;