import {FC, ReactElement, useContext, useEffect, useState} from "react";
import {Helmet} from "react-helmet";
import {AppContext} from "../App";
import {DataGrid} from "@material-ui/data-grid";
import {getJsonApi, isBranchingEnabled} from "../utils/network";
import {Button, Grid} from "@material-ui/core";
import {createStyles, makeStyles} from "@material-ui/core/styles";

interface AuditsCollection {
    data: Audit[]
}

interface Audit {
    id: number,
    attributes: {
        subject: string,
        object: string,
        action: string,
        dataPartition: string,
        time: number
    }
}

const useStyles = makeStyles(() =>
    createStyles({
        mainContent: {
            height: "90%"
        },
        buttonRow: {
            height: "10%",
            paddingTop: "16px"
        }
    })
);

const ROWS_PER_PAGE = 5;

const Audits: FC<{}> = (): ReactElement => {

    const context = useContext(AppContext);
    const classes = useStyles();

    const [audits, setAudits] = useState<AuditsCollection | null>(null);
    const [page, setPage] = useState<number>(0);
    const [error, setError] = useState<string | null>(null);

    const columns = [
        {field: 'id', headerName: 'Id', width: 70},
        {field: 'time', headerName: 'Time Stamp', width: 200},
        {field: 'subject', headerName: 'Subject', width: 300},
        {field: 'action', headerName: 'Action', width: 200},
        {field: 'object', headerName: 'Object', width: 200},
        {field: 'dataPartition', headerName: 'Data Partition', width: 200},
    ];

    useEffect(() => {
        getJsonApi<AuditsCollection>(context.settings.auditEndpoint + "?page[limit]=" + ROWS_PER_PAGE + "&page[offset]=0", context.partition)
            .then(data => setAudits(data))
            .catch(() => setError("Failed to retrieve audit resources. "
                + (isBranchingEnabled() ? "Branching rules are enabled - double check they are valid, or disable them." : "")))
    }, [setAudits, context.settings.auditEndpoint, context.partition]);

    const refresh = (page:number) => {
        setAudits(null);
        setError(null);
        getJsonApi<AuditsCollection>(context.settings.auditEndpoint + "?page[offset]=" + (page * ROWS_PER_PAGE) + "&page[limit]=" + ROWS_PER_PAGE, context.partition)
            .then(data => {
                setAudits(data);
                setPage(page);
            })
            .catch(() => setError("Failed to retrieve audit resources. "
                + (isBranchingEnabled() ? "Branching rules are enabled - double check they are valid, or disable them." : "")))
    }

    return (
        <>
            <Helmet>
                <title>
                    {context.settings.title}
                </title>
            </Helmet>
            {!audits && !error && <div>Loading...</div>}
            {!audits && error && <Grid container={true}>
                <Grid xs={12}>
                    <div>{error}</div>
                </Grid>
                <Grid xs={12}>
                    <Button variant={"outlined"} onClick={() => refresh(0)}>Reload</Button>
                </Grid>
            </Grid>}
            {audits && <Grid container={true}>
                <Grid xs={12} className={classes.mainContent}>
                    <DataGrid
                        pagination
                        paginationMode="server"
                        rowCount={1000}
                        rows={(audits.data || []).map((a: Audit) => ({
                            id: a.id,
                            time: a.attributes.time,
                            subject: a.attributes.subject,
                            action: a.attributes.action,
                            object: a.attributes.object,
                            dataPartition: a.attributes.dataPartition
                        }))}
                        page={page}
                        columns={columns}
                        pageSize={ROWS_PER_PAGE}
                        rowsPerPageOptions={[ROWS_PER_PAGE]}
                        onPageChange={(page) => refresh((page))}
                    />
                </Grid>
                <Grid xs={12} className={classes.buttonRow}>
                    <Button variant={"outlined"} onClick={() => refresh(0)}>Reload</Button>
                </Grid>
            </Grid>}
        </>
    );
}

export default Audits;