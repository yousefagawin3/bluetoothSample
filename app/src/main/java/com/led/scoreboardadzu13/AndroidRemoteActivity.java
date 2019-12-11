package com.led.scoreboardadzu13;

/*
 * TO-DO:
  *-remove the time
  *-use the time of arduino using string manipulation
  *-add the adjust time function
  *-clean the flow
  *
  *
  *
  *
  *
  * */





import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class AndroidRemoteActivity extends AppCompatActivity{
    private TextView logview;
    private Button connect, deconnect;
    private BluetoothAdapter mBluetoothAdapter = null;


    //for scoreboard
    Button H1, H2, H3, A1, A2, A3, HMinus, AMinus, show, endquar;
    Button PlusSec, MinSec, PlusMin, MinMin, change;
    TextView Hs,As, quartertext, f1,f2;
    int totalH, totalA;
    int quarter=1, OT=0, q=1;
    String totalAll;
    int foul1=0, foul2=0;


    //for gametime
    int t = 1;
    int starter;
    int starter1;

    TextView NHome, NAway;
    Button btnstart, btnreset;


    //for shotclock
    Button btnstart1, btnreset1, btnreset2;
    int t1=1;


    private String[] logArray = null;

    private BtInterface bt = null;

    static final String TAG = "Chihuahua";
    static final int REQUEST_ENABLE_BT = 3;

    //This handler listens to messages from the bluetooth interface and adds them to the log
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String data = msg.getData().getString("receivedData");
            addToLog(data);
        }
    };

    //this handler is dedicated to the status of the bluetooth connection
    final Handler handlerStatus = new Handler() {
        public void handleMessage(Message msg) {
            int status = msg.arg1;
            if(status == BtInterface.CONNECTED) {
                addToLog("Connected");
            } else if(status == BtInterface.DISCONNECTED) {
                addToLog("Disconnected");
            }
        }
    };

    //handles the log view modification
    //only the most recent messages are shown
    private void addToLog(String message){
        for (int i = 1; i < logArray.length; i++){
            logArray[i-1] = logArray[i];
        }
        logArray[logArray.length - 1] = message;

        logview.setText("");

        for (int i = 0; i < logArray.length; i++){
            if (logArray[i] != null){
                logview.append(logArray[i] + "\n");
            }
        }
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_remote);

        //clears the previous game history
        for (int i = 1; i < 5; i++) {
            try {
                FileOutputStream fos3 = null;
                fos3 = openFileOutput("Save" + String.valueOf(i) + ".txt", MODE_PRIVATE);
                fos3.write(" ".getBytes());

                fos3.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }




        //first, inflate all layout objects, and set click listeners

        //fouls
        f1 = (TextView) findViewById(R.id.f1);
        f2 = (TextView) findViewById(R.id.f2);

        //scores
        Hs = (TextView) findViewById(R.id.Hs);
        As = (TextView) findViewById(R.id.As);
        quartertext=(TextView) findViewById(R.id.quartertext);


        //countdown
        btnstart = (Button) findViewById(R.id.start);
        btnreset = (Button) findViewById(R.id.reset);

        NHome=(TextView)findViewById(R.id.NHome);
        NHome.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Foul1();

                return false;
            }
        });

        NAway=(TextView)findViewById(R.id.NAway);
        NAway.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Foul2();

                return false;
            }
        });

        //for shotclock
        btnstart1 = (Button) findViewById(R.id.start1);
        btnreset1 = (Button) findViewById(R.id.reset1);
        btnreset2 = (Button) findViewById(R.id.reset2);

        logview = (TextView)findViewById(R.id.logview);
        //I chose to display only the last 3 messages
        logArray = new String[3];

        connect = (Button)findViewById(R.id.connect);

        deconnect = (Button)findViewById(R.id.deconnect);

        //for scoreboard
        H1 = (Button)findViewById(R.id.H1);
        H2 = (Button)findViewById(R.id.H2);
        H3 = (Button)findViewById(R.id.H3);
        HMinus = (Button)findViewById(R.id.HMinus);
        A1 = (Button) findViewById(R.id.A1);
        A2 = (Button) findViewById(R.id.A2);
        A3 = (Button) findViewById(R.id.A3);
        AMinus = (Button) findViewById(R.id.AMinus);
        show = (Button) findViewById(R.id.show);
        endquar = (Button) findViewById(R.id.endquar);
        PlusSec = (Button) findViewById(R.id.PlusSec);
        MinSec = (Button) findViewById(R.id.MinSec);
        PlusMin = (Button) findViewById(R.id.PlusMin);
        MinMin = (Button) findViewById(R.id.MinMin);
        change = (Button) findViewById(R.id.change);



