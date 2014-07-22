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
import android.widget.Button;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MainActivity
//

public class MainActivity extends Activity {

    Button measureConnectButton;

    final String connectButtonText = "Connect";
    final String measureButtonText = "Measure";

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

        determineWhichButtonToShow();

    }//end of MainActivity::onCreate
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

    //-----------------------------------------------------------------------------
    // MainActivity::messageHandler
    //
    // Not really a function
    //
    // Instantiate a new handler and Override its handleMessage() method.
    //

    public final Handler messageHandler = new Handler() {

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

    };//end of MainActivity::messageHandler
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
    // MainActivity::onClickLister
    //
    // Not really a function.
    //
    // Listeners for clicks on the objects to which it was handed.
    //
    // Ids are used to determine which object was pressed.
    // When assigning this listener to any new objects, add the object's id to the
    // switch statement and handle the case properly.
    //

    View.OnClickListener onClickLister = new View.OnClickListener() {

        public void onClick(View pV) {

            int id = pV.getId();

            switch (id) {

                case R.id.measureConnectButton:
                    handleMeasureConnectButtonPressed();

                default:
                    return;
            }

        }

    };//end of MainActivity::onClickLister
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::determineWhichButtonToShow
    //
    // Determines whether to create the "Measure" button or the "Connect" button
    // according to the connected devices.
    //
    // hss wip -- currently just creates the "Connect" button
    //

    private void determineWhichButtonToShow() {

        //debug hss
        boolean h = true;

        if (h) {

            //hss wip -- what not
            measureConnectButton = (Button)findViewById(R.id.measureConnectButton);
            measureConnectButton.setBackground(getResources().getDrawable(R.drawable.green_styled_button));
            measureConnectButton.setText(connectButtonText);
            measureConnectButton.setOnClickListener(onClickLister);

        }

    }//end of MainActivity::determineWhichButtonToShow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleMeasureConnectButtonPressed
    //
    // Starts an activity for "More".
    // Should be called from the "More" onClick().
    //

    public void handleMeasureConnectButtonPressed() {

        String btnText = measureConnectButton.getText().toString();

        if (btnText == connectButtonText) {
            //stuff to do to connect to device
        }

        else if (btnText == measureButtonText) {
            //hss wip//stuff to do to measure
        }

    }//end of MainActivity::handleMeasureConnectButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleMoreButtonPressed
    //
    // Starts an activity for "More".
    // Should be called from the "More" onClick().
    //

    public void handleMoreButtonPressed(View pView) {

        Intent intent = new Intent(this, ItemListActivity.class);
        startActivity(intent);

    }//end of MainActivity::handleMoreButtonPressed
    //-----------------------------------------------------------------------------

}//end of class MainActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
