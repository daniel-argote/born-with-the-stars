package entity;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class Sound {

    Clip clip;
    URL soundURL[] = new URL[30];
    boolean errorLogged[] = new boolean[30];
    FloatControl fc;
    public int volumeScale = 3;
    float volume;

    public Sound() {
        // Initialize sound paths (Ensure these files exist in res/sound/)
        soundURL[0] = getClass().getResource("/sound/theme.wav");
        soundURL[1] = getClass().getResource("/sound/swing.wav");
        soundURL[2] = getClass().getResource("/sound/hit.wav");
        soundURL[3] = getClass().getResource("/sound/thwack.wav"); // Arrow hit wall
        soundURL[4] = getClass().getResource("/sound/tink.wav");   // Arrow hit rock
        soundURL[5] = getClass().getResource("/sound/pickup.wav");
        soundURL[6] = getClass().getResource("/sound/break.wav");  // Tree/Rock destroyed
        soundURL[7] = getClass().getResource("/sound/rock_hit_1.wav");
        soundURL[8] = getClass().getResource("/sound/rock_hit_2.wav");
    }

    public void setFile(int i) {
        try {
            if (soundURL[i] == null) {
                if (!errorLogged[i]) {
                    System.out.println("Sound file missing at index: " + i + " (Check res/sound/)");
                    errorLogged[i] = true;
                }
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
            fc = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            checkVolume();
        } catch(Exception e) {
            System.out.println("Error loading sound index: " + i);
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null) {
            clip.start();
        }
    }

    public void loop() {
        if (clip != null) clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        if (clip != null) clip.stop();
    }
    
    public void checkVolume() {
        switch(volumeScale) {
            case 0: volume = -80f; break;
            case 1: volume = -20f; break;
            case 2: volume = -12f; break;
            case 3: volume = -5f; break;
            case 4: volume = 1f; break;
            case 5: volume = 6f; break;
        }
        if (fc != null) fc.setValue(volume);
    }
}