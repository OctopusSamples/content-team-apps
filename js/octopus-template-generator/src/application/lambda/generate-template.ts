import {APIGatewayProxyEvent, APIGatewayProxyResult} from "aws-lambda";
import {deserialise} from 'kitsu-core'
import {TemplateGenerator} from "../../domain/hanlders/templateGenerator";
import * as fs from "fs";

const PATH = "/api/generatetemplate";

export async function lambdaHandler(event: APIGatewayProxyEvent): Promise<APIGatewayProxyResult> {

    return await generateZip(event) ??
        {
            statusCode: 404,
            body: "Resource not found"
        }
}

async function generateZip(event: APIGatewayProxyEvent): Promise<APIGatewayProxyResult | null> {
    if (event.path !== PATH || event.httpMethod.toLowerCase() !== "post") {
        return null;
    }

    const requestBody = event.isBase64Encoded
        ? Buffer.from(event.body ?? "", "base64").toString("utf8")
        : event.body ?? "{}";


    // Convert the JSONAPI body to a plain object
    const body = deserialise(JSON.parse(requestBody));

    const templateZip = await new TemplateGenerator().generateTemplateSync(
        body.data.generator,
        body.data.options,
        body.data.answers,
        body.data.args);

    const data = fs.readFileSync(templateZip).toString("base64");

    return {
        statusCode: 200,
        // eslint-disable-next-line @typescript-eslint/naming-convention
        headers: {"Content-Type": "application/zip"},
        body: data,
        isBase64Encoded: true
    }
}