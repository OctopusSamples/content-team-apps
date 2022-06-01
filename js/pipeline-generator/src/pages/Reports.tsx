import {FC, ReactElement, useContext, useEffect, useState} from "react";
import {getJsonApi, isBranchingEnabled} from "../utils/network";
import {AppContext} from "../App";
import {AuditsCollection} from "./Audits";

const Reports: FC<{}> = (): ReactElement => {
    const context = useContext(AppContext);
    const [emailAudits, setEmailAudits] = useState<AuditsCollection | null>(null);
    const [templateAudits, setTemplateAudits] = useState<AuditsCollection | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fourWeeksAgo = new Date(new Date().getTime() - (28 * 24 * 60 * 60 * 1000));

        getJsonApi<AuditsCollection>(context.settings.auditEndpoint + "?page[limit]=10000&page[offset]=0&filter=action==CreateTemplateFor;time>=" + fourWeeksAgo.toISOString(), context.partition)
            .then(data => {
                setEmailAudits(data);
            })
            .catch(() => setError("Failed to retrieve audit resources. Make sure you are logged in. "
                + (isBranchingEnabled() ? "Branching rules are enabled - double check they are valid, or disable them." : "")))
    }, [setEmailAudits, context.settings.auditEndpoint, context.partition]);

    useEffect(() => {
        const fourWeeksAgo = new Date(new Date().getTime() - (28 * 24 * 60 * 60 * 1000));

        getJsonApi<AuditsCollection>(context.settings.auditEndpoint + "?page[limit]=10000&page[offset]=0&filter=action==CreateTemplateUsing;time>=" + fourWeeksAgo.toISOString(), context.partition)
            .then(data => {
                setTemplateAudits(data);
            })
            .catch(() => setError("Failed to retrieve audit resources. Make sure you are logged in. "
                + (isBranchingEnabled() ? "Branching rules are enabled - double check they are valid, or disable them." : "")))
    }, [setEmailAudits, context.settings.auditEndpoint, context.partition]);

    return <div>
        {error && <span>{error}</span>}
        <h1>Last 28 Days</h1>
        <p>Emails collected: {emailAudits?.data?.length} (Note some of these email addresses are unusable "no-reply" addresses.)</p>
        <p>Jenkins templates: {templateAudits?.data?.filter(a => a.attributes.subject === "JenkinsPipelineBuilder").length}</p>
        <p>GitHub Actions templates: {templateAudits?.data?.filter(a => a.attributes.subject === "GithubActionWorkflowBuilder").length}</p>
        <p>Node.js templates: {templateAudits?.data?.filter(a => a.attributes.object === "Node.js").length}</p>
        <p>DotNET Core templates: {templateAudits?.data?.filter(a => a.attributes.object === "DotNET Core").length}</p>
        <p>Generic templates: {templateAudits?.data?.filter(a => a.attributes.object === "Generic").length}</p>
        <p>Go templates: {templateAudits?.data?.filter(a => a.attributes.object === "Go").length}</p>
        <p>Java Gradle templates: {templateAudits?.data?.filter(a => a.attributes.object === "Java Gradle").length}</p>
        <p>Java Maven templates: {templateAudits?.data?.filter(a => a.attributes.object === "Java Maven").length}</p>
        <p>PHP templates: {templateAudits?.data?.filter(a => a.attributes.object === "PHP").length}</p>
        <p>Python templates: {templateAudits?.data?.filter(a => a.attributes.object === "Python").length}</p>
        <p>Ruby templates: {templateAudits?.data?.filter(a => a.attributes.object === "Ruby").length}</p>
    </div>
}

export default Reports;