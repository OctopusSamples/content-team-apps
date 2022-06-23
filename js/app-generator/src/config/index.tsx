// components
import Branching from "../pages/developer/Branching";
import Health from "../pages/developer/Health";

// interface
import RouteItem from '../model/RouteItem.model';
import Settings from "../pages/developer/Settings";
import Home from "../pages/Home";
import Audits from "../pages/developer/Audits";
import Reports from "../pages/developer/Reports";

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
    },
    {
        key: "router-audits",
        title: "Audits",
        tooltip: "Audits",
        path: "/audits",
        enabled: true,
        component: () => () => <Audits/>,
        appendDivider: true
    },
    {
        key: "router-reports",
        title: "Reports",
        tooltip: "Reports",
        path: "/reports",
        enabled: true,
        component: () => () => <Reports/>,
        appendDivider: true
    }
]