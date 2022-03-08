import {Entity, model, property} from '@loopback/repository';

@model()
export class GenerateTemplate extends Entity {
  @property({id: true}) id: string;

  @property() type: string;

  @property() attributes: {
    template: string,
    options: { [key: string]: string; }
  }

  constructor(data?: Partial<GenerateTemplate>) {
    super(data);
  }
}
