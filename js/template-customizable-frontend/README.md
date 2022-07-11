This project builds a simple, customizable, no-code interface to the [Template Generator](/js/octopus-template-generator).

It is based on [Adaptive Cards](https://adaptivecards.io/), which provide the ability to define custom user interfaces
using a simple JSON syntax.

The `index.json` file is loaded first. This file can either define a template to be generated, or be used as an
index to load other cards (see the `downloadTemplate` verb below).

# Action.Execute verbs

To facilitate the generation of templates, this app recognises a number of `Action.Execute` verbs:

* `openCard` - Loads a new JSON file for display on the page.
* `downloadTemplate` - Converts the field values into a API request payload that is sent to the template generator configured in the `config.json` `templateGeneratorHost` setting.

# Field names

Field IDs must have a specific format to identify the options, answers, and arguments recognised by the 
Yeoman generator:

* `generator` - The NPM package defining the Yeoman generator that the template generator will use.
* `answer.string.[answername]` - Defines a string value for the answer called `answername`. Replace `answername` with
  the name of the prompt defined by the Yeoman template.
* `answer.boolean.[answername]` - Defines a boolean value for the answer called `answername`. Replace `answername` with
  the name of the prompt defined by the Yeoman template.
* `answer.number.[answername]` - Defines a number value for the answer called `answername`. Replace `answername` with
  the name of the prompt defined by the Yeoman template.
* `answer.char.[answername]` - Defines a character value for the answer called `answername`. Replace `answername` with
  the name of the prompt defined by the Yeoman template.
* `answer.list.[answername]` - Defines a list value for the answer called `answername`. Replace `answername` with
  the name of the prompt defined by the Yeoman template.
* `option.[optionname]` - Defines a list value for the option called `optionname`. Replace `optionname` with
  the name of the option defined by the Yeoman template.

# Generator names

The generator name is the full NPM module name, including the "generator" prefix. For example, the generator name
may be something like `@octopus-content-team/generator-octopus-project`. 

Note this is different to the value passed to the `yo` command, which would be `@octopus-content-team/octopus-project`,
where the `generator` prefix is dropped from the package name.

Subgenerators are defined after a colon, like `@octopus-content-team/generator-octopus-project:subgenerator`. 

Specific package versions are defined like `@octopus-content-team/generator-octopus-project@0.1.3`. 

Specific versions with a subgenerator are define like `@octopus-content-team/generator-octopus-project@0.1.3:subgenerator`.

# Generating cards

The [Yeoman Inspector](/js/yeoman-input-inspector) tool provides a way to extract the options, arguments, and questions
defined by a generator, as well as generating a sample Adaptive Card template.

Install the tool with:

```
npm install -g @octopus-content-team/yeoman-input-inspector
```

Install the generator to inspect with the command:

```
npm install -g @octopus-content-team/generator-octopus-project
```

Then inspect a generator with the command:

```
yeoman-inspector @octopus-content-team/octopus-project:apprunner
```

The Adaptive Card sample is displayed under the `ADAPTIVE CARD EXAMPLE` heading in the output.

# Live instances

This frontend is available [here](https://oc.to/content-team-platform-engineering).