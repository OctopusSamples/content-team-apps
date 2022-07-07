const lockFile = require('lockfile');

export default function lockFileAndContinue(lockFilePath: string, cb: () => unknown) {
    return new Promise((resolve, reject) => {
        lockFile.lock(lockFilePath, (err: never) => {
            if (err) return reject(err);
            return resolve(cb());
        })
    })
    .finally(() => lockFile.unlock(lockFilePath, (err: never) => {
        if (err) {
            console.error('TemplateGenerator-GenerateTemplate-UnlockFail: Failed to unlock the file: ' + err)
        }
    }));
}