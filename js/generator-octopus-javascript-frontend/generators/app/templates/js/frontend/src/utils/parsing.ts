/**
 I've seen an issue with services exposed by EKS have a content type of text/plain instead of application/json.
 Since we only ever expect a JSON response for most calls, go ahead and parse the text as an object, because we want to
 robust: "be conservative in what you do, be liberal in what you accept from others".
 */
export function convertToObject<T>(input: T | string): T {
    if (typeof input === 'string' || input instanceof String) {
        return JSON.parse(String(input))
    }

    return input;
}