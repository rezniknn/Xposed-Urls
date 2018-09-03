package com.alexeyreznik.xposedurls;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import de.robv.android.xposed.SELinuxHelper;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.services.BaseService;

public class FileUtils {

    private static final String DATA_DIRECTORY = "/data/data";
    private static final String SCAN_FLAG_FILENAME = "DT_log_urls";
    private static final String RESULTS_FILENAME = "urls.txt";

    /**
     * Checks if the flag file exists in the private application directory
     * indicating that the application should be scanned for URLs used
     *
     * @param packageName: package name of the current application
     * @return Returns true if the application should be scanned for URLs used
     */
    static boolean shouldScan(String packageName) {
        BaseService baseService = SELinuxHelper.getAppDataFileService();
        return baseService.checkFileExists(DATA_DIRECTORY + "/" + packageName + "/" + SCAN_FLAG_FILENAME);
    }

    /**
     * Appends a string to results file if the string is not contained in the file yet
     *
     * @param packageName:   package name of the current application
     * @param stringToWrite: string to be written to the results file
     */
    static void saveStringToResultsFile(String packageName, String stringToWrite) {
        try {
            File resultsFile = new File(DATA_DIRECTORY + "/" + packageName + "/" + RESULTS_FILENAME);

            //Create results file if not present
            if (!resultsFile.exists()) {
                boolean created = resultsFile.createNewFile();
                if (!created) {
                    XposedBridge.log("Failed to create results file. Path: " + resultsFile.getAbsolutePath());
                    return;
                }
            }

            //Read file content into a String
            FileReader fr = new FileReader(resultsFile);
            BufferedReader br = new BufferedReader(fr);

            StringBuilder fileContentBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                fileContentBuilder.append(line);
            }

            fr.close();
            br.close();

            String fileContent = fileContentBuilder.toString();

            //Skip if the file already contains the string
            if (!fileContent.contains(stringToWrite)) {

                //Append url to the file
                FileWriter fw = new FileWriter(resultsFile, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(stringToWrite + "\n");
                bw.close();
            }

        } catch (IOException e) {
            XposedBridge.log(e.getMessage());
        }
    }
}
