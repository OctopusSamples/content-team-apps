import {FC, ReactElement} from "react";
import {Button, Grid, Link} from "@mui/material";
import {buttonStyle, journeyContainer} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";

const LogIntoOctopus: FC<JourneyProps> = (props): ReactElement => {
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
                        <Link>&lt; Back</Link>
                        <h2>Do you have an existing cloud Octopus instance?</h2>
                        <p>
                            The app builder configures cloud Octopus instances for you. You can identify a cloud Octopus
                            instance by the URL you use to access it, which will be something like instancename.octopus.app.
                        </p>
                        <p>
                            The app builder can not configure self hosted Octopus instances.
                        </p>
                        <p>
                            If you do not have an existing cloud Octopus instance, select the No option, and you will
                            be able to create a free trial in the next screen.
                        </p>
                        <Button sx={buttonStyle} variant="outlined">
                            {"Yes"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined">
                            {"No"}
                        </Button>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>
        </>
    );
};

export default LogIntoOctopus;