import java.io.File;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundLoader {
	
	public static File sound;
	public static float value;
	public boolean is_playing = false;
	public static String ringtone = "lenovo default.wav";
	
	Clip clip;
	
	public SoundLoader() {
		value = 0;
	} 
	
	public void load() {
		sound = new File("res/ringtones/" + ringtone);
		System.out.println(ringtone);
	}
	
	public void play(File sound) {
		
		try {
			if(!is_playing) {
				clip = AudioSystem.getClip();
				clip.open(AudioSystem.getAudioInputStream(sound));
				clip.start();
				is_playing = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		clip.close();
		is_playing = false;
	}

}
