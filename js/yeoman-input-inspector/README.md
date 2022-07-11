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

## Debugging

By default, this project will attempt to run the generators up to the point where they define their
prompts. In most cases this allows `yeoman-inspector` to capture all the inputs without writing any
files.

This can lead to issues where some questions are missed though, as not all generators respect
the Yeoman lifecycle. For example, some generators compose in other generators during the writing
phase, which then introduces more prompts. These prompts will be missed by `yeoman-inspector` with
its default settings.

To have the `yeoman-inspector` tool run through the complete generator lifecycle (i.e. actually create
files), set the `ALLOW_FULL_INSTALL` environment variable to `true`.