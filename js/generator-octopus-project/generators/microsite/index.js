const Generator = require('yeoman-generator');
const {v4: uuidv4} = require('uuid');

module.exports = class extends Generator {
    async prompting() {
        this.answers = await this.prompt([
            {
                type: "input",
                name: "microsite_name",
                message: "The Microsite Name",
                default: "MyMicrosite"
            },
            {
                type: "input",
                name: "octopus_server",
                message: "The hostname of your Octopus instance.",
                default: "${{ secrets.OCTOPUS_SERVER }}"
            },
            {
                type: "password",
                name: "octopus_apikey",
                message: "The Octopus API key.",
                default: "${{ secrets.OCTOPUS_APIKEY }}"
            },
            {
                type: "input",
                name: "octopus_space",
                message: "The Octopus Space",
                default: "${{ secrets.OCTOPUS_SPACE }}"
            }
        ]);
    }

    writing() {
        const options = {
            microsite_name: this.answers["microsite_name"],
            octopus_server: this.answers["octopus_server"],
            octopus_apikey: this.answers["octopus_apikey"],
            octopus_space: this.answers["octopus_space"]
        };

        this.fs.copyTpl(
            this.templatePath('**/*'),
            this.destinationPath(''),
            options,
            null,
            { globOptions: { dot: true } }
        );
    }
};
