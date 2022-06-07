import {FC, ReactElement, useContext, useEffect, useState} from "react";
import {Helmet} from "react-helmet";
import {AppContext} from "../App";
import {Errors, Product} from "../model/Product";
import {useNavigate, useParams} from "react-router-dom";
import {styles} from "../utils/styles";
import {deleteJsonApi, getJsonApi, isBranchingEnabled} from "../utils/network";
import {Button, FormLabel, Grid, TextField} from "@mui/material";
import {convertToObject} from "../utils/parsing";

const DeleteBook: FC<{}> = (): ReactElement => {

    const {bookId} = useParams();
    const history = useNavigate();
    const context = useContext(AppContext);
    const classes = styles();
    const [disabled, setDisabled] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [book, setBook] = useState<Product | null>(null);

    context.setAllBookId("");

    useEffect(() => {
        getJsonApi<Product | Errors>(context.settings.productEndpoint + "/" + bookId, context.partition)
            .then(data => {
                const fixedData = convertToObject(data);

                if ("data" in fixedData) {
                    const product = fixedData as Product;
                    setBook(product);

                    if (product?.data?.attributes?.dataPartition !== context.partition) {
                        setError("This book belongs to the "
                            + product?.data?.attributes?.dataPartition
                            + " data partition, and cannot be deleted.");
                    } else {
                        setDisabled(false);
                    }
                }
                if ("errors" in fixedData) {
                    const error = fixedData as Errors;
                    setError(error.errors[0].title || "Failed to load book."
                        + (isBranchingEnabled() ? " Branching rules are enabled - double check they are valid, or disable them." : ""));
                }
            })
            .catch(() => {
                setError("There was an error retrieving the resource.");
                setBook(null);
                setDisabled(true);
            });
    }, [setBook, setDisabled, context.partition, context.settings.productEndpoint, bookId]);

    return (
        <>
            <Helmet>
                <title>
                    {context.settings.title}
                </title>
            </Helmet>

            <Grid container={true}>
                {book && book.data && book.data.attributes &&
                    <>
                        <Grid container={true} className={classes.cell} md={2} sm={12} xs={12}>
                            <FormLabel className={classes.label}>Name</FormLabel>
                        </Grid>
                        <Grid container={true} className={classes.cell} item md={10} sm={12} xs={12}>
                            <TextField id={"name"} disabled={true} fullWidth={true} variant={"outlined"}
                                       value={book.data.attributes.name}/>
                        </Grid>
                        <Grid container={true} className={classes.cell} item md={2} sm={12} xs={12}>
                            <FormLabel className={classes.label}>Image</FormLabel>
                        </Grid>
                        <Grid container={true} className={classes.cell} item md={10} sm={12} xs={12}>
                            <TextField id={"image"} disabled={true} fullWidth={true} variant={"outlined"}
                                       value={book.data.attributes.image}/>
                        </Grid>
                        <Grid container={true} className={classes.cell} item md={2} sm={12} xs={12}>
                            <FormLabel className={classes.label}>EPUB</FormLabel>
                        </Grid>
                        <Grid container={true} className={classes.cell} item md={10} sm={12} xs={12}>
                            <TextField id={"epub"} disabled={true} fullWidth={true} variant={"outlined"}
                                       value={book.data.attributes.epub}/>
                        </Grid>
                        <Grid container={true} className={classes.cell} item md={2} sm={12} xs={12}>
                            <FormLabel className={classes.label}>PDF</FormLabel>
                        </Grid>
                        <Grid container={true} className={classes.cell} item md={10} sm={12} xs={12}>
                            <TextField id={"pdf"} disabled={true} fullWidth={true} variant={"outlined"}
                                       value={book.data.attributes.pdf}/>
                        </Grid>
                        <Grid container={true} className={classes.cell} item md={2} sm={12} xs={12}>
                            <FormLabel className={classes.label}>Description</FormLabel>
                        </Grid>
                        <Grid container={true} className={classes.cell} item md={10} sm={12} xs={12}>
                            <TextField id={"description"} disabled={true} multiline={true} rows={10} fullWidth={true}
                                       variant={"outlined"}
                                       value={book.data.attributes.description}/>
                        </Grid>
                        <Grid container={true} className={classes.cell} item md={2} sm={12} xs={12}>

                        </Grid>
                        <Grid container={true} className={classes.cell} item md={10} sm={12} xs={12}>
                            <Button variant={"outlined"} disabled={disabled} onClick={_ => deleteBook()}>Delete
                                Book</Button>
                        </Grid>
                    </>}
                <Grid container={true} className={classes.cell} item md={2} sm={12} xs={12}>

                </Grid>
                <Grid container={true} className={classes.cell} item md={10} sm={12} xs={12}>
                    {error && <span>{error}</span>}
                </Grid>
            </Grid>
        </>
    );

    function deleteBook() {
        if (book) {
            setDisabled(true);
            deleteJsonApi(context.settings.productEndpoint + "/" + bookId, context.partition)
                .then(_ => history('/index.html'))
                .catch(reason => {
                    setDisabled(false);
                    setError("An error occurred while deleting the book: " + reason);
                });
        }
    }
}


export default DeleteBook;