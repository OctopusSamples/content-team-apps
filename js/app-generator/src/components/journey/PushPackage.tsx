import {FC, ReactElement, useState} from "react";
import {Button, Grid, Link} from "@mui/material";
import {journeyContainer, nextButtonStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import {postJsonApi} from "../../utils/network";
import lightDark from '../../images/spinnerDark.gif'
import Cookies from "js-cookie";

const PushPackage: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

    const [loading, setLoading] = useState<boolean>(false);

    const pushPackage = () => {
        setLoading(true);
        const body = {
            "data": {
                "type": "createserviceaccount",
                "attributes": {
                    "username": "AppBuilder",
                    "displayName": "App Builder Service Account",
                    "isService": true,
                    "octopusServer": "main.testoctopus.app"
                }
            }
        };
        postJsonApi(JSON.stringify(body), "http://localhost:12000/api/serviceaccounts")
            .then(body => {
                const populateRepoBody = {
                    "data": {
                        "type": "creategithubrepo",
                        "attributes": {
                            githubOwner: "mcasperson",
                            githubRepository: "AppBuilder",
                            secrets: [
                                {name: "OCTOPUS_SERVER", value: "main.testoctopus.app"},
                                {name: "OCTOPUS_APIKEY", value: Cookies.get("OctopusUserSession"), serverSideEncrypted: true},
                                {name: "AWS_ACCESS_KEY_ID", value: props.machine.state.context.awsAccessKey, encrypted: true},
                                {name: "AWS_SECRET_ACCESS_KEY", value: Cookies.get("awsSecretKey"), clientSideEncrypted: true},
                            ]
                        }
                    }
                }

                props.machine.send("NEXT")
            })
            .catch(() => props.machine.send("ERROR"))
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
                        <h2>Push the sample application and deployment configuration to GitHub.</h2>
                        <p>
                            Congratulations! You are now ready to create a new GitHub repository with the sample
                            application code and GitHub Actions workflow that configures Octopus, builds the code,
                            pushes the package to Octopus, and deploys the application.
                        </p>
                        <p>
                            Click the next button to configure your CI/CD pipeline.
                        </p>
                        <Button sx={nextButtonStyle} variant="outlined" onClick={pushPackage}>
                            {"Next >"}
                        </Button>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>
            {loading &&
                <div style={{
                    position: "fixed",
                    top: "0",
                    left: "0",
                    bottom: "0",
                    right: "0",
                    backgroundColor: "rgba(0, 0, 0, 0.8)",
                    zIndex: 10000
                }}>
                    <img alt="loading" id="spinner" src={lightDark} style={{
                        position: "fixed",
                        top: "50%",
                        left: "50%",
                        marginTop: "-64px",
                        marginLeft: "-64px",
                        zIndex: 10001
                    }}/>
                </div>}
        </>
    );
};

export default PushPackage;