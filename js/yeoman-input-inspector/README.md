This project provides a CLI based tool that captures the arguments, options, and questions
defined by a Yeoman generator, and uses them to create a sample adative card that can be
displayed by the (template-customizable-frontend)[/js/template-customizable-frontend]
project.

## Installation

```
npm install -g @octopus-content-team/yeoman-input-inspector
```

## Usage

Install the Yeoman generator (JHipster is used as an example here):

```
npm install generator-jhipster
```

Inspect the generator:

```
yeoman-inspector jhipster
```

The output will then display the arguments, options, and questions defined by the generator,
as well as a sample adaptive card.