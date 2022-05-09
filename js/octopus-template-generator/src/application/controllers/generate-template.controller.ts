import {post, requestBody, Response, RestBindings} from '@loopback/rest';
import {TemplateGenerator} from '../../domain/hanlders/templateGenerator';
import {inject} from '@loopback/core';
import {deserialise} from "kitsu-core";

export class GenerateTemplateController {
    private templateGenerator: TemplateGenerator;

    constructor() {
        this.templateGenerator = new TemplateGenerator();
    }

    @post('/api/generatetemplate')
    async generateTemplate(
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
