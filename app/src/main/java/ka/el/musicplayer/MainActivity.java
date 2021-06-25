package ka.el.musicplayer;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private ArrayList<String> musicsNames;
    private HashMap musicsPaths;
    private int currentIdxOfMusic = 0;
    private int countMusic;

    private TextView musicInfo_name, current_music_progress, current_music_duration;
    private ImageView skip_previous, skip_next, toggle_play, shuffle_btn, looping_btn;
    private SeekBar progressMusic;
    private boolean isTouchProgressBar = false;

    private Timer timer;
    private MediaPlayer musicPlayer;
    private AudioManager audioManager;

    private boolean isShuffle = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        initMusicsPath();

        initElements();
        addListeners();
    }

    private void initMusicsPath() {

        musicsPaths = new HashMap();
        musicsPaths.put("Dobro - Мой путь", R.raw.dobro_my_way);
        musicsPaths.put("Scott Rill - You don`t know me", R.raw.filv_scott_rill__you_dont_know_me);
        musicsPaths.put("Twenty One Pilots - Goner", R.raw.twenty_one_pilots__goner);
        musicsPaths.put("Imagine Dragons - Thunder", R.raw.imagine_dragons_thunder);

        musicsNames = new ArrayList<>();
        for (Object musicName : musicsPaths.keySet()) {
            musicsNames.add(musicName.toString());
        }

        countMusic = musicsNames.size();
    }

    private String getNameMusicById(int indexOfMusic) {
        return musicsNames.get(indexOfMusic);
    }

    private void initMusicPlayer(int musicIndex) {
        String musicName = getNameMusicById(musicIndex);
        Log.d("MyLog", "Name: " + getNameMusicById(musicIndex));

        musicInfo_name.setText(musicName);
        musicPlayer = MediaPlayer.create(this, (Integer) musicsPaths.get(musicName));
        musicPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d("MyLog", "IsLooping: " + mp.isLooping());

                        if (mp.isLooping()) {
                            chooseMusic("");
                        } else {
                            chooseMusic("Next");
                        }
                    }
                }
        );
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        int duration = musicPlayer.getDuration();
        current_music_duration.setText(secToMinutes(duration / 1000));
        progressMusic.setMax(duration);

    }

    @Override
    protected void onDestroy() {
        musicPlayer.stop();
        musicPlayer.setLooping(false);
        finish();

        super.onDestroy();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addListeners() {
        skip_previous.setOnClickListener(this);
        skip_previous.setOnTouchListener(this);

        skip_next.setOnClickListener(this);
        skip_next.setOnTouchListener(this);

        toggle_play.setOnClickListener(this);
        toggle_play.setOnTouchListener(this);

        shuffle_btn.setOnClickListener(this);
        looping_btn.setOnClickListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        String status = "";
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            status = "active";
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            status = "disable";
        }
        changeActiveBtn(v.getId(), status);

        return false;
    }

    private void changeActiveBtn(int id, String status) {
        ImageView element;
        int iconResId = 0;
        int ringResId = R.drawable.ring;

        switch (id) {
            case R.id.skip_previous: {
                element = skip_previous;
                iconResId = status.equals("active") ? R.drawable.skip_previous_active : R.drawable.skip_previous;
                break;
            }

            case R.id.skip_next: {
                element = skip_next;
                iconResId = status.equals("active") ? R.drawable.skip_next_active : R.drawable.skip_next;
                break;
            }

            case R.id.togglePlay: {
                element = toggle_play;
                if (musicPlayer.isPlaying()) {
                    iconResId = status.equals("active") ? R.drawable.pause_active: R.drawable.pause;
                } else {
                    iconResId = status.equals("active") ? R.drawable.play_arrow_active : R.drawable.play_arrow;
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + id);
        }

        ringResId = status.equals("active") ? R.drawable.ring_active : R.drawable.ring;
        element.setBackgroundResource(ringResId);
        element.setImageResource(iconResId);

    }

    private void initElements() {

        current_music_duration = findViewById(R.id.current_music_duration);
        current_music_progress = findViewById(R.id.current_music_progress);
        musicInfo_name = findViewById(R.id.musicInfo_name);
        progressMusic = findViewById(R.id.progressMusic);


        initMusicPlayer(0);


        shuffle_btn = findViewById(R.id.shuffle);
        looping_btn = findViewById(R.id.looping);


        skip_previous = findViewById(R.id.skip_previous);
        skip_next = findViewById(R.id.skip_next);
        toggle_play = findViewById(R.id.togglePlay);

        progressMusic.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        isTouchProgressBar = true;
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        musicPlayer.seekTo(seekBar.getProgress());
                        isTouchProgressBar = false;
                    }
                }
        );



        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isTouchProgressBar) {
                    int currentPos = musicPlayer.getCurrentPosition();
                    progressMusic.setProgress(currentPos);
                    current_music_progress.setText(secToMinutes(currentPos / 1000));
                    Log.d("MyLog", "Pos: " + currentPos / 1000);
                }
            }
        }, 0, 500);
    }

    private String secToMinutes(int currentPos) {
        int minutes = currentPos / 60;
        int sec = currentPos % 60;
        return addZero(minutes) + ":" + addZero(sec);
    }

    private String addZero(int i) {
        return i < 10 ? "0" + i : String.valueOf(i);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.skip_previous: {
                chooseMusic("Back");
                break;
            }

            case R.id.togglePlay: {
                togglePlayListener();
                break;
            }

            case R.id.skip_next: {
                chooseMusic("Next");
                break;
            }

            case R.id.looping: {
                if (musicPlayer.isLooping()) {
                    musicPlayer.setLooping(false);
                    Log.d("MyLog", "IsLooping: " + musicPlayer.isLooping());
                    loopingShuffleToggler();
                } else {
                    loopingShuffleToggler("loop");
                    musicPlayer.setLooping(true);
                }
                break;
            }

            case R.id.shuffle: {
                if (isShuffle) {
                    isShuffle = false;
                    loopingShuffleToggler();
                } else {
                    isShuffle = true;
                    loopingShuffleToggler("shuffle");
                }

                break;
            }
        }
    }

    private void loopingShuffleToggler(String goToElement) {
        if (goToElement.equals("loop")) {
            shuffle_btn.setImageResource(R.drawable.shuffle);
            looping_btn.setImageResource(R.drawable.loop_active);
        } else if (goToElement.equals("shuffle")) {
            shuffle_btn.setImageResource(R.drawable.shuffle_active);
            looping_btn.setImageResource(R.drawable.loop);
        }
    }

    private void loopingShuffleToggler() {
        shuffle_btn.setImageResource(R.drawable.shuffle);
        looping_btn.setImageResource(R.drawable.loop);
    }

    private int getRandomInt(int min, int max) {
        return (int) (Math.floor(Math.random() * (max - min + 1)) + min);
    }


    private void chooseMusic(String status) {
        stopMusic();

        if (isShuffle) {
            int newPos = getRandomInt(0, musicsNames.size() - 1);
            while (newPos == currentIdxOfMusic) {
                newPos = getRandomInt(0, musicsNames.size() - 1);
            }
            currentIdxOfMusic = newPos;
        } else if (status.equals("Next")) {
            currentIdxOfMusic = currentIdxOfMusic == countMusic - 1 ? 0 : currentIdxOfMusic + 1;
        } else if (status.equals("Back")) {
            currentIdxOfMusic = currentIdxOfMusic == 0 ? countMusic - 1 : currentIdxOfMusic - 1;
        }
        initMusicPlayer(currentIdxOfMusic);
        startMusic();
    }

    private void startMusic() {
        musicPlayer.start();
        toggle_play.setImageResource(R.drawable.pause);
    }

    private void stopMusic() {
        musicPlayer.pause();
        toggle_play.setImageResource(R.drawable.play_arrow);
    }

    private void togglePlayListener() {
        if (musicPlayer.isPlaying()) {
            stopMusic();
        } else {
            startMusic();
        }
    }
}