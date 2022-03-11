import {FC, ReactElement, useContext} from "react";
import {Button, Grid, Link} from "@mui/material";
import {journeyContainer, nextButtonStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import Cookies from "js-cookie";
import {AppContext} from "../../App";

// define the randomUUID function
declare global {
    interface Crypto {
        randomUUID: () => string;
    }
}

const LogIntoOctopus: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

    const context = useContext(AppContext);

    const login = () => {
        if (context.settings.disableExternalCalls) {
            // pretend to do a login
            props.machine.send("MOCK");
        } else {
            const nonce = crypto.randomUUID().toString().replaceAll("-", "").substr(0, 19);
            Cookies.set("appBuilderOctopusNonce", nonce);
            localStorage.setItem("appBuilderState", "loggedIntoOctopus");
            window.open(context.settings.octofrontOauthEndpoint, "_parent");
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