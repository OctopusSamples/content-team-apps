import {FC, ReactElement, useContext, useEffect, useRef, useState} from "react";
import {Button, FormControl, FormHelperText, Grid, Link, TextField} from "@mui/material";
import {buttonStyle, formElements, journeyContainer, nextButtonStyle, progressStyle} from "../../utils/styles";
import {JourneyProps, saveCurrentState} from "../../statemachine/appBuilder";
import Cookies from "js-cookie";
import {AppContext} from "../../App";
import LinearProgress from "@mui/material/LinearProgress";
import {auditPageVisit} from "../../utils/audit";

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
        const [signupButtonVisible, setSignupButtonVisible] = useState<boolean>(false);
        const [octopusServer, setOctopusServer] = useState<string>((props.machine.state && props.machine.state.context.octopusServer) || "");
        const [octopusServerError, setOctopusServerError] = useState<string | null>(null);

        const octopusServerRef = useRef(octopusServer);
        octopusServerRef.current = octopusServer;

        useEffect(() => {
            /*
                Assume that after 10 seconds the user does not have a cloud instance and
                present the button to sign up for a trial.
             */
            const timer = setTimeout(() => {
                setSignupButtonVisible(octopusServerRef.current === "");
            }, 10000);
            return () => clearTimeout(timer);
        }, []);

        const openSignUp = () => {
            auditPageVisit("externalOctopusSignup", context.settings, context.partition);
            window.open("https://octopus.com/start/cloud", '_blank');
        }

        /**
         * Extract the hostname from a fully formed URL if that was pasted in.
         *
         * @param input The server name
         * @returns The hostname of a full URL, or the input if it wasn't a URL
         */
        const sanitizeUrl = (input: string) => {
            if (!input) return input;

            try {
                const url = new URL(input.trim());
                return url.hostname;
            } catch {
                return input;
            }
        }

        const validate = () => {
            const santizedUrl = sanitizeUrl(octopusServer);

            if (!santizedUrl.trim()) {
                setOctopusServerError("The Octopus server name is a required field.");
                return false;
            } else if (!santizedUrl.trim().endsWith("octopus.app")) {
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

            const santizedUrl = sanitizeUrl(octopusServer);

            if (props.machine.state) {
                props.machine.state.context.octopusServer = santizedUrl;
                // We are leaving the site, so need to save the state so we can retain the Octopus instance name
                saveCurrentState("doYouHaveCloudOctopus");
            }

            if (context.settings.disableOctofrontLogin) {
                props.machine.send("APIKEY");
            } else if (context.settings.disableExternalCalls) {
                // pretend to do a login
                props.machine.send("MOCK");
            } else {
                // Clear the manually entered API key
                Cookies.set("octopusApiKey", "");

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
                            <LinearProgress variant="determinate" value={40} sx={progressStyle}/>
                            <Link onClick={() => props.machine.send("BACK")}>&lt; Back</Link>
                            <h2>Log into you cloud Octopus instance.</h2>
                            <img src={"hostname.png"} style={{width: "100%"}} alt={"Browser address bar"}/>
                            <p>
                                Please enter the hostname of the Octopus Cloud instance that you which to populate with the
                                sample deployment project.
                            </p>
                            <FormControl error variant="standard" sx={formElements}>
                                <TextField
                                    disabled={buttonDisabled}
                                    value={octopusServer}
                                    onChange={(event) => setOctopusServer(event.target.value)}
                                    helperText="Enter the Octopus server hostname, e.g. myinstance.octopus.app"
                                    aria-describedby="server-error-text"/>
                                {octopusServerError &&
                                    <FormHelperText id="server-error-text">{octopusServerError}</FormHelperText>}
                            </FormControl>

                            <Button sx={nextButtonStyle} variant="outlined" disabled={buttonDisabled} onClick={login}>
                                {context.settings.disableOctofrontLogin ? "Next >" : "Login >"}
                            </Button>

                            {signupButtonVisible && <>
                                <p>
                                    If you do not have a cloud Octopus instance, click the Signup button below to create
                                    a free trial account.
                                </p>
                                <Button sx={buttonStyle} variant="outlined" onClick={openSignUp}>
                                    {"Signup"}
                                </Button>
                            </>}
                        </Grid>
                    </Grid>
                    <Grid item md={3} xs={0}/>
                </Grid>
            </>
        );
    }
;

export default LogIntoOctopus;