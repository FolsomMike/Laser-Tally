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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.lang.ref.WeakReference;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// class MainActivity
//

public class MainActivity extends Activity {

    Button measureConnectButton;
    Button redoButton;

    BluetoothLeClient bluetoothLeClient = null;

    final String connectButtonText = "Connect";
    final String measureButtonText = "Measure";

    //-----------------------------------------------------------------------------
    // MainActivity::MainActivity (constructor)
    //

    public MainActivity() {

        super();

    }//end of MainActivity::MainActivity (constructor)
    //-----------------------------------------------------------------------------

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

    }//end of MainActivity::onDestroy
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onStart
    //
    // Automatically called when the activity is started.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onStart() {

        super.onStart();

    }//end of MainActivity::onStart
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::onStop
    //
    // Automatically called when the activity is stopped.
    // All functions that must be done upon instantiation should be called here.
    //

    @Override
    protected void onStop() {

            super.onStop();

    }//end of MainActivity::onStop
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handler
    //
    // Not really a function
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

    };//end of MainActivity::handler
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
                    break;

                case R.id.redoButton:
                    //debug hss//
                    handleRedoButtonPressed();
                    break;

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

            measureConnectButton = (Button)findViewById(R.id.measureConnectButton);
            measureConnectButton.setBackground(getResources().getDrawable
                                                                (R.drawable.green_styled_button));
            measureConnectButton.setText(connectButtonText);
            measureConnectButton.setOnClickListener(onClickLister);

            redoButton = (Button)findViewById(R.id.redoButton);
            redoButton.setOnClickListener(onClickLister);
            //debug hss//redoButton.setVisibility(View.INVISIBLE);

        }

    }//end of MainActivity::determineWhichButtonToShow
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::handleMeasureConnectButtonPressed
    //
    // Calls functions depending on the text of the handleMeasureConnectButton
    // button.
    //

    public void handleMeasureConnectButtonPressed() {

        String btnText = measureConnectButton.getText().toString();

        //debug hss//
        System.out.println("handleMeasureConnectButton was pressed -- text: " + btnText);

        if (btnText == connectButtonText) {
            //stuff to do to connect to device
           startLeScanningProcess();
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

    //-----------------------------------------------------------------------------
    // MainActivity::handleRedoButtonPressed
    //
    // //hss wip//
    //

    public void handleRedoButtonPressed() {

        if (bluetoothLeClient != null) {
            bluetoothLeClient.turnLaserOn();
        }

    }//end of MainActivity::handleRedoButtonPressed
    //-----------------------------------------------------------------------------

    //-----------------------------------------------------------------------------
    // MainActivity::startLeScanningProcess
    //
    // Creates a BluetoothLeClient and initiate it using MODE0.
    //

    public void startLeScanningProcess() {

        Intent intent = new Intent(this, BluetoothScanActivity.class);
        startActivity(intent);

    }//end of MainActivity::startLeScanningProcess
    //-----------------------------------------------------------------------------

}//end of class MainActivity
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
