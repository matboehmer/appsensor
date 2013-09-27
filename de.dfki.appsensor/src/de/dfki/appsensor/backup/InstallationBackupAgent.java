package de.dfki.appsensor.backup;

import de.dfki.appsensor.logging.DeviceObserver;
import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;

/**
* @author Matthias Boehmer, matthias.boehmer@dfki.de
*/
public class InstallationBackupAgent extends BackupAgentHelper {

    // A key to uniquely identify the set of backup data
    static final String FILES_BACKUP_KEY = "installation_id_file";

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        FileBackupHelper filesHelper = new FileBackupHelper(this, DeviceObserver.INSTALLATION);
        addHelper(FILES_BACKUP_KEY, filesHelper);
    }
}
