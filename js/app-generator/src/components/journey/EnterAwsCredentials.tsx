import {FC, ReactElement, useState} from "react";
import {Button, FormControl, FormHelperText, FormLabel, Grid, Link, TextField} from "@mui/material";
import {formContainer, formElements, journeyContainer, nextButtonStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import {encryptAndSaveInCookie} from "../../utils/security";
import Cookies from 'js-cookie'

const mask =  "**************";

const PushPackage: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

    const [accessKey, setAccessKey] = useState<string>((props.machine.state && props.machine.state.context.awsAccessKey) || "");
    const [accessKeyError, setAccessKeyError] = useState<string | null>(null);
    const [region, setRegion] = useState<string>((props.machine.state && props.machine.state.context.awsRegion) || "");
    const [regionError, setRegionError] = useState<string | null>(null);
    const [secretKey, setSecretKey] = useState<string>(Cookies.get('awsSecretKey') ? mask : "");
    const [secretKeyError, setSecretKeyError] = useState<string | null>(null);
    const [buttonDisabled, setButtonDisabled] = useState<boolean>(false);

    const validate = () => {
        var error = false;

        if (!accessKey.trim()) {
            setAccessKeyError("The Access Key is a required field.");
            error = true;
        } else if (accessKey.trim().length !== 20) {
            setAccessKeyError("The Access Key must be 20 characters long.");
            error = true;
        } else {
            setAccessKeyError(null)
        }

        if (!secretKey.trim()) {
            setSecretKeyError("The Secret Key is a required field.");
            error = true;
        } else if (secretKey.trim().length !== 40 && secretKey.trim() !== mask) {
            setSecretKeyError("The Secret Key must be 40 characters long.");
            error = true;
        } else {
            setSecretKeyError(null)
        }

        if (!region.trim()) {
            setRegionError("The Region is a required field.");
            error = true;
        } else {
            setRegionError(null)
        }

        return !error;
    }

    const next = () => {
        if (validate()) {
            setButtonDisabled(true);

            if (secretKey !== mask) {
                // Asymmetrically encrypt the secret so the browser can not read it again.
                encryptAndSaveInCookie(secretKey.trim(), "awsSecretKey", 1)
                    .then(nextState)
            } else {
                nextState()
            }
        }
    }

    const nextState = () => {
        setSecretKey("");
        if (props.machine.state) {
            props.machine.state.context.awsAccessKey = accessKey.trim();
            props.machine.state.context.awsRegion = region.trim();
        }
        props.machine.send("NEXT");
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
                        <h2>Enter your AWS credentials.</h2>
                        <p>
                            Enter your AWS access and secret keys. These credentials are used to deploy your application
                            to AWS. They are saved as secrets in GitHub repository that holds your application's code,
                            as well as an Octopus AWS account in your hosted instance.
                        </p>
                        <p>
                            You can find more information about creating access keys in the
                            &nbsp;<a href={"https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys"}>AWS documentation</a>.
                        </p>
                        <Grid container={true} className={classes.row} sx={formContainer}>
                            <Grid md={3} xs={12} container={true}>
                                <FormLabel sx={formElements}>Access Key</FormLabel>
                            </Grid>
                            <Grid md={9} xs={12} container={true}>
                                <FormControl error variant="standard" sx={formElements}>
                                    <TextField
                                        value={accessKey}
                                        onChange={(event) => setAccessKey(event.target.value)}
                                        aria-describedby="access-key-error-text"/>
                                    {accessKeyError && <FormHelperText id="access-key-error-text">{accessKeyError}</FormHelperText>}
                                </FormControl>
                            </Grid>
                            <Grid md={3} xs={12} container={true}>
                                <FormLabel sx={formElements}>Secret Key</FormLabel>
                            </Grid>
                            <Grid md={9} xs={12} container={true}>
                                <FormControl error variant="standard" sx={formElements}>
                                    <TextField
                                           value={secretKey}
                                           type="password"
                                           autoComplete="new-password"
                                           onChange={(event) => setSecretKey(event.target.value)}
                                           aria-describedby="secret-key-error-text"/>
                                    {secretKeyError && <FormHelperText id="secret-key-error-text">{secretKeyError}</FormHelperText>}
                                </FormControl>
                            </Grid>
                            <Grid md={3} xs={12} container={true}>
                                <FormLabel sx={formElements}>Region</FormLabel>
                            </Grid>
                            <Grid md={9} xs={12} container={true}>
                                <FormControl error variant="standard" sx={formElements}>
                                    <TextField
                                        value={region}
                                        onChange={(event) => setRegion(event.target.value)}
                                        aria-describedby="region-error-text"/>
                                    {regionError && <FormHelperText id="region-error-text">{regionError}</FormHelperText>}
                                </FormControl>
                            </Grid>
                        </Grid>
                        <Button sx={nextButtonStyle} variant="outlined" disabled={buttonDisabled} onClick={next}>
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