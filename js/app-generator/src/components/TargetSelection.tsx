import {FC, ReactElement, useContext} from "react";
import {Helmet} from "react-helmet";
import {makeStyles} from '@mui/styles';
import {Button, Grid} from "@mui/material";
import {AppContext} from "../App";
import {SxProps} from "@mui/system";
import {Theme} from "@mui/material/styles";

const useStyles = makeStyles((theme) => {
    return {
        root: {
            flex: 1,
            display: "flex",
            flexDirection: "row",
            minWidth: "100%",
            minHeight: "100%",
            justifyContent: "center"
        },
        column: {
            flex: 1,
            display: "flex",
            flexDirection: "column",
            minWidth: "100%",
            minHeight: "100%",
            justifyContent: "top"
        }
    }
});

const buttonStyle: SxProps<Theme> = {
    width: '100%',
    minHeight: '64px',
    marginTop: '8px'
}

const TargetSelection: FC = (): ReactElement => {
    const classes = useStyles();
    const {settings} = useContext(AppContext);

    return (
        <>
            <Helmet>
                <title>
                    {settings.title}
                </title>
            </Helmet>
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
                        <h2>Where are you deploying the microservice?</h2>
                        <p>
                            Select the platform that you wish to deploy the sample microservice application to in
                            Octopus. If you only want to download the application code, select the last option.
                        </p>
                        <Button sx={buttonStyle} variant="outlined">
                            {"AWS Elastic Kubernetes Engine (EKS)"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined">
                            {"AWS Elastic Compute Service (ECS)"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined">
                            {"AWS Lambda"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined">
                            {"Download the code with no CI/CD configuration"}
                        </Button>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>
        </>
    );
};

export default TargetSelection;