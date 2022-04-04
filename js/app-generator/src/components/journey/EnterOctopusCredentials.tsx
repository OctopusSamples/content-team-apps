import {FC, ReactElement, useState} from "react";
import {Button, FormControl, FormHelperText, Grid, Link, TextField} from "@mui/material";
import {formElements, journeyContainer, nextButtonStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import Cookies from "js-cookie";
import {encryptAndSaveInCookie} from "../../utils/security";

const mask =  "**************";

const EnterOctopusCredentials: FC<JourneyProps> = (props): ReactElement => {
        const classes = journeyContainer();

        const [buttonDisabled, setButtonDisabled] = useState<boolean>(false);
        const [octopusApiKey, setOctopusApiKey] = useState<string>(Cookies.get('octopusApiKey') ? mask : "");
        const [octopusApiKeyError, setOctopusApiKeyError] = useState<string | null>(null);

        const validate = () => {
            if (octopusApiKey.trim() === mask) {
                setOctopusApiKeyError(null);
                return true;
            } else if (!octopusApiKey.trim()) {
                setOctopusApiKeyError("The Octopus API key is a required field.");
                return false;
            } else if (!octopusApiKey.trim().startsWith("API-")) {
                setOctopusApiKeyError("The Octopus API must start with \"API-\".");
                return false;
            } else {
                setOctopusApiKeyError(null);
                return true;
            }
        }

        const nextState = () => {
            setOctopusApiKey("");
            props.machine.send("NEXT");
        }

        const next = () => {
            if (!validate()) {
                return;
            }

            setButtonDisabled(true);

            if (octopusApiKey.trim() !== mask) {
                // Asymmetrically encrypt the secret so the browser can not read it again.
                encryptAndSaveInCookie(octopusApiKey.trim(), "octopusApiKey", 14)
                    .then(nextState)
            } else {
                nextState()
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
                            <h2>Enter an API key for your Octopus instance.</h2>
                            <p>
                                This API key will be used to create a new space in your Octopus instance.
                            </p>
                            <p>
                                You can learn more about creating an API
                                key <a href={"https://octopus.com/docs/octopus-rest-api/how-to-create-an-api-key"}>here</a>,
                                and learn more about users and teams <a href={"https://octopus.com/docs/security/users-and-teams"}>here</a>.
                            </p>
                            <h3>Required Permissions</h3>
                            <p>
                                The account associated with the API key must have the <strong>SpaceCreate</strong> permission.
                            </p>
                            <h3>Octopus API Key</h3>
                            <FormControl error variant="standard" sx={formElements}>
                                <TextField
                                    disabled={buttonDisabled}
                                    value={octopusApiKey}
                                    type="password"
                                    autoComplete="new-password"
                                    onChange={(event) => setOctopusApiKey(event.target.value)}
                                    helperText="Enter the Octopus API key, e.g. API-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                                    aria-describedby="apikey-error-text"/>
                                {octopusApiKeyError &&
                                    <FormHelperText id="apikey-error-text">{octopusApiKeyError}</FormHelperText>}
                            </FormControl>

                            <Button sx={nextButtonStyle} variant="outlined" disabled={buttonDisabled} onClick={next}>
                                {"Next >"}
                            </Button>
                        </Grid>
                    </Grid>
                    <Grid item md={3} xs={0}/>
                </Grid>
            </>
        );
    }
;

export default EnterOctopusCredentials;