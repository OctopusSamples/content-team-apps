import {FC, ReactElement, useState} from "react";
import {Button, FormLabel, Grid, Link, TextField} from "@mui/material";
import {formContainer, formElements, journeyContainer, nextButtonStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import {encryptAndSaveInCookie} from "../../utils/security";
import Cookies from 'js-cookie'

const PushPackage: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

    const [accessKey, setAccessKey] = useState<string>((props.machine.state && props.machine.state.context.awsAccessKey) || "");
    const [region, setRegion] = useState<string>((props.machine.state && props.machine.state.context.awsRegion) || "");
    const [secretKey, setSecretKey] = useState<string>(Cookies.get('awsSecretKey') ? "**************" : "");
    const [buttonDisabled, setButtonDisabled] = useState<boolean>(false);

    const next = () => {
        setButtonDisabled(true);
        // Asymmetrically encrypt the secret so the browser can not read it again.
        encryptAndSaveInCookie(secretKey, "awsSecretKey", 1)
            .then(() => {
                setSecretKey("");
                if (props.machine.state) {
                    props.machine.state.context.awsAccessKey = accessKey;
                    props.machine.state.context.awsRegion = region;
                }
                props.machine.send("NEXT");
            })

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
                            For your security, once the secret key is supplied, it is encrypted with a one-way algorithm,
                            and the original value is discarded by the app builder.
                        </p>
                        <Grid container={true} className={classes.row} sx={formContainer}>
                            <Grid md={3} xs={12} container={true}>
                                <FormLabel sx={formElements}>Access Key</FormLabel>
                            </Grid>
                            <Grid md={9} xs={12} container={true}>
                                <TextField sx={formElements} value={accessKey} onChange={(event) => setAccessKey(event.target.value)}/>
                            </Grid>
                            <Grid md={3} xs={12} container={true}>
                                <FormLabel sx={formElements}>Secret Key</FormLabel>
                            </Grid>
                            <Grid md={9} xs={12} container={true}>
                                <TextField sx={formElements} value={secretKey} type="password" autoComplete="new-password" onChange={(event) => setSecretKey(event.target.value)}/>
                            </Grid>
                            <Grid md={3} xs={12} container={true}>
                                <FormLabel sx={formElements}>Region</FormLabel>
                            </Grid>
                            <Grid md={9} xs={12} container={true}>
                                <TextField sx={formElements} value={region} onChange={(event) => setRegion(event.target.value)}/>
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