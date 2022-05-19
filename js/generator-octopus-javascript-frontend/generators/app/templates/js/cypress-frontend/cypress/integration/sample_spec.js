describe('Random Quotes', () => {
    it('Can refresh', () => {
        cy.visit('/')
        cy.get('#refreshQuote').click()
        cy.wait(1000)
        cy.get('#quoteText')
            .should('not.be.empty')
    })
})