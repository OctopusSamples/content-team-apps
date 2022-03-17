import {FC, ReactElement, useContext, useState} from "react";
import {Button, Grid, Link} from "@mui/material";
import {journeyContainer, nextButtonStyle} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";
import {postJsonApi} from "../../utils/network";
import lightDark from '../../images/spinnerDark.gif'
import Cookies from "js-cookie";
import {AppContext} from "../../App";

const PushPackage: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

    const context = useContext(AppContext);

    const [loading, setLoading] = useState<boolean>(false);

    const pushPackage = () => {
        if (context.settings.disableExternalCalls) {
            // pretend to populate the repo
            props.machine.send("NEXT");
        } else {
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
            postJsonApi(JSON.stringify(body), context.settings.serviceAccountEndpoint, context.settings)
                .then((body: any) => {
                    const populateRepoBody = {
                        "data": {
                            "type": "creategithubrepo",
                            "attributes": {
                                githubOwner: "mcasperson",
                                githubRepository: "AppBuilder",
                                generator: "@octopus-content-team/generator-github-complete-eks-deployment",
                                secrets: [
                                    {name: "OCTOPUS_SERVER", value: "main.testoctopus.app"},
                                    {
                                        name: "OCTOPUS_APIKEY",
                                        value: body.included
                                            .filter((i: any) => i.type === "apikey")
                                            .map((i: any) => i.attributes?.apiKey)
                                            .pop()
                                    },
                                    {name: "AWS_ACCESS_KEY_ID", value: props.machine.state.context.awsAccessKey},
                                    {
                                        name: "AWS_SECRET_ACCESS_KEY",
                                        value: Cookies.get("awsSecretKey"),
                                        encrypted: true
                                    },
                                ],
                                options: {
                                    "awsRegion": "us-west-1",
                                    "octopusUserId": "Users-984"
                                }
                            }
                        }
                    }

                    postJsonApi(JSON.stringify(populateRepoBody), context.settings.githubRepoEndpoint, context.settings, null, () => {
                        // Call this endpoint async
                        const headers = new Headers();
                        headers.set("Invocation-Type", "Event");
                        return headers;
                    })
                        .then(body => {
                            props.machine.send("NEXT")
                        })
                        .catch(() => {
                            setLoading(false);
                            window.alert("DOH!")
                        });
                })
                .catch(() => {
                    setLoading(false);
                    window.alert("DOH!")
                })
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
                    backgroundColor: "rgba(0, 0, 0, 0.95)",
                    zIndex: 10000
                }}>
                    <div style={{
                        position: "fixed",
                        top: "30%",
                        left: "0",
                        right: "0",
                        textAlign: "center",
                        color: "white"
                    }}>
                        <h3>This will take a minute...</h3>
                    </div>
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