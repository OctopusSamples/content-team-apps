export interface Products {
    data: ProductData[]
}

export interface Product {
    data: ProductData
}

export interface Error {
    title?: string
}

export interface Errors {
    errors: Error[]
}

export interface ProductData {
    id: number | null,
    type: string | null,
    attributes: {
        dataPartition: string | null,
        name: string | null,
        image: string | null,
        pdf: string | null,
        epub: string | null,
        web: string | null,
        description: string | null
    }
}