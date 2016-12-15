package com.example.ivan.lab5_threads;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Random;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity {
    final int updateWinner = 1;
    final int updateLoser = 2;
    final int SEATS = 5;
    Driver d = new Driver(SEATS);
    Thread dt = new Thread(d);
    private ImageView[] seatImages;
    int numOfRounds = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seatImages = new ImageView[SEATS+1]; //To account for her design of +1 for loser seat
        seatImages[0] = (ImageView) findViewById(R.id.seatloser);
        seatImages[1] = (ImageView) findViewById(R.id.seat1);
        seatImages[2] = (ImageView) findViewById(R.id.seat2);
        seatImages[3] = (ImageView) findViewById(R.id.seat3);
        seatImages[4] = (ImageView) findViewById(R.id.seat4);
        seatImages[5] = (ImageView) findViewById(R.id.seat5);

        Button goBtn = (Button) findViewById(R.id.button);
        assert goBtn != null;
        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dt.start();
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
           // Log.d("switchMsg", "msg.what is: "+msg.what);
            int player = msg.arg2;
            Bitmap b = getImage(player);
            switch(msg.what){
               case updateWinner:
                    //Log.d("hndleWMsg", "Player: " + msg.what + " found seat");
                    //int player = msg.arg2;
                    int takenSeat = msg.arg1;
                    seatImages[takenSeat].setImageBitmap(b);
                    break;
                case updateLoser:
                    Log.d("hndleLMsg", "Player: " + player + " lost");
                    int loser = msg.arg1;
                    int loserSeat = 0;
                    seatImages[loserSeat].setImageBitmap(b); //last seat is for loser
                    break;

            }

        }
    };

    private Bitmap getImage(int threadNum){
        switch(threadNum) {
            case 1: return BitmapFactory.decodeResource(getResources(),R.drawable.one);
            case 2: return BitmapFactory.decodeResource(getResources(),R.drawable.two);
            case 3: return BitmapFactory.decodeResource(getResources(),R.drawable.three);
            case 4: return BitmapFactory.decodeResource(getResources(),R.drawable.four);
            case 5: return BitmapFactory.decodeResource(getResources(),R.drawable.five);
            case 6: return BitmapFactory.decodeResource(getResources(),R.drawable.six);
        }
        return BitmapFactory.decodeResource(getResources(),R.drawable.robot);
    }

    public class Driver implements Runnable {
        int num_seats;
        private Seat[] seats;
        private Player[] players;
        int seats_taken = 0;
        Message msg;

        Driver(int seats) {
            num_seats = seats;
        } //constructor for Driver

        Driver() {
            num_seats = 5;
        } // No argument constructor for Driver

        class Player implements Runnable {
            private int which_player;   // so they know which player they represent
            private boolean loser;    // keep track of whether or not they have lost
            //   - this is not used in the 1-round version
            private Random r = new Random();

            Player(int which) {
                which_player = which;
                loser = false;
            }

            // next two functions are not used in 1-round version but I found them
            // useful for the full game
            public boolean isLoser() {
                return loser;
            }

            public void lost() {
                loser = true;
            }

            // run() gets called when the thread is started
            public void run() {
                // start with random wait
                try {
                    int i = r.nextInt(10);
                    sleep(10 + i);
                } catch (InterruptedException e) {
                }
                // then try to get a seat
                for (int j = 0; j < (num_seats + 1); j++) {
                    if (seats[j].sit(which_player))
                      //  msg = handler.obtainMessage(updateWinner, j, which_player);
                     //   Toast.makeText(getApplicationContext(),"Player: "+which_player
                     //   +" got a seat",Toast.LENGTH_SHORT).show();
                        break;
                }
                //handler.sendMessage(msg);
            }
        }


        public void run() {
// Create the correct number of seats, plus an extra 'loser' seat.
            seats = new Seat[num_seats + 1]; // Last 'seat' is the loser
            for (int i = 0; i < num_seats + 1; i++) seats[i] = new Seat();
//
// Create the Players
            Player p[] = new Player[num_seats + 1];
            for (int i = 0; i < num_seats + 1; i++) {
                p[i] = new Player(i);
            }

            int seats_left = num_seats + 1;


// This is the code for a single round - this will go inside your main loop
//     Step 1:  initialize the remaining seats
            seats_taken = 0;
            int num_Of_Rounds = 5;
            for (int a = 0; a < num_Of_Rounds; a++) {
                for (int i = 0; i < num_seats + 1; i++) seats[i].reset_seat();

                //     Step 2:  start the correct number of threads.  In the code below, I start all of them
                //     but you will want to only start players who haven't lost yet
                for (int i = 0; i < num_seats + 1; i++) {
                    Thread pt = new Thread(p[i]);
                    if (p[i].isLoser() == false) {
                        pt.start();
                    }

                }

                //    Step 3: have the players compete for the seats.
                while (seats_taken < seats_left)
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                    }

                //    Step 4: print out the results of the round
                for (int i = 0; i < num_seats; i++) {
                    //System.out.println("Seat "+i+ ": Player " + seats[i].get_player());
                    System.out.println("Seat: " + i + ": Player " + seats[i].get_player());
                    msg = handler.obtainMessage(updateWinner,i+1,seats[i].get_player());
                   handler.sendMessage(msg);
                }
                //System.out.println("Player " + seats[seats_left-1].get_player() + " is out");
                System.out.println("Player " + seats[num_seats].get_player() + " is out");
                msg = handler.obtainMessage(updateLoser,0,seats[num_seats].get_player());
                handler.sendMessage(msg);
                num_seats--;

            }
            System.out.println("Player " + seats[0].get_player() + " is the winner!");
        }

        class Seat {
            // This class implements the seats.  Initially a seat is empty
// (isTaken = false) - once some thread calls sit, it claims that
// seat (isTaken = true) and any other player is returned a false value.
// Notice that the 'sit' function is synchronized - only one player can be
// trying to sit at a time.
            boolean isTaken;
            int who;

            Seat() {
                isTaken = false;
                who = -1;
            }

            int whoseSeat() {
                return who;
            }

            synchronized boolean sit(int i) {
                if (isTaken) {
                    return false;
                } else {
                    who = i;
                    isTaken = true;
                    seats_taken++;
                    return true;
                }
            }

            int get_player() {
                return who;
            }

            void reset_seat() {
                isTaken = false;
            }
        }


    }

}
