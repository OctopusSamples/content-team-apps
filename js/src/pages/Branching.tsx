import {FC, ReactElement, useContext, useState} from "react";
import {Helmet} from "react-helmet";
import {AppContext} from "../App";
import {DataGrid, GridCellEditCommitParams, GridRowId} from "@material-ui/data-grid";
import {styles} from "../utils/styles";
import {Button, Checkbox, FormLabel, Grid} from "@material-ui/core";

export interface RedirectRule {
    id: number,
    path: string,
    destination: string
}

const Branching: FC<{}> = (): ReactElement => {

    const context = useContext(AppContext);
    context.setCopyText("");

    const classes = styles();

    const [rules, setRules] = useState<RedirectRule[]>(JSON.parse(localStorage.getItem("branching") || "[]"));
    const [rulesEnabled, setRulesEnabled] = useState<boolean>((localStorage.getItem("branchingEnabled") || "").toLowerCase() !== "false");

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
                        style={{height: "60vh"}}
                        rows={rules}
                        columns={columns}
                        pageSize={10}
                        rowsPerPageOptions={[10]}
                        onSelectionModelChange={(selection) => selectedRows = selection}
                        onCellEditCommit={onEdit}
                    />
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