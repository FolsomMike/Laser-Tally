/******************************************************************************
* Title: Tools.java
* Author: Hunter Schoonover
* Create Date: 09/26/14
* Last Edit: 
*
* Purpose:
*
* This class is used for static functions that serve as common tools.
*
*/

//-----------------------------------------------------------------------------


package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
//class Tools
//

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class Tools {

    public static final String TAG = "Tools";

    //-----------------------------------------------------------------------------
    // Tools::deleteDirectory
    //
    // Deletes the passed in directory and all of its contents.
    //

    public static void deleteDirectory(File pDir) {

        try {

            File[] files = pDir.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                    continue;
                }
                f.delete();
            }

            pDir.delete();

        } catch (Exception e) {}

    }//end of Tools::deleteDirectory
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // Tools::extractValueFromString
    //
    // Extracts and returns the value after the equals sign from the passed in
    // string.
    //

    public static String extractValueFromString(String pString) {

        int startPos = pString.indexOf("=") + 1;
        return pString.substring(startPos);

    }//end of Tools::extractValueFromString
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // Tools::getValueFromList
    //
    // Scans through the passed in List for the passed in Key and returns the
    // value found at that string. Returns empty string if the Key is not found.
    //

    public static String getValueFromList(String pKey, ArrayList<String> pList) {

        // If the key is found in the list, the value for the
        // key is extracted from the line and returned.
        for (String s : pList) { if (s.contains(pKey)) { return extractValueFromString(s);} }

        return "";

    }//end of Tools::getValueFromList
    //-----------------------------------------------------------------------------

}//end of class Tools
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