//game setup
        LayoutInflater factory = LayoutInflater.from(this);

//text_entry is an Layout XML file containing two text field to display in alert dialog
        final View textEntryView = factory.inflate(R.layout.dialog_basketball_start, null);

        final EditText input1 = (EditText) textEntryView.findViewById(R.id.HomeName);
        final EditText input2 = (EditText) textEntryView.findViewById(R.id.AwayName);
        final EditText input3 = (EditText) textEntryView.findViewById(R.id.Mins);


        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Enter Game Setup:").setView(textEntryView).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        if(input1.getText().toString().matches("") || input2.getText().toString().matches("") || input3.getText().toString().matches("")) {
                            Toast.makeText(getApplicationContext(),"Some detail is missing.",Toast.LENGTH_SHORT).show();

                            //restarter
                            Intent i=getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }

                        else {

                            NHome.setText(input1.getText().toString());
                            NAway.setText(input2.getText().toString());
                            starter = Integer.parseInt(input3.getText().toString());

                            bt.connect();
                        }
                    }
                });
        alert.show();



    }

    //it is better to handle bluetooth connection in onResume (ie able to reset when changing screens)
    @Override
    public void onResume() {
        super.onResume();
        //first of all, we check if there is bluetooth on the phone
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.v(TAG, "Device does not support Bluetooth");
        }
        else{
            //Device supports BT
            if (!mBluetoothAdapter.isEnabled()){
                //if Bluetooth not activated, then request it
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            }
            else{
                //BT activated, then initiate the BtInterface object to handle all BT communication
                bt = new BtInterface(handlerStatus, handler);
            }
        }
    }

    //called only if the BT is not already activated, in order to activate it
    protected void onActivityResult(int requestCode, int resultCode, Intent moreData){
        if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == Activity.RESULT_OK){
                //BT activated, then initiate the BtInterface object to handle all BT communication
                bt = new BtInterface(handlerStatus, handler);
            }
            else if (resultCode == Activity.RESULT_CANCELED)
                Log.v(TAG, "BT not activated");
            else
                Log.v(TAG, "result code not known");
        }
        else{
            Log.v(TAG, "request code not known");
        }
    }

    //handles the clicks on various parts of the screen
    //all buttons launch a function from the BtInterface object

    public void onCLick1(View v){

            setgametime();

            quartertext.setText("QUARTER: "+Integer.toString(quarter));


        connect.setVisibility(View.GONE);
            btnstart.setVisibility(View.VISIBLE);
            btnreset.setVisibility(View.VISIBLE);
            btnstart1.setVisibility(View.VISIBLE);
            btnreset1.setVisibility(View.VISIBLE);
            btnreset2.setVisibility(View.VISIBLE);
            NHome.setVisibility(View.VISIBLE);
            NAway.setVisibility(View.VISIBLE);
            Hs.setVisibility(View.VISIBLE);
            As.setVisibility(View.VISIBLE);
            H1.setVisibility(View.VISIBLE);
            H2.setVisibility(View.VISIBLE);
            H3.setVisibility(View.VISIBLE);
            HMinus.setVisibility(View.VISIBLE);
            A1.setVisibility(View.VISIBLE);
            A2.setVisibility(View.VISIBLE);
            A3.setVisibility(View.VISIBLE);
            AMinus.setVisibility(View.VISIBLE);
            show.setVisibility(View.VISIBLE);
            endquar.setVisibility(View.VISIBLE);
        f1.setVisibility(View.VISIBLE);
        f2.setVisibility(View.VISIBLE);
        PlusMin.setVisibility(View.VISIBLE);
        MinMin.setVisibility(View.VISIBLE);
        PlusSec.setVisibility(View.VISIBLE);
        MinSec.setVisibility(View.VISIBLE);
        change.setVisibility(View.VISIBLE);

        }
    public void onCLick2(View v){
            addToLog("closing connection");
            bt.close();

        }
    public void onCLick3(View v){

            if (t == 1) {
                bt.sendData("S");
                runShot();
                btnstart.setText("Pause");
                t = 0;


            } else {
                pause();
                pause1();

            }
        }
    public void onCLick4(View v){

            reset();
        }
    public void onCLick5(View v) {

            runShot();
        }
    public void onCLick6(View v) {

            reset1();
        }
    public void onCLick7(View v){

            reset2();
        }

    public void onCLick8(View v) {
            bt.sendData("1");
            reset1();
            totalH=Integer.parseInt(Hs.getText().toString())+ 1;
            Hs.setText(Integer.toString(totalH));
            totalAll=Integer.toString(totalH)+" "+Integer.toString(totalA);
        }
    public void onCLick9(View v){
            bt.sendData("2");
            reset1();
            totalH=Integer.parseInt(Hs.getText().toString())+ 2;
            Hs.setText(Integer.toString(totalH));
            totalAll=Integer.toString(totalH)+" "+Integer.toString(totalA);

        }
    public void onCLick10(View v){
            bt.sendData("3");
            reset1();
            totalH=Integer.parseInt(Hs.getText().toString())+ 3;
            Hs.setText(Integer.toString(totalH));
            totalAll=Integer.toString(totalH)+" "+Integer.toString(totalA);

        }
    public void onCLick11(View v) {
            if(totalH!=0) {
                totalH = Integer.parseInt(Hs.getText().toString()) - 1;
                Hs.setText(Integer.toString(totalH));
                totalAll = Integer.toString(totalH) + " " + Integer.toString(totalA);
                bt.sendData("4");
            }
        }
    public void onCLick12(View v) {
            bt.sendData("5");
            reset1();
            totalA=Integer.parseInt(As.getText().toString())+ 1;
            As.setText(Integer.toString(totalA));
            totalAll=Integer.toString(totalH)+" "+Integer.toString(totalA);

        }
    public void onCLick13(View v){
            bt.sendData("6");
            reset1();
            totalA=Integer.parseInt(As.getText().toString())+ 2;
            As.setText(Integer.toString(totalA));
            totalAll=Integer.toString(totalH)+" "+Integer.toString(totalA);

        }
    public void onCLick14(View v) {
            bt.sendData("7");
            reset1();
            totalA=Integer.parseInt(As.getText().toString())+ 3;
            As.setText(Integer.toString(totalA));
            totalAll=Integer.toString(totalH)+" "+Integer.toString(totalA);

        }
    public void onCLick15(View v) {
            if(totalA!=0) {
                totalA = Integer.parseInt(As.getText().toString()) - 1;
                As.setText(Integer.toString(totalA));
                totalAll = Integer.toString(totalH) + " " + Integer.toString(totalA);
                bt.sendData("8");
            }
        }


    public void onCLick16(View v) {
                showhist();
        }
    public void onCLick17(View v) {

                totalAll = Integer.toString(totalH) + " " + Integer.toString(totalA);
                if (quarter < 5) {

                    try {
                        FileOutputStream fos3 = null;
                        fos3 = openFileOutput("Save" + String.valueOf(quarter) + ".txt", MODE_PRIVATE);
                        fos3.write(totalAll.getBytes());

                        fos3.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    quarter = quarter + 1;
                    quartertext.setText("QUARTER: "+Integer.toString(quarter));
                }

                bt.sendData("Q");
            setgametime();
                showhist();
            }

    public void onCLick18(View v) {
        bt.sendData("E");
        Toast.makeText(getApplicationContext(),"+1 Minute.",Toast.LENGTH_SHORT).show();
        //plus min
    }

    public void onCLick19(View v) {
        bt.sendData("T");
        Toast.makeText(getApplicationContext(),"-1 Minute.",Toast.LENGTH_SHORT).show();

        //minus min
    }
    public void onCLick20(View v) {
        bt.sendData("Y");
        Toast.makeText(getApplicationContext(),"+1 Second.",Toast.LENGTH_SHORT).show();
        //Plus sec
    }
    public void onCLick21(View v) {
        bt.sendData("U");
        Toast.makeText(getApplicationContext(),"-1 Second.",Toast.LENGTH_SHORT).show();
        //Minus sec
    }
    public void onCLick22(View v) {

        if (q == 1) {
            bt.sendData("h");
            q = 0;


        } else {
            bt.sendData("j");
            q = 1;
        }

        Toast.makeText(getApplicationContext(),"Possession Changed.",Toast.LENGTH_SHORT).show();
        //Changed possession
    }





    //function for text file
    public String retrive(String string) {
        String out = null;
        try {
            FileInputStream fis = openFileInput(string);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String sLine = null;
            out = "";
            while ((sLine = br.readLine()) != null) {
                out += sLine;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (out == null)
            out = "N/A";
        return out;
    }


    public void showhist() {

            LayoutInflater factory = LayoutInflater.from(this);

//text_entry is an Layout XML file containing two text field to display in alert dialog
            final View textEntryView = factory.inflate(R.layout.dialog_basketball_history, null);


            final AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Game History").setView(textEntryView).setPositiveButton("OK",


                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {

                        }
                    });
            alert.setMessage("1st Quarter: " + retrive("Save1.txt") + "\n" +
                    "2nd Quarter: " + retrive("Save2.txt") + "\n" +
                    "3rd Quarter: " + retrive("Save3.txt") + "\n" +
                    "4th Quarter: " + retrive("Save4.txt")
            );
            alert.show();

    }

    public void setgametime(){

        bt.sendData("R");

        if(starter<10){
            starter1=starter-10;
            starter1=starter1*-1;
            for(int i=starter1;i>0;i--){
                bt.sendData("T");

                //ewan pero ito ang paraan para magwork
                bt.sendData("T");
            }
        }

        if(starter>10){
            starter1=starter-10;
            for(int i=starter1;i>0;i--){
                bt.sendData("E");

                //ewan pero ito ang paraan para magwork
                bt.sendData("E");
            }
        }
    }

    public void runShot() {
        if (t1 == 1) {
            btnstart1.setText("Pause");
            t1 = 0;
            bt.sendData("s");
        } else {
            pause1();
        }
        return;
    }

    public void reset(){
        reset1();
        t = 1;
        bt.sendData("R");
        return;
    }

    public void reset1(){
        t1 = 1;
        btnstart1.setText("Start");
        bt.sendData("r");
        return;
    }

   public void reset2(){

        t1 = 1;
        bt.sendData("z");
       return;
    }

   public void pause(){
        btnstart.setText("Start");
        t = 1;
        bt.sendData("P");
       return;
    }

    public void pause1(){

        t1 = 1;
        btnstart1.setText("Start");
        bt.sendData("p");
        return;
    }

    public void Foul1(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Give foul on Team A?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                bt.sendData(",");
                foul1=foul1+1;
                f1.setText("Foul1: "+Integer.toString(foul1));
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){

            }
        });
        AlertDialog alert= builder.create();
        alert.show();
    }

    public void Foul2(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Give foul on Team B?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                bt.sendData("/");
                foul2=foul2+1;
                f2.setText("Foul2: "+Integer.toString(foul2));
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){

            }
        });
        AlertDialog alert= builder.create();
        alert.show();
    }

}