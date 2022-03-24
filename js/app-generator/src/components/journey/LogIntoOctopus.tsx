import {FC, ReactElement, useContext, useState} from "react";
import {Button, FormControl, FormHelperText, Grid, Link, TextField} from "@mui/material";
import {formElements, journeyContainer, nextButtonStyle} from "../../utils/styles";
import {JourneyProps, saveCurrentState} from "../../statemachine/appBuilder";
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

        const [buttonDisabled, setButtonDisabled] = useState<boolean>(false);
        const [octopusServer, setOctopusServer] = useState<string>("main.testoctopus.app");
        const [octopusServerError, setOctopusServerError] = useState<string | null>(null);

        const validate = () => {
            if (!octopusServer.trim()) {
                setOctopusServerError("The Octopus server name is a required field.");
                return false;
            } else if (!octopusServer.trim().endsWith("octopus.app")) {
                setOctopusServerError("The Octopus server hostname must end with octopus.app.");
                return false;
            } else {
                setOctopusServerError(null);
                return true;
            }
        }

        const login = () => {
            if (!validate()) {
                return;
            }

            setButtonDisabled(true);

            if (props.machine.state) {
                props.machine.state.context.octopusServer = octopusServer.trim();
                // We are leaving the site, so need to save the state so we can retain the Octopus instance name
                saveCurrentState("doYouHaveCloudOctopus");
            }

            if (context.settings.disableExternalCalls) {
                // pretend to do a login
                props.machine.send("MOCK");
            } else {
                const nonce = crypto.randomUUID().toString().replaceAll("-", "").substring(0, 19);
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
                                Please enter the hostname of the Octopus Cloud instance that you which to populate with the
                                sample deployment project and click the login button to be taken to the Octopus login page.
                            </p>
                            <p>
                                <strong>This field is currently locked to main.testoctopus.app for testing.</strong>
                            </p>
                            <FormControl error variant="standard" sx={formElements}>
                                <TextField
                                    disabled={true}
                                    value={octopusServer}
                                    onChange={(event) => setOctopusServer(event.target.value)}
                                    helperText="Enter the Octopus server hostname, e.g. myinstance.octopus.app"
                                    aria-describedby="server-error-text"/>
                                {octopusServerError &&
                                    <FormHelperText id="server-error-text">{octopusServerError}</FormHelperText>}
                            </FormControl>

                            <Button sx={nextButtonStyle} variant="outlined" disabled={buttonDisabled} onClick={login}>
                                {"Login >"}
                            </Button>
                            <p style={{marginTop: "80px", color: "grey"}}>
                                Octonauts - If you don't have a hosted instance, use <strong>main.testoctopus.app</strong>.
                                Ping the team in #secops-requests to get access to this instance if you don't already
                                have it.
                            </p>
                        </Grid>
                    </Grid>
                    <Grid item md={3} xs={0}/>
                </Grid>
            </>
        );
    }
;

export default LogIntoOctopus;