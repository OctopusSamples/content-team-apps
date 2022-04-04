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
    const [buttonDisabled, setButtonDisabled] = useState<boolean>(false);

    function createRepoName() {
        return "AppBuilder-" + props.machine.state.context.targetPlatform;
    }

    function getOctopusServer() {
        if (props.machine.state.context.octopusServer) {
            try {
                const url = new URL(props.machine.state.context.octopusServer);
                return "https://" + url.hostname;
            } catch {
                return "https://" + props.machine.state.context.octopusServer.split("/")[0];
            }
        }
        // Let the service return an error in its response code, and handle the response as usual.
        return "";
    }

    const createServiceAccount = (callback: (apiKey: string, apiKeyEncrypted: boolean, server: string) => void) => {
        const body = {
            "data": {
                "type": "createserviceaccount",
                "attributes": {
                    "username": "AppBuilder",
                    "displayName": "App Builder Service Account",
                    "isService": true,
                    "octopusServer": getOctopusServer()
                }
            }
        };
        postJsonApi(JSON.stringify(body), context.settings.serviceAccountEndpoint, context.settings)
            .then(body => {
                const bodyObject = body as any;
                callback(
                    bodyObject.included
                        .filter((i: any) => i.type === "apikey")
                        .map((i: any) => i.attributes?.apiKey)
                        .pop(),
                    false,
                    bodyObject.data.attributes.octopusServer);
            })
            .catch(() => {
                setLoading(false);
                window.alert("DOH!")
            });
    }

    /**
     * Determines if the values we have allow for a valid repo to be created.
     * We can't continue if there is no generator defined, or if the cookies holding
     * the api and secret keys have expired.
     */
    const selectionsValid = () => {
        if (props.machine.state) {
          return !!props.machine.state.context.generator &&
              !!Cookies.get("awsSecretKey") &&
              !!Cookies.get("octopusApiKey");
        }

        return false;
    }

    const populateGitHubRepo = (apiKey: string, apiKeyEncrypted: boolean, server: string) => {
        const populateRepoBody = {
            "data": {
                "type": "creategithubrepo",
                "attributes": {
                    githubRepository: createRepoName(),
                    createNewRepo: false,
                    generator: props.machine.state.context.generator,
                    secrets: [
                        {
                            name: "OCTOPUS_SERVER",
                            value: server
                        },
                        {
                            name: "OCTOPUS_APIKEY",
                            value: apiKey,
                            encrypted: apiKeyEncrypted
                        },
                        {name: "AWS_ACCESS_KEY_ID", value: props.machine.state.context.awsAccessKey},
                        {
                            name: "AWS_SECRET_ACCESS_KEY",
                            value: Cookies.get("awsSecretKey"),
                            encrypted: true
                        },
                        {
                            name: "TERRAFORM_BUCKET_SUFFIX",
                            value: crypto.randomUUID(),
                            preserveExistingSecret: true
                        },
                        {
                            name: "TERRAFORM_BUCKET_REGION",
                            value: props.machine.state.context.awsRegion,
                            preserveExistingSecret: true
                        },
                    ],
                    options: {
                        "awsStateBucketRegion": "$TERRAFORM_BUCKET_REGION",
                        "s3BucketSuffix": "$TERRAFORM_BUCKET_SUFFIX",
                        "awsRegion": props.machine.state.context.awsRegion,
                        "framework": "",
                        "platform": props.machine.state.context.targetPlatform
                    }
                }
            }
        }

        postJsonApi(JSON.stringify(populateRepoBody), context.settings.githubRepoEndpoint, context.settings, null, true, () => {
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
    }

    const pushPackage = () => {
        if (!selectionsValid()) {
            props.machine.send("ERROR");
            return;
        }

        setButtonDisabled(true);

        props.machine.state.context.githubRepo = createRepoName();

        if (context.settings.disableExternalCalls) {
            // pretend to populate the repo
            props.machine.send("NEXT");
        } else {
            setLoading(true);
            const manuallyEnteredApiKey = Cookies.get("octopusApiKey");

            /*
                If we manually enter the API key, create the GitHub repo with the entered credentials.
                If there is no manually entered API key, assume the service account is to be created using the ID token
                from an octofront login.
             */
            if (manuallyEnteredApiKey) {
                populateGitHubRepo(manuallyEnteredApiKey, true, getOctopusServer());
            } else {
                createServiceAccount(populateGitHubRepo);
            }
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
                        <Button sx={nextButtonStyle} variant="outlined" disabled={buttonDisabled} onClick={pushPackage}>
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