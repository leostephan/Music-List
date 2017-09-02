package com.lstephan.spacedev.musiclist;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.SharedPreferences.Editor;

public class MainActivity extends AppCompatActivity {

    private String[] displayer;
    private ListView mListView;
    private List<Song> filesMP3;

    private SharedPreferences mPrefs;
    private Editor prefsEditor;

    private int length;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying;

    private int lasti;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.myListView);
        final ImageButton pauseButton = (ImageButton) findViewById(R.id.imageButton2);
        ImageButton stopButton = (ImageButton) findViewById(R.id.imageButton3);
        Button seekButton = (Button) findViewById(R.id.button3);
        length = 0;

        final TextView info = (TextView) findViewById(R.id.textView2);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this.getApplicationContext());

        mediaPlayer = new MediaPlayer();
        Boolean isAny = mPrefs.getBoolean("isAny", false);
        System.out.println("isAny : " + isAny);
        if (isAny) {
            Gson gson = new Gson();
            String jsonSongs = mPrefs.getString("filesMP3", "");
            Type type = new TypeToken<List<Song>>() {
            }.getType();
            filesMP3 = gson.fromJson(jsonSongs, type);

            int listLength = 0;
            for (Song ignored : filesMP3) {
                listLength++;
            }
            displayer = new String[listLength];

            int ite = 0;
            for (Song song : filesMP3) {
                displayer[ite] = song.getName() + " - " + milliSecondsToTimer(song.getDuration());
                ite++;
            }

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, displayer);
            mListView.setAdapter(adapter);
        } else {
            info.setText("To synchronize your musics into the app, press on the top right button! (it can take up to 3mins so don't panic!)");
            info.setVisibility(View.VISIBLE);
        }

        seekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                filesMP3 = getListMP3Files();

                //SAVE filesMP3 to SharedPreference
                mPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this.getApplicationContext());
                prefsEditor = mPrefs.edit();

                Gson gson = new Gson();
                String jsonSongs = gson.toJson(filesMP3);

                prefsEditor.putString("filesMP3", jsonSongs);

                if (prefsEditor.commit()) {
                    prefsEditor.putBoolean("isAny", true);
                    prefsEditor.apply();
                }
                //filesMP3 SAVED

                int listLength = 0;
                for (Song ignored : filesMP3) {
                    listLength++;
                }
                displayer = new String[listLength];

                int ite = 0;
                for (Song song : filesMP3) {
                    displayer[ite] = song.getName() + " - " + milliSecondsToTimer(song.getDuration());
                    ite++;
                }

                final ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, displayer);
                mListView.setAdapter(adapter);

                info.setVisibility(View.INVISIBLE);
            }
        });
//FROM HERE

//TO COMPLETE
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                stopPlaying();
                isPlaying = false;
                mediaPlayer = new MediaPlayer();

                try {
                    mediaPlayer.setDataSource(filesMP3.get(i).getPath());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
                isPlaying = true;
                pauseButton.setImageResource(R.mipmap.ic_pause_circle_outline_white_24dp);
                pauseButton.setVisibility(View.VISIBLE);
                lasti = i;
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying) {
                    mediaPlayer.pause();
                    length = mediaPlayer.getCurrentPosition();
                    isPlaying = false;
                    pauseButton.setImageResource(R.mipmap.ic_play_circle_outline_white_24dp);
                } else {
                    mediaPlayer.seekTo(length);
                    mediaPlayer.start();
                    isPlaying = true;
                    pauseButton.setImageResource(R.mipmap.ic_pause_circle_outline_white_24dp);
                }
            }
        });
        /*mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                System.out.println("OnCompletion DONE");
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(filesMP3.get(lasti+1).getPath());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
                isPlaying = true;
                pauseButton.setImageResource(R.mipmap.ic_pause_circle_outline_white_24dp);
                pauseButton.setVisibility(View.VISIBLE);
                lasti++;
            }
        });*/

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlaying();
                isPlaying = false;
                pauseButton.setImageResource(R.mipmap.ic_pause_circle_outline_white_24dp);
                pauseButton.setVisibility(View.INVISIBLE);

            }
        });
    }


    public  String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    private List<Song> getListMP3Files(){
        Uri allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        ArrayList<Song> inFiles = new ArrayList<>();
        Cursor cursor = MainActivity.this.getContentResolver().query(allsongsuri, null, null, null, selection);
        int pos = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)).endsWith(".mp3")
                            && (Integer.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))) >= 1500)) {

                        String song_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        String fullpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        int duration = Integer.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                        Song newSong = new Song(fullpath, song_name, duration, artist);
                        inFiles.add(pos, newSong);

                        pos++;
                    }
                } while (cursor.moveToNext());

            }
            cursor.close();
        }
        return inFiles;
    }

    private void stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
