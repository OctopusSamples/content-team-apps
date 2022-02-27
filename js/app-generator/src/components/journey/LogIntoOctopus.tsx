import {FC, ReactElement} from "react";
import {Button, Grid, Link} from "@mui/material";
import {journeyContainer, nextButtonStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";

const LogIntoOctopus: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

    const login = () => {
        localStorage.setItem("appBuilderState", "loggedIntoOctopus");
        window.open("https://octopus.com/oauth2/authorize?"
            + "client_id=855b8e5a-c3c4-4c4d-91b1-fef5dd762ec2&scope=openid%20profile%20email"
            + "&response_type=code+id_token"
            + "&response_mode=form_post&nonce=dhIdjCXzcBPsaYUJuUQ"
            + "&redirect_uri=http://localhost:10000/oauth/octopus",
            "_parent");
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
                        <h2>Log into you cloud Octopus instance.</h2>
                        <p>
                            You must log into your cloud Octopus instance to allow the app builder to configure your
                            application deployment process.
                        </p>
                        <p>
                            Click the login button to be taken to the Octopus login page. You will be returned to this
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

export default LogIntoOctopus;