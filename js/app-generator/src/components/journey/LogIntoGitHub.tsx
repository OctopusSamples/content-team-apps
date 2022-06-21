import {FC, ReactElement, useContext, useState} from "react";
import {Button, Grid, Link} from "@mui/material";
import {journeyContainer, nextButtonStyle, progressStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import {AppContext} from "../../App";
import LinearProgress from "@mui/material/LinearProgress";

const LogIntoGitHub: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

    const context = useContext(AppContext);

    const [buttonDisabled, setButtonDisabled] = useState<boolean>(false);

    const login = () => {
        setButtonDisabled(true);
        if (context.settings.disableExternalCalls) {
            // pretend to do a login
            props.machine.send("MOCK");
        } else {
            localStorage.setItem("appBuilderState", "loggedIntoGithub");
            window.open(context.settings.githubOauthEndpoint, "_parent");
        }
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
                        <LinearProgress variant="determinate" value={60} sx={progressStyle}/>
                        <Link onClick={() => props.machine.send("BACK")}>&lt; Back</Link>
                        <h2>Log into GitHub.</h2>
                        <p>
                            You must log into GitHub to grant the Octopus Workflow Builder the permissions required to populate
                            a new git repository with the sample application and GitHub Actions workflow.
                        </p>
                        <p>By logging in, you agree to your GitHub email address being collected for marketing or advertising purposes.</p>
                        <p>View the <a target={"_blank"} rel={"noreferrer"} href={"https://oc.to/aNRn3b"}>Octopus privacy policy</a> for more information.</p>
                        <p>
                            Click the login button to be taken to the GitHub login page. You will be returned to this
                            page once you have successfully logged in.
                        </p>
                        <Button sx={nextButtonStyle} variant="outlined" disabled={buttonDisabled} onClick={login}>
                            {"Next >"}
                        </Button>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>
        </>
    );
};

export default LogIntoGitHub;