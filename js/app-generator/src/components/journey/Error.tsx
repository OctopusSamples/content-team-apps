import {FC, ReactElement} from "react";
import {Grid} from "@mui/material";
import {journeyContainer} from "../../utils/styles";
import {JourneyProps} from "../../statemachine/appBuilder";

const Error: FC<JourneyProps> = (props): ReactElement => {
    const classes = journeyContainer();

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
                        <h2>Doh!</h2>
                        <p>
                            Unfortunately the Octopus Builder has encountered an error and can not proceed. This issue has
                            been logged, so check back later to see if the problem has been resolved.
                        </p>
                    </Grid>
                </Grid>
                <Grid item md={3} xs={0}/>
            </Grid>
        </>
    );
};

export default Error;