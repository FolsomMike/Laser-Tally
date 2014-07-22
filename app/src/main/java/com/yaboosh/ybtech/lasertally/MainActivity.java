/******************************************************************************
 * Title: MainActivity.java
 * Author: Hunter Schoonover
 * Date: 7/22/14
 *
 * Purpose:
 *
 * This class creates the main activity for the application.
 * It is created and used upon app startup.
 *
 */

//-----------------------------------------------------------------------------

package com.yaboosh.ybtech.lasertally;

//-----------------------------------------------------------------------------

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MainActivity
//

public class MainActivity extends Activity {

    //-----------------------------------------------------------------------------
    // MainActivity::onCreate
    //
    // Automatically called when the activity is created.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }//end of MainActivity::onCreate
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::moreButtonPressed
    //
    // Starts an activity for "More".
    // Should be called from the "More" onClick().
    //

    public void moreButtonPressed(View view) {

        Intent intent = new Intent(this, ItemListActivity.class);
        startActivity(intent);

    }//end of MainActivity::moreButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onActivityResult
    //
    // Listens for activity results and decides what actions to take depending on
    // their request codes and requests' result codes.
    //

    @Override
    public void onActivityResult(int pRequestCode, int pResultCode, Intent pData)
    {
        //debug hss//
    	/*switch (pRequestCode) {
    		case BluetoothTools.BLUETOOTH_REQUEST_CODE:
	            if (pResultCode == RESULT_OK) {
	            	bluetoothHandler.handleBluetoothOnState();
	            }
	            if (pResultCode == RESULT_CANCELED) {
	            	bluetoothHandler.handleBluetoothTurnOnFailed();
	            }
	            break;

            default:
            	break;
    	}//end of switch (pRequestCode)*/

    }//end of MainActivity::onActivityResult
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::Defining a Handler
    //
    // Instantiate a new handler and Override its handleMessage() method.
    //

    public final Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            //get the message type
            int type = msg.getData().getInt("what");

            //debug hss//
            //check the message types and decide what to do for each type
    			/*if (type == BluetoothConnectionThread.HANDLER_MESSAGE_BT_INSTREAM) {
    				//msg.//hss wip//
    			}*/

        }

    };//end of MainActivity::Defining a Handler
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onDestroy
    //
    // Automatically called when the activity is destroyed.
    // All functions that must be done upon destruction should be called here.
    //

    @Override
    protected void onDestroy()
    {

        super.onDestroy();

        //debug hss//bluetoothHandler.unregisterBluetoothReceiver();

    }//end of MainActivity::onDestroy
    //-----------------------------------------------------------------------------

}//end of class MainActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
