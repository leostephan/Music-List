package com.example.spacedev.musiclist;

import android.content.SharedPreferences;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.SharedPreferences.*;

public class MainActivity extends AppCompatActivity {

    private String[] displayer;
    private ListView mListView;
    private List<Song> filesMP3;

    private SharedPreferences mPrefs;
    private Editor prefsEditor;

    private int length;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.myListView);
        final ImageButton pauseButton = (ImageButton) findViewById(R.id.imageButton2);
        ImageButton stopButton = (ImageButton) findViewById(R.id.imageButton3);
        Button seekButton = (Button) findViewById(R.id.button3);
        length = 0;

        mPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this.getApplicationContext());

        Boolean isAny = mPrefs.getBoolean("isAny", false);
        System.out.println("isAny : "+isAny );
        if(isAny){
            Gson gson = new Gson();
            String jsonSongs = mPrefs.getString("filesMP3", "");
            Type type = new TypeToken<List<Song>>(){}.getType();
            filesMP3 = gson.fromJson(jsonSongs, type);

            int listLength = 0;
            for (Song ignored : filesMP3) {
                listLength++;
            }
            displayer = new String[listLength];

            int ite = 0;
            for (Song song : filesMP3){
                displayer[ite] = song.getName();
                ite++;
            }

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, displayer);
            mListView.setAdapter(adapter);
        }

        seekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                filesMP3 = getListMP3Files(new File("/storage"));

                System.out.println("filesMP3 = getListMP3Files(new File(...)); PASSED");
                //SAVE filesMP3 to SharedPreference
                mPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this.getApplicationContext());
                prefsEditor = mPrefs.edit();

                System.out.println("prefsEditor = mPrefs.edit(); PASSED");

                Gson gson = new Gson();
                String jsonSongs = gson.toJson(filesMP3);

                System.out.println(jsonSongs);
                System.out.println("jsonSongs = gson.toJson(filesMP3); PASSED");

                prefsEditor.putString("filesMP3", jsonSongs);

                System.out.println("prefsEditor.putString(\"filesMP3\", jsonSongs); PASSED");
                if(prefsEditor.commit()){
                    System.out.println("commit succeed!");
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
                for (Song song : filesMP3){
                    displayer[ite] = song.getName();
                    ite++;
                }

                final ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, displayer);
                mListView.setAdapter(adapter);

            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("You clicked on : " + filesMP3.get(i).getName());

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
                pauseButton.setVisibility(View.VISIBLE);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlaying){
                    mediaPlayer.pause();
                    length = mediaPlayer.getCurrentPosition();
                    isPlaying = false;
                    pauseButton.setImageResource(R.mipmap.ic_play_circle_outline_white_24dp);
                }
                else{
                    mediaPlayer.seekTo(length);
                    mediaPlayer.start();
                    isPlaying = true;
                    pauseButton.setImageResource(R.mipmap.ic_pause_circle_outline_white_24dp);
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlaying();
                isPlaying = false;
                pauseButton.setVisibility(View.INVISIBLE);
            }
        });
    }


    public ListView getmListView() {
        return mListView;
    }



    private List<Song> getListMP3Files(File parentDir) {
        ArrayList<Song> inFiles = new ArrayList<>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListMP3Files(file));
            } else {
                if(file.getName().length() > 4) {
                    String extension = file.getName().substring(file.getName().length() - 4);
                    if (extension.compareTo(".mp3") == 0) {
                        System.out.println(file.getAbsolutePath());
                        Song newSong = new Song(file.getPath(), file.getName());
                        inFiles.add(newSong);
                    }
                }
            }
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
    public void setmListView(ListView mListView) {
        this.mListView = mListView;
    }
}
