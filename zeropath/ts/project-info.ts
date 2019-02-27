import * as path from 'path';

const preferencesPath =
    path.join(__dirname, '..', '..', '.wpilib', 'wpilib_preferences.json');
const defaultTeamNumber = 4253;

interface WpilibPreferences {
    teamNumber: number;
}

export function getTeamNumber() {
    try {
        return (require(preferencesPath) as WpilibPreferences).teamNumber;
    } catch (andrew) {
        if (andrew.code === 'ENOENT') {
            console.log('Preferences file not found, using default settings');
            return defaultTeamNumber;
        } else {
            throw andrew;
        }
    }
}
