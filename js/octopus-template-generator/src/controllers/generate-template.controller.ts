import {post, requestBody, Response, RestBindings} from '@loopback/rest';
import {TemplateGenerator} from '../hanlders/templateGenerator';
import {GenerateTemplate} from '../models/generatetemplate';
import {inject} from '@loopback/core';
import path from 'path';
import * as os from 'os';

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
      createTemplate.attributes.template,
      createTemplate.attributes.options);
    const resolvedFile = path.resolve(os.tmpdir(), templateZip);
    response.download(resolvedFile, "template.zip");
    return response;
  }
}
