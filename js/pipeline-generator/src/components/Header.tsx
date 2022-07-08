import clsx from "clsx";
import {FC, useContext, useState} from "react";
import {AppContext} from "../App";
import {
    Brightness3,
    Brightness7,
    ContentCopy,
    History,
    LocalHospital,
    Person,
    PieChart,
    SettingsApplications,
    Share
} from "@mui/icons-material";
import {useHistory} from "react-router-dom";
import {getAccessToken} from "../utils/security";
import {AppBar, createStyles, IconButton, Link, Theme, Toolbar, Tooltip, Typography} from "@mui/material";
import {makeStyles} from "@mui/styles";

const titleSx = {
    flex: 1,
    display: "flex",
    flexDirection: "row",
    alignItems: "center"
}

const toolbarSx = {
    flex: 1,
    display: "flex",
    flexDirection: "row",
    alignItems: "center",
}

const headingSx = {
    color: "white"
}

// define css-in-js
const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        appBar: {
            zIndex: theme.zIndex.drawer + 1,
            transition: theme.transitions.create(["width", "margin"], {
                easing: theme.transitions.easing.sharp,
                duration: theme.transitions.duration.leavingScreen,
            }),
        },
        toolbar: {
            flex: 1,
            display: "flex",
            flexDirection: "row",
            alignItems: "center",
        },
        title: {
            flex: 1,
            display: "flex",
            flexDirection: "row",
            alignItems: "center"
        },
        menuButton: {
            marginRight: 36,
        },
        hide: {
            display: "none",
        },
        heading: {
            color: "white"
        }
    })
);

// define interface to represent component props
interface HeaderProps {
    toggleTheme: () => void;
    useDefaultTheme: boolean;
    copyText?: string;
}

const Header: FC<HeaderProps> = ({
                                     toggleTheme,
                                     useDefaultTheme,
                                     copyText
                                 }: HeaderProps) => {
    const classes = useStyles();
    const context = useContext(AppContext);
    const history = useHistory();
    const [accessToken] = useState<string | null>(getAccessToken());

    return (
        <AppBar
            position="relative"
            elevation={0}
            className={clsx(classes.appBar)}
        >
            <Toolbar sx={toolbarSx}>
                <div sx={titleSx}>
                    <Link href={`${process.env.PUBLIC_URL}/index.html`} sx={headingSx}>
                        <Typography variant="h6">
                            {context.settings.title}
                        </Typography>
                    </Link>
                </div>
                {copyText &&
                    <IconButton onClick={() => navigator.clipboard.writeText(copyText)}>
                        <Tooltip title={"Copy to clipboard"} placement={"bottom"}>
                            <ContentCopy/>
                        </Tooltip>
                    </IconButton>
                }
                {context.developerMode && <div>
                    {accessToken && <IconButton>
                        <Tooltip title={"Logged In"} placement={"bottom"}>
                            <Person/>
                        </Tooltip>
                    </IconButton>}
                    <IconButton onClick={() => history.push('/reports')}>
                        <Tooltip title={"Reports"} placement={"bottom"}>
                            <PieChart/>
                        </Tooltip>
                    </IconButton>
                    <IconButton onClick={() => history.push('/audits')}>
                        <Tooltip title={"Audits"} placement={"bottom"}>
                            <History/>
                        </Tooltip>
                    </IconButton>
                    <IconButton onClick={() => history.push('/branching')}>
                        <Tooltip title={"Branching"} placement={"bottom"}>
                            <Share/>
                        </Tooltip>
                    </IconButton>
                    <IconButton onClick={() => history.push('/settings')}>
                        <Tooltip title={"Settings"} placement={"bottom"}>
                            <SettingsApplications/>
                        </Tooltip>
                    </IconButton>
                    <IconButton onClick={() => history.push('/health')}>
                        <Tooltip title={"Health"} placement={"bottom"}>
                            <LocalHospital/>
                        </Tooltip>
                    </IconButton>
                </div>}
                <IconButton onClick={toggleTheme}>
                    {useDefaultTheme ? (
                        <Tooltip title="Switch to dark mode" placement="bottom">
                            <Brightness3/>
                        </Tooltip>
                    ) : (
                        <Tooltip title="Switch to light mode" placement="bottom">
                            <Brightness7/>
                        </Tooltip>
                    )}
                </IconButton>
            </Toolbar>
        </AppBar>
    );
};

export default Header;