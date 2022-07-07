const AdmZip = require("adm-zip");

export default function createZipFile(tempDir: string, zipPath: string) {
    console.log("Zipping up the template");
    const zip = new AdmZip();
    zip.addLocalFolder(tempDir);
    zip.writeZip(zipPath);
    console.log("Zip file generated");
}