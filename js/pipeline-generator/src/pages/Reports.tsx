import {FC, ReactElement, useContext, useEffect, useState} from "react";
import {getJsonApi, isBranchingEnabled} from "../utils/network";
import {AppContext} from "../App";
import {AuditsCollection} from "./Audits";

const Reports: FC<{}> = (): ReactElement => {
    const context = useContext(AppContext);
    const [emailAuditsFourWeeks, setEmailAuditsFourWeeks] = useState<AuditsCollection | null>(null);
    const [emailAuditsOneWeek, setEmailAuditsOneWeek] = useState<AuditsCollection | null>(null);
    const [templateAuditsFourWeeks, setTemplateAuditsFourWeeks] = useState<AuditsCollection | null>(null);
    const [templateAuditsOneWeek, setTemplateAuditsOneWeek] = useState<AuditsCollection | null>(null);
    const [error, setError] = useState<string | null>(null);


    useEffect(() => {
        const fourWeeksAgo = new Date(new Date().getTime() - (28 * 24 * 60 * 60 * 1000));
        const oneWeekAgo = new Date(new Date().getTime() - (7 * 24 * 60 * 60 * 1000));

        getJsonApi<AuditsCollection>(context.settings.auditEndpoint + "?page[limit]=10000&page[offset]=0&filter=action==CreateTemplateFor;time>=" + fourWeeksAgo.toISOString(), "main")
            .then(data => {
                setEmailAuditsFourWeeks(data);
                setEmailAuditsOneWeek({...data, data: data.data?.filter(a => a.attributes.time >= oneWeekAgo.getTime())});
            })
            .catch(() => setError("Failed to retrieve audit resources. Make sure you are logged in. "
                + (isBranchingEnabled() ? "Branching rules are enabled - double check they are valid, or disable them." : "")))
    }, [setEmailAuditsFourWeeks, setEmailAuditsOneWeek, context.settings.auditEndpoint, context.partition]);

    useEffect(() => {
        const fourWeeksAgo = new Date(new Date().getTime() - (28 * 24 * 60 * 60 * 1000));
        const oneWeekAgo = new Date(new Date().getTime() - (7 * 24 * 60 * 60 * 1000));

        getJsonApi<AuditsCollection>(context.settings.auditEndpoint + "?page[limit]=10000&page[offset]=0&filter=action==CreateTemplateUsing;time>=" + fourWeeksAgo.toISOString(), "main")
            .then(data => {
                setTemplateAuditsFourWeeks(data);
                setTemplateAuditsOneWeek({...data, data: data.data?.filter(a => a.attributes.time >= oneWeekAgo.getTime())});
            })
            .catch(() => setError("Failed to retrieve audit resources. Make sure you are logged in. "
                + (isBranchingEnabled() ? "Branching rules are enabled - double check they are valid, or disable them." : "")))
    }, [setEmailAuditsFourWeeks, setTemplateAuditsOneWeek, context.settings.auditEndpoint, context.partition]);

    return <div>
        {error && <span>{error}</span>}
        <p>Note some of these email addresses are unusable "no-reply" addresses.</p>
        <table>
            <tr>
                <td style={{padding: "32px"}}>
                    <h1>Last 28 Days</h1>
                    <p>Emails collected: {emailAuditsFourWeeks?.data?.length}</p>
                    <p>Jenkins templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.subject === "JenkinsPipelineBuilder").length}</p>
                    <p>GitHub Actions templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.subject === "GithubActionWorkflowBuilder").length}</p>
                    <p>Node.js templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Node.js").length}</p>
                    <p>DotNET Core templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "DotNET Core").length}</p>
                    <p>Generic templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Generic").length}</p>
                    <p>Go templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Go").length}</p>
                    <p>Java Gradle templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Java Gradle").length}</p>
                    <p>Java Maven templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Java Maven").length}</p>
                    <p>PHP templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "PHP").length}</p>
                    <p>Python templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Python").length}</p>
                    <p>Ruby templates: {templateAuditsFourWeeks?.data?.filter(a => a.attributes.object === "Ruby").length}</p>
                </td>
                <td style={{padding: "32px"}}>
                    <h1>Last 7 Days</h1>
                    <p>Emails collected: {emailAuditsOneWeek?.data?.length}</p>
                    <p>Jenkins templates: {templateAuditsOneWeek?.data?.filter(a => a.attributes.subject === "JenkinsPipelineBuilder").length}</p>
                    <p>GitHub Actions templates: {templateAuditsOneWeek?.data?.filter(a => a.attributes.subject === "GithubActionWorkflowBuilder").length}</p>
                    <p>Node.js templates: {templateAuditsOneWeek?.data?.filter(a => a.attributes.object === "Node.js").length}</p>
                    <p>DotNET Core templates: {templateAuditsOneWeek?.data?.filter(a => a.attributes.object === "DotNET Core").length}</p>
                    <p>Generic templates: {templateAuditsOneWeek?.data?.filter(a => a.attributes.object === "Generic").length}</p>
                    <p>Go templates: {templateAuditsOneWeek?.data?.filter(a => a.attributes.object === "Go").length}</p>
                    <p>Java Gradle templates: {templateAuditsOneWeek?.data?.filter(a => a.attributes.object === "Java Gradle").length}</p>
                    <p>Java Maven templates: {templateAuditsOneWeek?.data?.filter(a => a.attributes.object === "Java Maven").length}</p>
                    <p>PHP templates: {templateAuditsOneWeek?.data?.filter(a => a.attributes.object === "PHP").length}</p>
                    <p>Python templates: {templateAuditsOneWeek?.data?.filter(a => a.attributes.object === "Python").length}</p>
                    <p>Ruby templates: {templateAuditsOneWeek?.data?.filter(a => a.attributes.object === "Ruby").length}</p>
                </td>
            </tr>
        </table>
    </div>
}

export default Reports;