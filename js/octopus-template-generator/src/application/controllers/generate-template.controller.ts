import {get, param, post, requestBody, Response, RestBindings} from '@loopback/rest';
import {TemplateGenerator} from '../../domain/hanlders/templateGenerator';
import {inject} from '@loopback/core';
import {deserialise, serialise} from "kitsu-core";

export class GenerateTemplateController {
    private templateGenerator: TemplateGenerator;

    constructor() {
        this.templateGenerator = new TemplateGenerator();
    }

    @post('/api/template')
    async generateTemplate(
        // eslint-disable-next-line @typescript-eslint/naming-convention
        @requestBody({content: {'application/vnd.api+json': {}}}) body: object,
        @inject(RestBindings.Http.RESPONSE) response: Response): Promise<Response> {

        // The incoming string is a JSONAPI object
        const parsedBody = deserialise(body);

        const hash = await this.templateGenerator.generateTemplate(
            parsedBody.data.generator,
            parsedBody.data.options);

        const returnValue =  serialise("template", {id: hash});
        response.send(returnValue);
        return response;
    }

    @get('/api/template/{id}')
    async returnTemplate(
        @param.path.string('id') id: string,
        @inject(RestBindings.Http.RESPONSE) response: Response): Promise<Response> {

        const templateZip = await this.templateGenerator.getTemplate(id);

        if (templateZip) {
            // If the zip file exists, download it
            response.setHeader("Content-Type", "application/zip");
            response.download(templateZip, "template.zip", (err: Error) => {
                if (err) {
                    console.log(err);
                }
            });

        } else {
            // Otherwise the file doesn't exist.
            response.statusCode = 404;
        }

        return response;
    }

    @post('/api/generatetemplate')
    async generateAndReturnTemplate(
        // eslint-disable-next-line @typescript-eslint/naming-convention
        @requestBody({content: {'application/vnd.api+json': {}}}) body: object,
        @inject(RestBindings.Http.RESPONSE) response: Response): Promise<Response> {

        // The incoming string is a JSONAPI object
        const parsedBody = deserialise(body);

        const templateZip = await this.templateGenerator.generateTemplate(
            parsedBody.data.generator,
            parsedBody.data.options);
        response.setHeader("Content-Type", "application/zip");
        response.download(templateZip, "template.zip", (err: Error) => {
            if (err) {
                console.log(err);
            }
        });
        return response;
    }
}
