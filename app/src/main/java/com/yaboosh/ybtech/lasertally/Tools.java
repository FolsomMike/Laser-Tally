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

}//end of class Tools
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
