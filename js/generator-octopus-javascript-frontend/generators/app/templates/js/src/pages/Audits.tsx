import {FC, ReactElement, useContext, useEffect, useState} from "react";
import {Helmet} from "react-helmet";
import {AppContext} from "../App";
import {getJsonApi, isBranchingEnabled} from "../utils/network";
import {DataGrid} from "@mui/x-data-grid";

interface AuditsCollection {
    data: Audit[]
}

interface Audit {
    id: number,
    attributes: {
        subject: string,
        object: string,
        action: string,
        dataPartition: string
    }
}

const Audits: FC<{}> = (): ReactElement => {

    const context = useContext(AppContext);

    context.setAllBookId("");

    const [audits, setAudits] = useState<AuditsCollection | null>(null);
    const [error, setError] = useState<string | null>(null);

    const columns = [
        {field: 'id', headerName: 'Id', width: 70},
        {field: 'subject', headerName: 'Subject', width: 130},
        {field: 'action', headerName: 'Action', width: 200},
        {field: 'object', headerName: 'Object', width: 200},
        {field: 'dataPartition', headerName: 'Data Partition', width: 130},
    ];

    useEffect(() => {
        getJsonApi<AuditsCollection>(context.settings.auditEndpoint, context.partition)
            .then(data => setAudits(data))
            .catch(() => setError("Failed to retrieve audit resources. "
                + (isBranchingEnabled() ? "Branching rules are enabled - double check they are valid, or disable them." : "")))
    }, [setAudits, context.settings.auditEndpoint, context.partition]);

    return (
        <>
            <Helmet>
                <title>
                    {context.settings.title}
                </title>
            </Helmet>
            {!audits && !error && <div>Loading...</div>}
            {!audits && error && <div>{error}</div>}
            {audits && <DataGrid
                rows={audits.data.map((a: Audit) => ({
                    id: a.id,
                    subject: a.attributes.subject,
                    action: a.attributes.action,
                    object: a.attributes.object,
                    dataPartition: a.attributes.dataPartition
                }))}
                columns={columns}
                pageSize={5}
                rowsPerPageOptions={[5]}
            />}

        </>
    );
}

export default Audits;