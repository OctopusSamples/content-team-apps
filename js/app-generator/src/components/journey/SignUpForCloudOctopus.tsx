import {FC, ReactElement} from "react";
import {Button, Grid} from "@mui/material";
import {buttonStyle, journeyContainer} from "../../utils/styles";
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
                        <h2>Complete the cloud Octopus trail sign up.</h2>
                        <p>
                            Clicking the signup button below redirects you to the Octopus cloud trial signup page.
                        </p>
                        <Button sx={buttonStyle} variant="outlined" onClick={() => window.open("https://octopus.com/start/cloud")}>
                            {"Signup"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined" onClick={() => props.machine.send("NEXT")}>
                            {"Next"}
                        </Button>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>
        </>
    );
};

export default SignUpForCloudOctopus;