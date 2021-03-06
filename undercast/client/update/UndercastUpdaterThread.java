package undercast.client.update;
//You may not release this source under any condition, it must be linked to this page
//You may recompile and publish as long as skipperguy12 and Guru_Fraser are given credit
//You may not claim this to be your own
//You may not remove these comments

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import undercast.client.UndercastData;

import net.minecraft.src.mod_Undercast;
import net.minecraft.src.mod_Undercast;

public class UndercastUpdaterThread extends Thread{
    boolean errorOccured;
    public static boolean finished = false;
    
    public UndercastUpdaterThread(){
        errorOccured = false;
        try {
            start();
        } catch(Exception e) {
            System.out.println("[UndercastMod]: Failed to check for updates");
            System.out.println("[UndercastMod]: ERROR: " + e.toString());
        }
    }

    public void run() {
        String readline = "";
        String readline2 = "Could not get update information.";
        String readline3 = "1:2:3:-1";
        boolean emergencyParser = false; //If we should use the emergency parser
        errorOccured = false;
        try {
            //download link
            URL data = new URL("https://raw.github.com/UndercastTeam/UndercastClient/master/version.txt");
            final BufferedReader in = new BufferedReader(new InputStreamReader(data.openStream()));
            readline = in.readLine();
            readline2 = in.readLine();
            readline3 = in.readLine();
            emergencyParser = Boolean.parseBoolean(in.readLine());
            UndercastData.emergencyParser = emergencyParser;
            UndercastData.latestVersion = readline;
        } catch (Exception e) {
            UndercastData.setUpdate(false);
            UndercastData.setUpdateLink("Could not get update information.");
        }
        if (!mod_Undercast.MOD_VERSION.contains("dev") && compareVersions(readline)){
            UndercastData.setUpdate(false);
            if(!errorOccured) {
                UndercastData.setUpdateLink(readline2);
            } else {
                UndercastData.setUpdateLink("An unknown error occured while getting the update information.");
            }
        }
        if (readline3 != null) {
            try {
                Integer[] pagesInt;
                String[] pagesStr = readline3.split("[:]{1}");
                pagesInt = new Integer[pagesStr.length];
                for (int c = 0; c < pagesInt.length; c++) {
                    pagesInt[c] = Integer.parseInt(pagesStr[c]);
                }
                UndercastData.parsedPages = pagesInt;
            } catch (Exception e) {
            }
        }
        finished = true;
    }

    /**
     * @return true if the current version number is lower than the latest version = update necessary
     */
    private boolean compareVersions(String internetVersion) {
        try {
            String debug[] = mod_Undercast.MOD_VERSION.split("[.]");
            int majorVersionMod = Integer.parseInt(mod_Undercast.MOD_VERSION.split("[.]")[0]);;
            int majorVersionLatest = Integer.parseInt(internetVersion.split("[.]")[0]);
            int minorVersionMod = Integer.parseInt(mod_Undercast.MOD_VERSION.split("[.]")[1]);;
            int minorVersionLatest = Integer.parseInt(internetVersion.split("[.]")[1]);
            int revisionMod = Integer.parseInt(mod_Undercast.MOD_VERSION.split("[.]")[2]);;
            int revisionLatest = Integer.parseInt(internetVersion.split("[.]")[2]);;
            
            if (majorVersionMod < majorVersionLatest) {
                return true;
            } else if (majorVersionMod == majorVersionLatest && minorVersionMod < minorVersionLatest) {
                return true;
            } else if (majorVersionMod == majorVersionLatest && minorVersionMod == minorVersionLatest && revisionMod < revisionLatest) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            // failed to compare the data, sending update message.
            errorOccured = true;
            return true;
        }
    }
}