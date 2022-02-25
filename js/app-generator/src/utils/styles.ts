import { makeStyles } from '@mui/styles';
import {SxProps} from "@mui/system";
import {Theme} from "@mui/material/styles";

export const styles = makeStyles((theme) => {
        return {
            container: {
                height: "fit-content"
            },
            cell: {
                padding: "8px",
                alignItems: "center"
            },
            label: {
                color: theme.palette.text.primary
            },
            helpText: {
                color: theme.palette.text.primary
            }
        }
    }
);

export const journeyContainer = makeStyles((theme) => {
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

export const buttonStyle: SxProps<Theme> = {
    width: '100%',
    minHeight: '64px',
    marginTop: '8px'
}

export const backButtonStyle: SxProps<Theme> = {
    width: '64px',
    minHeight: '32px',
    marginTop: '8px'
}

export const nextButtonStyle: SxProps<Theme> = {
    width: '128px',
    minHeight: '64px',
    marginTop: '8px'
}