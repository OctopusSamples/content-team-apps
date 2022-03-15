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

    // Convert the JSONAPI body to a plain object
    const body = deserialise(event.body);

    const templateZip = await new TemplateGenerator().generateTemplate(
        body.data.generator,
        body.data.options);

    const data = fs.readFileSync(templateZip).toString('base64');

    return {
        statusCode: 200,
        // eslint-disable-next-line @typescript-eslint/naming-convention
        headers: {"Content-Type": "application/zip"},
        body: data,
        isBase64Encoded: true
    }
}