describe('Octopub', () => {
    it('Can open book', () => {
        cy.visit('/')
        cy.get('#book1').click()
        cy.wait(1000)
        cy.get('#coverimage')
            .should('be.visible')
    })
})