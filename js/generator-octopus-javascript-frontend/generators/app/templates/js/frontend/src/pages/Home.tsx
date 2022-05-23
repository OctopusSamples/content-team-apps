import React, {FC, ReactElement, useContext, useEffect, useState} from "react";
import {Helmet} from "react-helmet";
import {AppContext} from "../App";
import {getJsonApi, isBranchingEnabled} from "../utils/network";
import {Grid} from "@mui/material";
import {Products} from "../model/Product";
import {useNavigate} from "react-router-dom";
import {styles} from "../utils/styles";
import {convertToObject} from "../utils/parsing";

const Home: FC = (): ReactElement => {
    const context = useContext(AppContext);
    const classes = styles();
    const history = useNavigate();

    const [books, setBooks] = useState<Products | null>(null);
    const [error, setError] = useState<string | null>(null);

    context.setAllBookId("");

    useEffect(() => {
        getJsonApi<Products>(context.settings.productEndpoint, context.partition)
            .then(data => setBooks(convertToObject(data)))
            .catch(() => setError("Failed to retrieve the list of books."
                + (isBranchingEnabled() ? " Branching rules are enabled - double check they are valid, or disable them." : "")));
    }, [context.settings.productEndpoint, setBooks, setError, context.partition]);

    return (
        <>
            <Helmet>
                <title>
                    {context.settings.title}
                </title>
            </Helmet>
            <Grid
                container={true}
                className={classes.root}
            >
                {!books && !error && <div>Loading...</div>}
                {!books && error && <div>{error}</div>}
                {books && books.data.map(b =>
                    <Grid item
                          key={b.id}
                          md={4} sm={6} xs={12}
                          className={classes.bookshelfImage}
                          container={true}
                          onClick={() => {
                              history('/book/' + b.id);
                          }}>
                        <img id={"book" + b.id}
                             className={classes.image}
                             src={b.attributes.image || "https://via.placeholder.com/300x400"}
                             alt={b.attributes.name || "Unknown"}/>
                        <h3 className={classes.bookshelfTitle}>{b.attributes.name}</h3>
                    </Grid>)}
            </Grid>
        </>
    );
};

export default Home;