import clsx from "clsx";
import {AppBar, IconButton, Link, Theme, Toolbar, Tooltip, Typography} from "@mui/material";
import {makeStyles} from '@mui/styles';
import Brightness7Icon from "@mui/icons-material/Brightness7";
import Brightness3Icon from "@mui/icons-material/Brightness3";
import {FC, useContext} from "react";
import {AppContext} from "../App";
import {History, LocalHospital, SettingsApplications, Share} from "@mui/icons-material";
import {useNavigate} from "react-router-dom";

const useStyles = makeStyles((theme: Theme) => {
        return {
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
        }
    }
);

// define interface to represent component props
interface HeaderProps {
    toggleTheme: () => void;
    useDefaultTheme: boolean;
    enableToggle: boolean;
}

const Header: FC<HeaderProps> = ({
                                     toggleTheme,
                                     useDefaultTheme,
                                     enableToggle
                                 }: HeaderProps) => {
    const classes = useStyles();
    const context = useContext(AppContext);
    const history = useNavigate();
    return (
        <AppBar
            position="relative"
            elevation={0}
            className={clsx(classes.appBar)}
        >
            <Toolbar className={classes.toolbar}>
                <div className={classes.title}>
                    <Link href={`${process.env.PUBLIC_URL}/index.html`} className={classes.heading}>
                        <Typography variant="h6">
                            {context.settings.title}
                        </Typography>
                    </Link>
                    &nbsp;{context.settings.frontendVersion}
                </div>
                {context.developerMode && <div>
                    <IconButton onClick={() => history('/audits')} size="large">
                        <Tooltip title={"Audits"} placement={"bottom"}>
                            <History/>
                        </Tooltip>
                    </IconButton>
                    <IconButton onClick={() => history('/branching')} size="large">
                        <Tooltip title={"Branching"} placement={"bottom"}>
                            <Share/>
                        </Tooltip>
                    </IconButton>
                    <IconButton onClick={() => history('/settings')} size="large">
                        <Tooltip title={"Settings"} placement={"bottom"}>
                            <SettingsApplications/>
                        </Tooltip>
                    </IconButton>
                    <IconButton onClick={() => history('/health')} size="large">
                        <Tooltip title={"Health"} placement={"bottom"}>
                            <LocalHospital/>
                        </Tooltip>
                    </IconButton>
                </div>}
                {enableToggle &&
                    <IconButton onClick={toggleTheme} size="large">
                        {useDefaultTheme ? (
                            <Tooltip title="Switch to dark mode" placement="bottom">
                                <Brightness3Icon/>
                            </Tooltip>
                        ) : (
                            <Tooltip title="Switch to light mode" placement="bottom">
                                <Brightness7Icon/>
                            </Tooltip>
                        )}
                    </IconButton>
                }
            </Toolbar>
        </AppBar>
    );
};

export default Header;