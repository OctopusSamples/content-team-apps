import {appendUtms} from "../network";
import {UtmParams} from "../tracking";

test('UTMs are added to paths with existing query param', () => {
    const path = "/test?somequery=test"
    const utms: UtmParams = {
        source: "utmsource",
        medium: "utmmedium",
        campaign: "utmcampaign",
        term: "utmterm",
        content: "utmcontent"
    }
    expect(appendUtms(path, utms)).toBe("/test?somequery=test&utm_source=utmsource&utm_medium=utmmedium&utm_campaign=utmcampaign&utm_term=utmterm&utm_content=utmcontent")
});

test('UTMs are added to paths', () => {
    const path = "/test"
    const utms: UtmParams = {
        source: "utmsource",
        medium: "utmmedium",
        campaign: "utmcampaign",
        term: "utmterm",
        content: "utmcontent"
    }
    expect(appendUtms(path, utms)).toBe("/test?utm_source=utmsource&utm_medium=utmmedium&utm_campaign=utmcampaign&utm_term=utmterm&utm_content=utmcontent")
});

test('UTMs are added to URL', () => {
    const path = "http://localhost/test"
    const utms: UtmParams = {
        source: "utmsource",
        medium: "utmmedium",
        campaign: "utmcampaign",
        term: "utmterm",
        content: "utmcontent"
    }
    expect(appendUtms(path, utms)).toBe("http://localhost/test?utm_source=utmsource&utm_medium=utmmedium&utm_campaign=utmcampaign&utm_term=utmterm&utm_content=utmcontent")
});

test('Mixed UTMs are added to URL', () => {
    const path = "http://localhost/test"
    const utms: UtmParams = {
        source: "",
        medium: "",
        campaign: "utmcampaign",
        term: "utmterm",
        content: "utmcontent"
    }
    expect(appendUtms(path, utms)).toBe("http://localhost/test?utm_campaign=utmcampaign&utm_term=utmterm&utm_content=utmcontent")
});