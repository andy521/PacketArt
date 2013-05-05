package jp.ac.kansai_u.kutc.firefly.packetArt.music;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

public class MusicPlayer {
	public static boolean judgetone;
	public static int velocity;
	public static int length;
	public static Sequencer sequencer;
	public static Sequence sequence;
	
	public static void main(String[] args) throws InvalidMidiDataException{
		playMusic(70, 300, false);
	}
	
	public static void playMusic(int velocity, int length, boolean judgetone) throws InvalidMidiDataException{
		Sequence sequence = new Sequence(Sequence.PPQ, 24, 3);
		if(judgetone == true){
			System.out.print("Make Cheerful Song.\r\n");
			try{
				MelodyMaker.setCheerfulMelody(sequence, length, velocity);
				AccompanimentMaker.makeCheerfulAccompaniment(sequence, length, velocity);
				sequencer = MidiSystem.getSequencer();
				sequencer.open();
				sequencer.setSequence(sequence);
				sequencer.start();
				while(sequencer.isRunning()) Thread.sleep(100);
			}catch(InterruptedException e){
				e.printStackTrace();
			}catch(MidiUnavailableException e){
				e.printStackTrace();
			}catch(InvalidMidiDataException e){
				e.printStackTrace();
			}finally{
				if(sequencer != null && sequencer.isOpen()) sequencer.close();
			}
		}else if(judgetone == false){
			System.out.print("Make Gloomy Song.\r\n");
			try{
				MelodyMaker.setGloomyMelody(sequence, length, velocity);
				AccompanimentMaker.makeGloomyAccompaniment(sequence, length, velocity);
				sequencer = MidiSystem.getSequencer();
				sequencer.open();
				sequencer.setSequence(sequence);
				sequencer.start();
				while(sequencer.isRunning()) Thread.sleep(100);
			}catch(InterruptedException e){
				e.printStackTrace();
			}catch(MidiUnavailableException e){
				e.printStackTrace();
			}catch(InvalidMidiDataException e){
				e.printStackTrace();
			}finally{
				if(sequencer != null && sequencer.isOpen()) sequencer.close();
			}
		}
	}
}

