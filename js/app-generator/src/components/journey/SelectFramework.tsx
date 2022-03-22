import {FC, ReactElement, useState} from "react";
import {Button, Grid, Link} from "@mui/material";
import {buttonStyle, journeyContainer} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";

const SelectFramework: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

    const [buttonDisabled, setButtonDisabled] = useState<boolean>(false);

    const next = (state: string) => {
        setButtonDisabled(true);
        if (props.machine.state) {
            props.machine.state.context.developmentFramework = state;
        }
        props.machine.send(state);
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
                        <h2>What application framework do you wish to deploy?</h2>
                        <p>
                            Select the framework for the sample application you wish to deploy.
                        </p>
                        <Button sx={buttonStyle} variant="outlined" disabled={buttonDisabled} onClick={() => next("QUARKUS")}>
                            {"Java - Quarkus"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined" disabled={buttonDisabled} onClick={() => next("SPRING")}>
                            {"Java - Spring"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined" disabled={buttonDisabled} onClick={() => next("DOTNET")}>
                            {"DotNET Core"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined" disabled={buttonDisabled} onClick={() => next("NODEJS")}>
                            {"Node.js"}
                        </Button>
                        <Button sx={buttonStyle} variant="outlined" disabled={buttonDisabled} onClick={() => next("GO")}>
                            {"Go"}
                        </Button>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>
        </>
    );
};

export default SelectFramework;