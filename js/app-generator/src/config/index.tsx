// icons
import HomeIcon from '@material-ui/icons/Home';

// components
import Home from '../pages/Home';
import GitHubLogin from "../pages/GitHubLogin";
import Branching from "../pages/Branching";
import Health from "../pages/Health";

// interface
import RouteItem from '../model/RouteItem.model';
import Settings from "../pages/Settings";

// define app routes
export const routes: Array<RouteItem> = [
    {
        key: "router-home",
        title: "Home",
        tooltip: "Home",
        path: "/",
        enabled: true,
        component: () => () => <Home/>,
        icon: HomeIcon,
        appendDivider: true
    },
    {
        key: "router-home-index",
        title: "Home",
        tooltip: "Home",
        path: "/index.html",
        enabled: true,
        component: () => () => <Home/>,
        icon: HomeIcon,
        appendDivider: true
    },
    {
        key: "router-githublogin",
        title: "GitHub Login",
        tooltip: "GitHub Login",
        path: "/githublogin",
        enabled: true,
        component: () => () => <GitHubLogin/>,
        icon: HomeIcon,
        appendDivider: true
    },
    {
        key: "router-settings",
        title: "Settings",
        tooltip: "Settings",
        path: "/settings",
        enabled: true,
        component: () => () => <Settings/>,
        icon: HomeIcon,
        appendDivider: true
    },
    {
        key: "router-health",
        title: "Health",
        tooltip: "Health",
        path: "/health",
        enabled: true,
        component: () => () => <Health/>,
        icon: HomeIcon,
        appendDivider: true
    },
    {
        key: "router-branching",
        title: "Branching",
        tooltip: "Branching",
        path: "/branching",
        enabled: true,
        component: () => () => <Branching/>,
        icon: HomeIcon,
        appendDivider: true
    }
]