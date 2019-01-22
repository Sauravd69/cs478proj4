package com.saurav.guessfour;

/*
    Flow of this game:
    display player1's secret 4 digit
    display player2's secret 4 digit
    player1 will a chance to guess player2's secret digit
    player2 will give response after player1's guess
    player2 will get a chance to guess player1's secret digit
    player1 will give response after player2's guess
*/

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends Activity {

    private TextView player1Secret;   //textview to display player1's current secret 4 digit number
    private TextView player2Secret;   //textview to display player2's current secret 4 digit number
    private Button btnStart;          //button to start the game
    /*
    there are two listviews, left one to display info about player1, right one for player2
    */
    private ListView player1ListView;  //listview for player1
    private ListView player2ListView;  //listview for player2
    private ArrayList<String> player1DisplayList = new ArrayList<String>();
    private ArrayList<String> player2DisplayList = new ArrayList<String>();
    private int player1SecretDigit;   //variable to hold player1's current secret 4 digit number
    private int player2SecretDigit;   //variable to hold player2's current secret 4 digit number
    private int player1GuessDigit;    //variable to hold player2's secret 4 digit number what has to be guessed by player1
    private int player2GuessDigit;    //variable to hold player1's secret 4 digit number what has to be guessed by player2
    private int round1 = 0;
    private int round2 = 0;
    private int totalRound = 20;
    private int clicked = 0;
    private int i;
    private int j;
    private Thread t1;
    private Thread t2;
    private boolean isOver = false;
    private boolean isNewGame = false;

    // Values to be used by handleMessage()
    public static final int CHANGE_PLAYER1_SECRET = 0;
    public static final int CHANGE_PLAYER2_SECRET = 1;
    public static final int PLAYER1_GUESS = 2;
    public static final int PLAYER2_GUESS = 3;
    public static final int PLAYER1_RESPONSE = 4;
    public static final int PLAYER2_RESPONSE = 5;

    //handler for player1
    private Handler player1Handler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what ;
            switch (what) {
                //change player1's current secret 4 digit and display it
                case CHANGE_PLAYER1_SECRET:
                    player1Secret.setText("Secret 4 digit: "+msg.arg1);
                    break;

                //try to guess player2's secret 4 digit and display in player1's listview
                case PLAYER1_GUESS:
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                            R.layout.activity_listview_item,player1DisplayList);
                    player1ListView.setAdapter(adapter);

                //response from player1 when player2 guess and display in player2's listview
                case PLAYER1_RESPONSE:
                    ArrayAdapter<String> adapter1 = new ArrayAdapter<>(MainActivity.this,
                            R.layout.activity_listview_item2,player2DisplayList);
                    player2ListView.setAdapter(adapter1);
            }

        }
    }; // Handler is associated with UI Thread for player1

    private Handler player2Handler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what ;
            switch (what) {
                case CHANGE_PLAYER2_SECRET:
                    player2Secret.setText("Secret 4 digit: "+msg.arg1);
                    break;
                case PLAYER2_GUESS:
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                            R.layout.activity_listview_item2,player2DisplayList);
                    player2ListView.setAdapter(adapter);
                case PLAYER2_RESPONSE:
                    ArrayAdapter<String> adapter1 = new ArrayAdapter<>(MainActivity.this,
                            R.layout.activity_listview_item,player1DisplayList);
                    player1ListView.setAdapter(adapter1);
            }
        }
    }; // Handler is associated with UI Thread for player2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        player1Secret = (TextView) findViewById(R.id.player1secret);
        player2Secret = (TextView) findViewById(R.id.player2secret);
        btnStart = (Button) findViewById(R.id.buttonStart);
        player1ListView = (ListView) findViewById(R.id.player1listview);
        player2ListView = (ListView) findViewById(R.id.player2listview);


        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //user is playing first time
                if(clicked == 0)
                {
                    clicked++;

                    t1 = new Thread(new Player1()) ;
                    t2 = new Thread(new Player2()) ;

                    //start thread of player1
                    t1.start();

                    //wait 1 sec and start thread of player2
                    try { Thread.sleep(1000); }
                    catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }
                    t2.start();
                }
                //user played once, either game is over and user wants to play again or
                // user wants to do restart in middle of the game
                else
                {
                    //if the game is over because someone wins, then user wants to play again
                    if(isOver)
                    {
                        isOver = false;
                        NewGame();
                    }
                    //if the game is over because maximum number of rounds already finished
                    else if(round2 == totalRound && round1 == totalRound)
                    {
                        NewGame();
                    }
                    //user wants to do restart the game in the middle of one game
                    else if(isNewGame)
                    {
                        isNewGame = false;
                        NewGame();
                    }
                    //restart the game
                    else
                    {
                        isNewGame = true;
                    }

                    //try { Thread.sleep(1000); }
                    //catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }
                }
            }
        });
    }

    public class Player1 implements Runnable {

        public void run() {

            // Get a message instance with target set to UI thread's message queue

            //looping through total number of rounds
            for(i=0; round1 < totalRound; i=i+2)
            {
                try { Thread.sleep(2000); }
                catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

                //user wants to do restart the game
                if(isNewGame){break;}

                Message msg = player1Handler.obtainMessage(CHANGE_PLAYER1_SECRET);

                //game is over. so break the loop
                if(isOver)
                {
                    break;
                }
                //get the random digit for player1 and send msg to the handler
                else
                {
                    player1SecretDigit = getRandomDigit();
                    msg.arg1 = player1SecretDigit;
                    player1Handler.sendMessage(msg);
                }

                try { Thread.sleep(2000); }
                catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

                //get a random digit to guess player2's secret digit and send msg to handler
                msg = player1Handler.obtainMessage(PLAYER1_GUESS);
                player1GuessDigit = getRandomDigit1();
                player1DisplayList.add(i,"Player1 is guessing "+player1GuessDigit);
                player1Handler.sendMessage(msg);

                try { Thread.sleep(2000); }
                catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

                //figuring out the response from player2
                msg = player1Handler.obtainMessage(PLAYER1_RESPONSE);

                //player1 win and add to player2's arraylist to display
                if(player1GuessDigit == player2SecretDigit)
                {
                    player2DisplayList.add(i,"Player1 guessed successfully");
                    isOver = true;
                    player1Handler.sendMessage(msg);
                    break;
                }
                //player1 doesn't win
                else
                {
                    String s = Response(player2SecretDigit, player1GuessDigit);
                    player2DisplayList.add(i,s);
                    player1Handler.sendMessage(msg);
                }

                try { Thread.sleep(3000); }
                catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

                round1++;
            }
        }
    }

    public class Player2 implements Runnable {

        public void run() {

            // Get a message instance with target set to UI thread's message queue

            for(j=1; round2 < totalRound; j=j+2)
            {
                try { Thread.sleep(2000); }
                catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

                if(isNewGame){break;}

                Message msg = player2Handler.obtainMessage(CHANGE_PLAYER2_SECRET);
                player2SecretDigit = getRandomDigit();
                msg.arg1 = player2SecretDigit;
                player2Handler.sendMessage(msg);

                try { Thread.sleep(5000); }
                catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

                if(isOver)
                {
                    break;
                }
                else
                {
                    msg = player2Handler.obtainMessage(PLAYER2_GUESS);
                    player2GuessDigit = getRandomDigit();
                    player2DisplayList.add(j,"Player2 is guessing "+player2GuessDigit);
                    player2Handler.sendMessage(msg);
                }

                try { Thread.sleep(2000); }
                catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

                msg = player2Handler.obtainMessage(PLAYER2_RESPONSE);
                if(player1SecretDigit == player2GuessDigit)
                {
                    player1DisplayList.add(j,"Player2 guessed successfully");
                    isOver = true;
                    player2Handler.sendMessage(msg);
                    break;
                }
                else
                {
                    String s = Response(player1SecretDigit, player2GuessDigit);
                    player1DisplayList.add(j,s);
                    player2Handler.sendMessage(msg);
                }

                round2++;
            }
            //if(isNewGame){NewGame();}
        }
    }

    //function to generate 4 digit random number
    public int getRandomDigit()
    {
        ArrayList<Integer> digits = new ArrayList<Integer>();
        for(int k=1; k<10; k++)
        {
            digits.add(k);
        }
        Collections.shuffle(digits);
        String s = String.valueOf(digits.get(0)) + String.valueOf(digits.get(1)) +
                String.valueOf(digits.get(2)) + String.valueOf(digits.get(3));
        return Integer.parseInt(s);
    }

    //function to generate 4 digit number for player1 to guess player2's secret digit
    //where 1st 2 digit will be always same as player2's 1st 2 digit if round < 10
    public int getRandomDigit1()
    {
        int temp = player2SecretDigit;
        int x1 = temp % 10;
        temp = temp / 10;
        int x2 = temp % 10;
        temp = temp / 10;
        int x3 = temp % 10;
        ArrayList<Integer> digits = new ArrayList<Integer>();
        if(round1 < 10)
        {
            for(int k=1; k<10; k++)
            {
                if(k != x1 && k != x2)
                {digits.add(k);}
            }
            Collections.shuffle(digits);
            String s = String.valueOf(digits.get(2)) + String.valueOf(digits.get(3))+
                    String.valueOf(x2) + String.valueOf(x1);
            return Integer.parseInt(s);
        }
        else
        {
            for(int k=1; k<10; k++)
            {
                if(k != x1 && k != x2 && k != x3)
                {digits.add(k);}
            }
            Collections.shuffle(digits);
            String s = String.valueOf(digits.get(2)) + String.valueOf((x3))+
                    String.valueOf(x2) + String.valueOf(x1);
            return Integer.parseInt(s);

        }
    }

    //starting from the initial value
    public void NewGame()
    {
        //clear the listviews
        player1DisplayList.clear();
        player2DisplayList.clear();

        //removing data from both array lists
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                R.layout.activity_listview_item,player1DisplayList);
        player1ListView.setAdapter(adapter);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(MainActivity.this,
                R.layout.activity_listview_item2,player2DisplayList);
        player2ListView.setAdapter(adapter1);

        round1 = 0;
        round2 = 0;
        i=0;
        j=1;
        t1 = new Thread(new Player1()) ;
        t2 = new Thread(new Player2()) ;
        t1.start();
        try { Thread.sleep(1000); }
        catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }
        t2.start();
    }

    //return string where the string contains how many digits match and how many digits don't match
    public String Response(int secret, int guess)
    {
        int cor1 = -1;
        int cor2 = -1;
        int cor3 = -1;
        int totalcorrect = 0;
        int totalincorrect = 0;
        String s1 = "";
        String s2 = "";
        ArrayList<Integer> secretList = new ArrayList<>();
        ArrayList<Integer> guessList = new ArrayList<>();
        ArrayList<Integer> incorList = new ArrayList<>();

        for(int k = 0; k < 4; k++)
        {
            int rem1 = secret%10;
            secretList.add(rem1);
            secret = secret/10;
            int rem2 = guess%10;
            guessList.add(rem2);
            guess = guess/10;
        }

        for(int k=0; k<4; k++)
        {
            if(secretList.get(k) == guessList.get(k))
            {
                if(cor1 == -1)
                {
                    cor1 = secretList.get(k);
                    s1 = ""+cor1+" is guessed correctly.";
                    totalcorrect++;
                }
                else if(cor2 == -1)
                {
                    cor2 = secretList.get(k);
                    s1 = ""+cor1+","+cor2+" are guessed correctly.";
                    totalcorrect++;
                }
                else if(cor3 == -1)
                {
                    cor3 = secretList.get(k);
                    s1 = ""+cor1+","+cor2+","+cor3+" are guessed correctly.";
                    totalcorrect++;
                }
            }
            else
            {
                incorList.add(guessList.get(k));
            }
        }

        for(int k=0; k<incorList.size(); k++)
        {
            for(int l=0; l<4; l++)
            {
                if(incorList.get(k) == secretList.get(l))
                {
                    totalincorrect++;
                    if(totalincorrect == 1)
                    {s2 = s2 + incorList.get(k);}
                    else if(totalincorrect > 1)
                    {s2 = s2 +"," + incorList.get(k);}
                }
            }
        }
        if(totalincorrect != 0){s2 = s2 + " are guessed incorrect position";}
        if(totalcorrect == 0 && totalincorrect == 0)
        {
            return "Not a single digit doesn't match with opponent's 4 digits.";
        }
        return s1 + s2;
    }
}
