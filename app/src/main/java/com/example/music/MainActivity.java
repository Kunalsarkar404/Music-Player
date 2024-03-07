package com.example.music;

// Import necessary packages and classes

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Declare MediaPlayer and other variables
    MediaPlayer music;
    int[] songs = {R.raw.sound, R.raw.sound2, R.raw.sound3,};
    int currentSongIndex = 0;

    // Declare UI elements
    ImageView musicImageView;
    TextView musicNameTextView;
    SeekBar musicSeekBar;
    TextView currentTimeTextView;
    TextView durationTextView;
    Handler seekBarHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        musicImageView = findViewById(R.id.musicImageView);
        musicNameTextView = findViewById(R.id.musicNameTextView);
        music = MediaPlayer.create(this, songs[currentSongIndex]);
        updateMusicName();

        musicSeekBar = findViewById(R.id.musicSeekBar);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        durationTextView = findViewById(R.id.durationTextView);
        seekBarHandler = new Handler(Looper.getMainLooper());

        // Get the original bitmap from the drawable resource
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_image);

        // Apply rounded corners and white border to the bitmap
        Bitmap roundedBitmap = getRoundedCornerBitmap(originalBitmap, 20, Color.WHITE, 10);

        // Set the modified bitmap to the ImageView
        musicImageView.setImageDrawable(new BitmapDrawable(getResources(), roundedBitmap));

        // Set SeekBar change listener to update music playback position
        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // If SeekBar position is changed by the user, update music playback position
                    music.start();
                    music.seekTo(progress);
                    currentTimeTextView.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action when the user starts tracking the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action when the user stops tracking the SeekBar
            }
        });

        // Initialize and update SeekBar and time display
        updateSeekBarAndTime();
    }

    // Function to play or pause the music
    public void musicplay(View view) {
        if (music.isPlaying()) {
            music.pause();
        } else {
            music.start();
        }
        updatePlayPauseButton();
    }

    // Function to play the next song
    public void musicnext(View view) {
        if (currentSongIndex < songs.length - 1) {
            currentSongIndex++;
        } else {
            currentSongIndex = 0;
        }

        playSelectedSong();
    }

    // Function to play the previous song
    public void musicprevious(View view) {
        if (currentSongIndex > 0) {
            currentSongIndex--;
        } else {
            currentSongIndex = songs.length - 1;
        }

        playSelectedSong();
    }

    // Function to play the selected song
    private void playSelectedSong() {
        if (music.isPlaying()) {
            music.stop();
        }
        music = MediaPlayer.create(this, songs[currentSongIndex]);
        music.start();
        updateMusicName();
        updatePlayPauseButton();
    }

    // Function to update the displayed music name
    private void updateMusicName() {
        int currentSongResourceId = songs[currentSongIndex];
        String musicName = getResources().getResourceEntryName(currentSongResourceId);
        musicNameTextView.setText(musicName);
    }

    // Function to update the Play/Pause button based on the music playback state
    private void updatePlayPauseButton() {
        Button playPauseButton = findViewById(R.id.playPause);
        if (music.isPlaying()) {
            playPauseButton.setBackgroundResource(R.drawable.baseline_pause_24);
        } else {
            playPauseButton.setBackgroundResource(R.drawable.baseline_play_arrow_24);
        }
    }

    // Function to continuously update SeekBar and time display
    private void updateSeekBarAndTime() {
        musicSeekBar.setMax(music.getDuration());
        seekBarHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int currentPosition = music.getCurrentPosition();
                int totalDuration = music.getDuration();
                musicSeekBar.setProgress(currentPosition);
                currentTimeTextView.setText(formatTime(currentPosition));
                durationTextView.setText(formatTime(totalDuration));

                // Schedule the next update after a delay of 1000 milliseconds (1 second)
                seekBarHandler.postDelayed(this, 1000);
            }
        }, 100);


        Intent intent = getIntent();
        if (intent.hasExtra("songPosition")) {
            int selectedSongPosition = intent.getIntExtra("songPosition", 0);
            currentSongIndex = selectedSongPosition;
            playSelectedSong();
        }

    }

    @SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        if (music.isPlaying()) {
            music.stop();
        }

        Intent intent = new Intent(this, activity_song_list.class);
        startActivity(intent);
        finish();
    }


    // Function to format time in minutes and seconds
    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


    // Function to apply rounded corners and a border to a bitmap
    private Bitmap getRoundedCornerBitmap(Bitmap bitmap, int radius, int borderColor, int borderWidth) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        // Anti-aliasing is added for smoother edges
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(borderColor);

        // Draw the outer border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        canvas.drawRoundRect(new RectF(rect), radius, radius, paint);

        // Draw the rounded image
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(new RectF(rect), radius, radius, paint);

        // Set the Xfermode to cut out the corners
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // Draw the original image onto the canvas
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}


