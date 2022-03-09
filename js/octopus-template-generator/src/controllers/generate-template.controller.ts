import {post, requestBody, Response, RestBindings} from '@loopback/rest';
import {TemplateGenerator} from '../hanlders/templateGenerator';
import {GenerateTemplate} from '../models/generate-template';
import {inject} from '@loopback/core';

export class GenerateTemplateController {
  private templateGenerator: TemplateGenerator;

  constructor() {
    this.templateGenerator = new TemplateGenerator();
  }

  @post('/api/generatetemplate')
  async generateTemplate(
    @requestBody() createTemplate: GenerateTemplate,
    @inject(RestBindings.Http.RESPONSE) response: Response): Promise<Response> {
    const templateZip = await this.templateGenerator.generateTemplate(
      createTemplate.data.attributes.template,
      createTemplate.data.attributes.options);
    response.download(templateZip, "template.zip", (err: Error) => {
      console.log(err);
    });
    return response;
  }
}
