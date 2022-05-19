import {makeStyles} from '@mui/styles';
import {SxProps} from "@mui/system";
import {Theme} from "@mui/material/styles";

export const styles = makeStyles((theme) => {
        return {
            root: {
                flex: 1,
                display: "flex",
                flexDirection: "row",
                minWidth: "100%",
                minHeight: "100%",
                justifyContent: "center"
            },
            book: {
                justifyContent: "center",
                height: "40%",
                width: "30%",
                paddingBottom: "8px"
            },
            bookshelfImage: {
                objectFit: "contain",
                height: "512px",
                cursor: "pointer",
                overflow: "hidden"
            },
            bookshelfTitle: {
                width: "100%",
                textAlign: "center"
            },
            image: {
                objectFit: "contain",
                padding: "64px",
                width: "100%",
                maxHeight: "440px"
            },
            content: {

                "& a": {
                    color: theme.palette.text.primary
                }
            },
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
            },
            icon: {
                width: "32px",
                height: "32px"
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
        },
        row: {
            display: "flex",
            flexDirection: "row",
            minWidth: "100%",
            justifyContent: "center"
        }
    }
});

export const iconStyle: SxProps<Theme> = {
    margin: "16px",
    width: "32px",
    height: "32px"
}

export const progressStyle: SxProps<Theme> = {
    marginBottom: "8px"
}

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
    width: '192px',
    minHeight: '64px',
    marginTop: '32px'
}

export const openResourceStyle: SxProps<Theme> = {
    width: '192px',
    minHeight: '32px'
}

export const formContainer: SxProps<Theme> = {
    alignItems: "center",
    justifyContent: "center"
}

export const formElements: SxProps<Theme> = {
    paddingTop: "8px",
    width: "100%"
}

export const validationError: SxProps<Theme> = {
    color: "red"
}