// components
import GitHubLogin from "../pages/GitHubLogin";
import Branching from "../pages/Branching";
import Health from "../pages/Health";

// interface
import RouteItem from '../model/RouteItem.model';
import Settings from "../pages/Settings";
import Home from "../pages/Home";

// define app routes
export const routes: Array<RouteItem> = [
    {
        key: "router-home",
        title: "TargetSelection",
        tooltip: "TargetSelection",
        path: "/",
        enabled: true,
        component: () => () => <Home/>,
        appendDivider: true
    },
    {
        key: "router-home-index",
        title: "TargetSelection",
        tooltip: "TargetSelection",
        path: "/index.html",
        enabled: true,
        component: () => () => <Home/>,
        appendDivider: true
    },
    {
        key: "router-githublogin",
        title: "GitHub Login",
        tooltip: "GitHub Login",
        path: "/githublogin",
        enabled: true,
        component: () => () => <GitHubLogin/>,
        appendDivider: true
    },
    {
        key: "router-settings",
        title: "Settings",
        tooltip: "Settings",
        path: "/settings",
        enabled: true,
        component: () => () => <Settings/>,
        appendDivider: true
    },
    {
        key: "router-health",
        title: "Health",
        tooltip: "Health",
        path: "/health",
        enabled: true,
        component: () => () => <Health/>,
        appendDivider: true
    },
    {
        key: "router-branching",
        title: "Branching",
        tooltip: "Branching",
        path: "/branching",
        enabled: true,
        component: () => () => <Branching/>,
        appendDivider: true
    }
]