import {get, param, post, requestBody, Response, RestBindings} from '@loopback/rest';
import {TemplateGenerator} from '../../domain/hanlders/templateGenerator';
import {inject} from '@loopback/core';
import {deserialise, serialise} from "kitsu-core";

export class GenerateTemplateController {
    private templateGenerator: TemplateGenerator;

    constructor() {
        this.templateGenerator = new TemplateGenerator();
    }

    /**
     * This endpoint is suitable for long-lived servers where a cached file can be assumed to be
     * available between when it is saved and when it is requested. It generates the template file and
     * returns the ID of the template to be downloaded with a call to /api/template/{id}.
     */
    @post('/api/template')
    async generateTemplate(
        // eslint-disable-next-line @typescript-eslint/naming-convention
        @requestBody({content: {'application/vnd.api+json': {}}}) body: object,
        @inject(RestBindings.Http.RESPONSE) response: Response): Promise<Response> {

        // The incoming string is a JSONAPI object
        const parsedBody = deserialise(body);

        const hash = await this.templateGenerator.generateTemplate(
            parsedBody.data.generator,
            parsedBody.data.options,
            parsedBody.data.questions);

        const returnValue = serialise("template", {id: hash});
        response.send(returnValue);
        return response;
    }

    /**
     * Generating templates can take a bit of time, so clients can make a POST request to /api/template,
     * save the ID returned by the response, and then download it at a later time with this endpoint.
     * Typically, clients will poll this endpoint waiting for a valid response.
     */
    @get('/api/template/{id}')
    async returnTemplate(
        @param.path.string('id') id: string,
        @inject(RestBindings.Http.RESPONSE) response: Response): Promise<Response> {

        const templateZip = await this.templateGenerator.getTemplate(id);

        if (templateZip) {
            /*
                If the zip file exists, download it.
                Note this does require that all instances of this service share the same temp directory, otherwise
                one instance may build the template and the second tries to find the resulting files in it's own
                temp dir that will never be populated.
             */

            response.setHeader("Content-Type", "application/zip");
            response.download(templateZip, "template.zip", (err: Error) => {
                if (err) {
                    console.log("TemplateGenerator-Download-GeneralError: " + err);
                }
            });

        } else {
            // Otherwise the file doesn't exist.
            response.statusCode = 404;
        }

        return response;
    }

    /**
     * This endpoint is suitable for lambdas where we don't have any guarantee that a cached file
     * will persist between when it is saved and when it is retrieved. It generates, saves, and download
     * the template file. It may make use of a cached version, but does not assume any caching is available.
     */
    @post('/api/generatetemplate')
    async generateAndReturnTemplate(
        // eslint-disable-next-line @typescript-eslint/naming-convention
        @requestBody({content: {'application/vnd.api+json': {}}}) body: object,
        @inject(RestBindings.Http.RESPONSE) response: Response): Promise<Response> {

        // The incoming string is a JSONAPI object
        const parsedBody = deserialise(body);

        const templateZip = await this.templateGenerator.generateTemplateSync(
            parsedBody.data.generator,
            parsedBody.data.options,
            parsedBody.data.questions);
        response.setHeader("Content-Type", "application/zip");
        response.download(templateZip, "template.zip", (err: Error) => {
            if (err) {
                console.log("TemplateGenerator-GenerateAndDownload-GeneralError: " + err);
            }
        });
        return response;
    }
}
