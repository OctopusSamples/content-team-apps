const lockFile = require('lockfile');

/**
 * A reasonable amount of time to wait for any generator to complete.
 */
const WAIT_TIME = 60000;
/**
 * We don't need to be too aggressive with polling.
 */
const POLL_PERIOD = 1000;
/**
 * Stale locks can occur if you kill the server with after a lock file is created.
 * Assume locks are stale after some multiple of the WAIT_TIME.
 */
const STALE_PERIOD = WAIT_TIME * 3;

export default function lockFileAndContinue(lockFilePath: string, wait: boolean, cb: () => unknown) {
    /*
     If we are waiting for the lock, define the wait and pollPeriod options.
     We always set the stale option to deal with old locks.
     */
    const options = {...(wait ? {wait: WAIT_TIME, pollPeriod: POLL_PERIOD} : {}), stale: STALE_PERIOD};

    return new Promise((resolve, reject) => {
        lockFile.lock(lockFilePath, options, (err: never) => {
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