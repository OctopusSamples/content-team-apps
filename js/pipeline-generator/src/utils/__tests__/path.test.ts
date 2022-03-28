import {getHashField} from "../path";

test('single hash is returned', () => {
    window.location.hash = "#hi=test";
    expect(getHashField("hi")).toBe("test")
});

test('multiple hash is returned', () => {
    window.location.hash = "#hi=test&blah=test2";
    expect(getHashField("hi")).toBe("test")
    expect(getHashField("blah")).toBe("test2")
    expect(getHashField("none")).toBe(undefined)
});