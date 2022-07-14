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
/**
 * The options always used when acquiring a lock file.
 */
const COMMON_OPTIONS = {stale: STALE_PERIOD};
/**
 * The options used when waiting for a lock file.
 */
const WAIT_OPTIONS = {wait: WAIT_TIME, pollPeriod: POLL_PERIOD, ...COMMON_OPTIONS};

/**
 * Attempt to acquire a lock file, after optional waiting for other processes, and then call the callback.
 * @param lockFilePath The lock file path.
 * @param wait true if we should wait for an existing lock file to be cleared.
 * @param cb The callback to call upon success.
 */
export default function lockFileAndContinue(lockFilePath: string, wait: boolean, cb: () => unknown) {
    /*
     If we are waiting for the lock, define the wait and pollPeriod options.
     We always set the stale option to deal with old locks.
     */
    const options = wait ? WAIT_OPTIONS : COMMON_OPTIONS;

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