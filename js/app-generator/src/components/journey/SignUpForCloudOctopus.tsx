import {FC, ReactElement, useContext, useState} from "react";
import {Button, Grid, Link} from "@mui/material";
import {buttonStyle, journeyContainer, nextButtonStyle, progressStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import LinearProgress from "@mui/material/LinearProgress";
import {auditPageVisit} from "../../utils/audit";
import {AppContext} from "../../App";

const SignUpForCloudOctopus: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();
    const context = useContext(AppContext);

    const [buttonDisabled, setButtonDisabled] = useState<boolean>(false);

    const openSignUp = () => {
        auditPageVisit("externalOctopusSignup", context.settings, context.partition);
        window.open("https://octopus.com/start/cloud", '_blank');
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
                        <LinearProgress variant="determinate" value={30} sx={progressStyle}/>
                        <Link onClick={() => props.machine.send("BACK")}>&lt; Back</Link>
                        <h2>Complete the cloud Octopus trail sign up.</h2>
                        <p>
                            Click the signup button to open a new tab where you can sign up for a free Octopus cloud
                            trial.
                        </p>
                        <p>
                            Once you have completed the signup for the cloud trial, return to this page, and click the
                            next button.
                        </p>
                        <Button sx={buttonStyle} variant="outlined" onClick={openSignUp}>
                            {"Signup"}
                        </Button>
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

export default SignUpForCloudOctopus;