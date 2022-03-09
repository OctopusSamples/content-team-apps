import {FC, ReactElement, useContext} from "react";
import {Button, Grid, Link} from "@mui/material";
import {journeyContainer, nextButtonStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import {AppContext} from "../../App";

const LogIntoGitHub: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

    const context = useContext(AppContext);

    const login = () => {
        localStorage.setItem("appBuilderState", "loggedIntoGithub");
        window.open(context.settings.githubOauthEndpoint, "_parent");
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
                        <Link onClick={() => props.machine.send("BACK")}>&lt; Back</Link>
                        <h2>Log into GitHub.</h2>
                        <p>
                            You must log into GitHub to grant the app builder the permissions required to populate
                            a new git repository with the sample application and GitHub Actions workflow.
                        </p>
                        <p>
                            Click the login button to be taken to the GitHub login page. You will be returned to this
                            page once you have successfully logged in.
                        </p>
                        <Button sx={nextButtonStyle} variant="outlined" onClick={login}>
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