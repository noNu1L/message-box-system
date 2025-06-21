import { homeDir, join } from '@tauri-apps/api/path';
import {
    exists,
    mkdir,
    writeFile,
    readFile,
    readTextFile,
    writeTextFile,
    remove,
} from '@tauri-apps/plugin-fs';

let baseDir = null;
const DATA_DIR = 'data';
const SETTINGS_FILE = 'setting.config';

/**
 * Initializes the storage by creating the necessary directories.
 */
async function initStorage() {
    if (baseDir) {
        return;
    }

    try {
        const home = await homeDir();
        baseDir = await join(home, '.msg_box_client');

        // Create base directory and data directory.
        // The `recursive: true` option makes it behave like `mkdir -p`,
        // which doesn't fail if the directory already exists.
        await mkdir(baseDir, { recursive: true });
        const dataPath = await join(baseDir, DATA_DIR);
        await mkdir(dataPath, { recursive: true });

    } catch (error) {
        console.error("Failed to initialize storage:", error);
        // Fallback to a local directory if homeDir is not available
        try {
            baseDir = '.msg_box_client';
            await mkdir(baseDir, { recursive: true });
            const dataPath = await join(baseDir, DATA_DIR);
            await mkdir(dataPath, { recursive: true });
        } catch (fallbackError) {
            console.error("Failed to initialize fallback storage:", fallbackError);
        }
    }
}

/**
 * Gets the base directory, initializing storage if needed.
 * @returns {Promise<string>} The base directory path.
 */
async function getBaseDir() {
    if (!baseDir) {
        await initStorage();
    }
    return baseDir;
}

/**
 * Saves settings to the config file.
 * @param {object} settings - The settings object to save.
 */
export async function saveSettings(settings) {
    try {
        const dir = await getBaseDir();
        const filePath = await join(dir, SETTINGS_FILE);
        await writeTextFile(filePath, JSON.stringify(settings, null, 2));
    } catch (error) {
        console.error("Failed to save settings:", error);
    }
}

/**
 * Loads settings from the config file.
 * @returns {Promise<object|null>} The settings object or null if not found or on error.
 */
export async function loadSettings() {
    try {
        const dir = await getBaseDir();
        const filePath = await join(dir, SETTINGS_FILE);
        if (await exists(filePath)) {
            const content = await readTextFile(filePath);
            return JSON.parse(content);
        }
        return null;
    } catch (error) {
        console.error("Failed to load settings:", error);
        return null;
    }
}

/**
 * Loads data from today's date-stamped file.
 * @returns {Promise<Array|null>} The data array or null if not found or on error.
 */
export async function loadTodayData() {
    try {
        const dir = await getBaseDir();
        const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD
        const fileName = `${today}.json`;
        const filePath = await join(dir, DATA_DIR, fileName);

        if (await exists(filePath)) {
            const content = await readTextFile(filePath);
            return JSON.parse(content);
        }
        return []; // Return empty array if no file for today
    } catch (error) {
        console.error("Failed to load today's data:", error);
        return null;
    }
}

/**
 * Clears all stored data by removing and recreating the data directory.
 */
export async function clearAllData() {
    try {
        const dir = await getBaseDir();
        const dataPath = await join(dir, DATA_DIR);
        if (await exists(dataPath)) {
            await remove(dataPath, { recursive: true });
        }
        await mkdir(dataPath, { recursive: true });
    } catch (error) {
        console.error("Failed to clear all data:", error);
    }
}

/**
 * Saves data to a date-stamped file.
 * @param {object|Array} data - The data to save.
 */
export async function saveData(data) {
    try {
        const dir = await getBaseDir();
        const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD
        const fileName = `${today}.json`;
        const filePath = await join(dir, DATA_DIR, fileName);

        let existingData = [];
        if (await exists(filePath)) {
            const content = await readTextFile(filePath);
            existingData = JSON.parse(content);
        }

        const newData = Array.isArray(existingData) ? existingData : [existingData];
        newData.push(data);

        await writeTextFile(filePath, JSON.stringify(newData, null, 2));

    } catch (error) {
        console.error("Failed to save data:", error);
    }
}

// Initialize storage on module load
initStorage(); 