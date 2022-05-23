import {FC, ReactElement, useContext, useState} from "react";
import {Helmet} from "react-helmet";
import {AppContext} from "../../App";
import {DataGrid, GridCellEditCommitParams, GridRowId} from "@mui/x-data-grid";
import {styles} from "../../utils/styles";
import {Button, Checkbox, FormLabel, Grid} from "@mui/material";
import {getSavedBranchingRules} from "../../utils/network";

export interface RedirectRule {
    id: number,
    path: string,
    destination: string
}

const Branching: FC = (): ReactElement => {

    const context = useContext(AppContext);

    const classes = styles();

    const [rules, setRules] = useState<RedirectRule[]>(JSON.parse(getSavedBranchingRules() || "[]"));
    const [rulesEnabled, setRulesEnabled] = useState<boolean>((localStorage.getItem("branchingEnabled") || "").toLowerCase() === "true");

    const columns = [
        {field: 'id', headerName: 'Index', width: 30},
        {field: 'path', headerName: 'Path', editable: true, width: 400},
        {field: 'destination', headerName: 'Destination', editable: true, width: 600}
    ];

    let selectedRows: GridRowId[] = [];

    return (
        <>
            <Helmet>
                <title>
                    {context.settings.title}
                </title>
            </Helmet>
            <Grid container={true} className={classes.container}>
                <Grid className={classes.cell} xs={12}>
                    <DataGrid
                        style={{height: "50vh"}}
                        rows={rules}
                        columns={columns}
                        pageSize={10}
                        rowsPerPageOptions={[10]}
                        onSelectionModelChange={(selection) => selectedRows = selection}
                        onCellEditCommit={onEdit}
                    />
                </Grid>
                <Grid container={true} className={classes.cell} xs={12}>
                    <p>
                        Branching rules allow for client directed routing, enabling feature and local branches to be
                        called as part of the microservice graph. You must be logged in and belong to the "Developer"
                        group to have branching rules applied. The routing is applied by the
                        &nbsp;<a href={"https://github.com/OctopusSamples/content-team-apps/tree/main/go/reverse-proxy"}>reverse proxy</a>.
                    </p>
                    <p>
                        An example path is <code>/api/audits:GET</code>, where the first half is the path, then a colon,
                        then the HTTP method. Ant wildcards are accepted e.g. <code>/api/audits/**:GET</code>.
                    </p>
                    <p>
                        An example destination is <code>url[https://49c5-94-177-118-180.ngrok.io]</code>, which redirects
                        requests matching the path to the specified URL. Tools like &nbsp;<a href={"https://ngrok.com/"}>ngrok</a>&nbsp;
                        are useful for routing public traffic to your local developer workstation.
                    </p>
                    <p>
                        Destinations prefixed with an underscore, like <code>_url[https://49c5-94-177-118-180.ngrok.io]</code>, are
                        considered to be disabled. This allows you to save the details of a rule but not have it applied.
                    </p>
                </Grid>
                <Grid container={true} className={classes.cell} xs={4}>
                    <FormLabel className={classes.label}>Branching rules enabled</FormLabel>
                </Grid>
                <Grid container={true} className={classes.cell} item xs={8}>
                    <Checkbox
                        checked={rulesEnabled}
                        onChange={event => {
                            setRulesEnabled(event.target.checked);
                            localStorage.setItem("branchingEnabled", event.target.checked.toString());
                        }}/>
                </Grid>
                <Grid container={true} className={classes.cell} sm={3} xs={12}>
                    <Button variant={"outlined"} onClick={_ => addRule()}>Add Rule</Button>
                </Grid>
                <Grid container={true} className={classes.cell} sm={3} xs={12}>
                    <Button variant={"outlined"} onClick={_ => deleteRule()}>Delete Rule</Button>
                </Grid>

            </Grid>
        </>
    );

    function onEdit(params: GridCellEditCommitParams) {
        for (let i = 0; i < rules.length; ++i) {
            if (rules[i].id === params.id) {
                if (params.field === "path") {
                    rules[i].path = params.value?.toString() || "";
                } else if (params.field === "destination") {
                    rules[i].destination = params.value?.toString() || "";
                }
            }
        }
        localStorage.setItem("branching", JSON.stringify(rules));
        setRules([...rules])
    }

    function addRule() {
        const maxId = rules.reduce((previousValue, currentValue) => previousValue < currentValue.id ? currentValue.id : previousValue, 0);

        const newRules = [...rules, {id: maxId + 1, path: "", destination: ""}];
        localStorage.setItem("branching", JSON.stringify(newRules));
        setRules([...newRules])
    }

    function deleteRule() {
        const newRules = rules.filter(r => !selectedRows.some(s => s === r.id));
        localStorage.setItem("branching", JSON.stringify(newRules));
        setRules([...newRules])
    }
}


export default Branching;