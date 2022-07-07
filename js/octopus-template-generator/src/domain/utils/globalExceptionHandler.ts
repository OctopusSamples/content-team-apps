import process from "node:process";

export default function handleExceptions() {
    /*
        Missing files and other errors will kill the node process by default. This is
        undesirable for a long-running web server, so we catch the exception here.
        https://nodejs.org/api/process.html#event-uncaughtexception
     */
    process.on('uncaughtException', (err, origin) => {
        console.log(err);
    });
}