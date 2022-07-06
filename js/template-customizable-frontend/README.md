This project builds a simple, customizable, no-code interface to the [Template Generator](/octopus-template-generator).

It is based on [Adaptive Cards](https://adaptivecards.io/), which provide the ability to define custom user interfaces
using a simple JSON syntax.

The `index.json` file is loaded first. This file can either define a template to be generated, or be used as an
index to load other cards (see the `downloadTemplate` verb below).

# Action.Execute verbs

To facilitate the generation of templates, this app recognises a number of `Action.Execute` verbs:

* `openCard` - Loads a new JSON file for display on the page.
* `downloadTemplate` - Converts the field values into a API request payload that is sent to the template generator configured in the `config.json` `templateGeneratorHost` setting.

# Field names

Field ids sent to the template generator must have a specific format to identify the options, answers, and arguments
recognised by the YeoMan templates:

* `generator` - The NPM package defining the Yeoman generator that the template generator will use. 
  Note the value for this field is the complete NPM package name, including the "generator" prefix. For example,
  the value of the field would be `@octopus-content-team/generator-octopus-project`. This is different to the
  value passed to the `yo` command, which would be `@octopus-content-team/octopus-project`. Subgenerators are
  defined after a colon, like `@octopus-content-team/generator-octopus-project:subgenerator`. Specific package
  versions are defined like `@octopus-content-team/generator-octopus-project@0.1.3`. Specific versions with a 
  subgenerator are define like `@octopus-content-team/generator-octopus-project@0.1.3:subgenerator`.
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