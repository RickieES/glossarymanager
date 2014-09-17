/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.localizethat.glossarymanager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import net.localizethat.glossarymanager.gui.MainWindow;
import net.localizethat.glossarymanager.system.AppSettings;
import net.localizethat.glossarymanager.system.DBChecker;

/**
 * Main entry for GlossaryManager application
 * @author rpalomares
 */
public class GlossaryManager {
    
    /**
     * String containing the version number in the format Major.Minor.devversion
     * Major and minor values are parsed and compared as integers, whereas the
     * devversion is parsed and compared as an String like "a1" for alpha
     * versions, "b2" for beta versions, or "r5" for release maintenance version
     * 
     * For instance, when comparing "2.0.b4" to "12.0.r3", the comparison would be:
     * - Major: 2 < 12
     * - Minor: 0 == 0
     * - Bugrelease: "b4" < "r3"
     */
    public static final String version = "0.0.a2";

    /**
     * Reference to the application settings
     */
    public static AppSettings appSettings;

    /**
     * Reference to the application main window
     */
    public static MainWindow mainWindow;

    /**
     * Reference to global Entity Manager Factory
     */
    public static EntityManagerFactory emf;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Process parameters
        processParameters(args);

        // Process preferences
        appSettings = new AppSettings();

        // Check database existence and create if needed
        DBChecker dbChecker = new DBChecker(appSettings.getString(AppSettings.PREF_DB_PATH),
                                            appSettings.getString(AppSettings.PREF_DB_LOGIN),
                                            appSettings.getString(AppSettings.PREF_DB_PASSWD));
        if (!(dbChecker.checkAndCreateDBDir() && dbChecker.checkAndCreateDB())) {
            Logger.getLogger(GlossaryManager.class.getName()).log(Level.SEVERE,
                "Can't properly access the database, check error dump log");
            System.exit(1);
        }

        // Set up persistence unit
        Map<String, String> connProps = new HashMap<>(4);
        connProps.put("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
        connProps.put("javax.persistence.jdbc.url", "jdbc:derby:" + DBChecker.DB_NAME);
        connProps.put("javax.persistence.jdbc.user", appSettings.getString(AppSettings.PREF_DB_LOGIN));
        connProps.put("javax.persistence.jdbc.password", appSettings.getString(AppSettings.PREF_DB_PASSWD));
        emf = Persistence.createEntityManagerFactory("GlossaryManagerPU", connProps);

        // Create GUI
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        mainWindow = new MainWindow();
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainWindow.setVisible(true);
            }
        });

        // Clean up
        // cleanUpResources();
        // System.out.println("Exitting the application");

    }

    private static void processParameters(String[] args) {
        // Nothing to do at the moment
    }

    public static void cleanUpResources() {
        emf.close();
    }
}
