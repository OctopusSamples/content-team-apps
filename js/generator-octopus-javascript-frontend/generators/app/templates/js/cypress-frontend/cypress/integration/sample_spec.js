describe('Octopub', () => {
    it('Can open book', () => {
        cy.visit('/')
        // It can take quite some time for an Aurora serverless database to wake up, so set a long timeout to get the book
        cy.get('#book1', { timeout: 60000 }).click()
        cy.wait(1000)
        cy.get('#coverimage')
            .should('be.visible')
    })
})