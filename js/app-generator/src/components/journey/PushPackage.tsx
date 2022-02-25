import {FC, ReactElement} from "react";
import {Button, Grid, Link} from "@mui/material";
import {journeyContainer, nextButtonStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";

const PushPackage: FC<JourneyProps> = (props): ReactElement => {
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
                        <h2>Push the sample application and deployment configuration to GitHub.</h2>
                        <p>
                            Congratulations! You are now ready to create a new GitHub repository with the sample
                            application code and GitHub Actions workflow that configures Octopus, builds the code,
                            pushes the package to Octopus, and deploys the application.
                        </p>
                        <p>
                            Click the next button to configure your CI/CD pipeline.
                        </p>
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

export default PushPackage;