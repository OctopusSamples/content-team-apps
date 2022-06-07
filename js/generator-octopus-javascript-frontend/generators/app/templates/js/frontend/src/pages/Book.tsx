import {FC, ReactElement, useContext, useEffect, useState} from "react";
import {Helmet} from "react-helmet";
import {AppContext} from "../App";
import {Product} from "../model/Product";
import {useParams} from "react-router-dom";
import {getJsonApi} from "../utils/network";
import {Grid} from "@mui/material";
import {styles} from "../utils/styles";
import {convertToObject} from "../utils/parsing";

const Book: FC<{}> = (): ReactElement => {

    const classes = styles();

    const context = useContext(AppContext);

    const { bookId } = useParams();

    context.setAllBookId(bookId || "");

    const [book, setBook] = useState<Product | null>(null);

    useEffect(() => {
        getJsonApi<Product>(context.settings.productEndpoint + "/" + bookId, context.partition)
            .then(data => setBook(convertToObject(data)))
    }, [bookId, setBook, context.settings.productEndpoint, context.partition]);

    return (
        <>
            <Helmet>
                <title>
                    {context.settings.title}
                </title>
            </Helmet>
            {!book && <div>Loading...</div>}
            {book && <Grid container={true} className={classes.content}>
                <Grid item md={4} sm={12}>
                    <img id="coverimage"
                         className={classes.image}
                         src={book.data.attributes.image || "https://via.placeholder.com/300x400"}
                         alt={book.data.attributes.name || ""}/>
                </Grid>
                <Grid  item md={8} sm={12}>
                    <h1>{book.data.attributes.name}</h1>
                    <p>{book.data.attributes.description}</p>
                    <h2>Downloads</h2>
                    <ul>
                        {book.data.attributes.pdf && <li><a href={book.data.attributes.pdf}>PDF</a></li>}
                        {book.data.attributes.epub && <li><a href={book.data.attributes.epub}>EPUB</a></li>}
                        {book.data.attributes.web && <li><a href={book.data.attributes.web}>Link</a></li>}
                    </ul>
                </Grid>
            </Grid>}

        </>
    );
}

export default Book;