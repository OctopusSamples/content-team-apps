const Generator = require('yeoman-generator');

module.exports = class extends Generator {
    async prompting() {
        this.answers = await this.prompt([
            {
                type: "input",
                name: "book_title",
                message: "The book title",
                default: "A dube and his pants"
            },
            {
                type: "input",
                name: "book_description",
                message: "The book description",
                default: "A story about a dude who loves pants"
            },
            {
                type: "input",
                name: "book_author",
                message: "The book author",
                default: "Dude McPants"
            }
        ]);
    }

    writing() {
        const options = {
            book_author: this.answers["book_author"],
            book_title: this.answers["book_title"],
            book_description: this.answers["book_description"]
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
