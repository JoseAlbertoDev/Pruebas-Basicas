/******************************************************************************
 * 
 *  $Id$
 * 
 * Copyright 2018 INGENIA S.A. All rights reserved.
 * 
 * $Date$ 
 * $Revision$
 * $URL$ 
 * $Author$ 
 * 
 * Fecha creación 21 mar. 2018
 * 
 * @autor jamartin
 *
 *
 * ***************************************************************************/
package org.alberto.complete;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

/**
 * @author jamartin
 *
 */
public class MusicaGUI {
	
	private String username;
	private Socket socket;
	private PrintWriter enviarMensaje;
	private BufferedReader recibirMensaje;
	private List<Boolean> jChecksActivos;
	private Color fondo = new Color(255, 240, 230);
	
	private String[] nombreInstrumentos = 
		{"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat","Acoustic Snare", "Crash Cymbal",};
	private int[] idInstrumento = {35, 42, 46, 38, 49};
	
	JFrame frame = new JFrame("Caja de música");
	JPanel main = new JPanel(new BorderLayout());
	
	Box boxNombreInstrumentos = new Box(BoxLayout.Y_AXIS);
	
	Box boxListaBotones = new Box(BoxLayout.Y_AXIS);
	JButton jButtonReproducir = new JButton("Play");
	
	JPanel chat = new JPanel();
	JTextArea textAreaChat = new JTextArea();
	JScrollPane scrollTextAreaChat = new JScrollPane(textAreaChat);
	
	JPanel jPanelTextoYBoton = new JPanel(new BorderLayout());
	JTextField textField = new JTextField();
	JButton jButtonEnviar = new JButton("Enviar");
	
	// GridLayout
	// cargarCheckBoxesTicks()
	JPanel combos;
	
	Sequencer sequencer;
	Sequence sequence;
	Track trackList;
	
	public MusicaGUI(String username) {
		this.username = username;
		System.out.println("Cargando UI con el usuario: " + this.username);
		loadNetwork();
		loadUi();
	}
	
	private void loadNetwork() {
		 try {
			socket = new Socket("192.168.19.33", 5555);
			enviarMensaje = new PrintWriter(socket.getOutputStream());
			InputStreamReader in = new InputStreamReader(socket.getInputStream());
			recibirMensaje = new BufferedReader(in);
			Runnable lector = new ReceptorMensaje();
			Thread threadLector = new Thread(lector);
			threadLector.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadUi() {
		loadMidi();
		configMain();
		loadChat();
		
		//frame.setSize(500, 400);
		frame.setLocation(600, 250);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(main);
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
	}
	
	private void loadMidi() {
		try {
			System.out.println("Cargando Midi");
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequencer.setTempoInBPM(120);
			sequence = new Sequence(Sequence.PPQ, 4);
			trackList = sequence.createTrack();
		} catch (MidiUnavailableException | InvalidMidiDataException m) {
			m.printStackTrace();
		}
	}
	
	private void configMain() {
		cargarListaInstrumentos();
		cargarCheckBoxesTicks();
		cargarBotones();
		
		main.add(BorderLayout.CENTER, combos);
		main.add(BorderLayout.WEST, boxNombreInstrumentos);
		main.add(BorderLayout.EAST, boxListaBotones);
		main.add(BorderLayout.SOUTH, chat);
		main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		main.setBackground(fondo);
	}
	
	private void loadChat() {
		textAreaChat.setEditable(false);
		textAreaChat.setLineWrap(true);
		textAreaChat.setWrapStyleWord(true);
		textAreaChat.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		textAreaChat.setRows(7);
		scrollTextAreaChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollTextAreaChat.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		//jButtonEnviar
		textField.setSize(new Dimension(100, 10));
		textField.addKeyListener(new JTextFieldEnviarKeyListener());
		jButtonEnviar.addActionListener(new JButtonEnviarActionListener());
		jPanelTextoYBoton.add(BorderLayout.CENTER, textField);
		jPanelTextoYBoton.add(BorderLayout.EAST, jButtonEnviar);
		
		BoxLayout box = new BoxLayout(chat, BoxLayout.Y_AXIS);
		chat.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
		chat.setLayout(box);
		chat.add(scrollTextAreaChat);
		chat.add(jPanelTextoYBoton);
		chat.setBackground(fondo);
		
	}
	
	private void cargarListaInstrumentos() {
		boxNombreInstrumentos.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		for(int i = 0; i < nombreInstrumentos.length; i++) {
			JLabel ins = new JLabel(nombreInstrumentos[i]);
			ins.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
			boxNombreInstrumentos.add(ins);
		}
	}
	
	private void cargarCheckBoxesTicks() {
		GridLayout grid = new GridLayout(5, 5, 0, 0);
		combos = new JPanel(grid);
		combos.setBackground(fondo);
		combos.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
		for(int i = 0; i < 25; i++) {
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			combos.add(c);
		}
	}
	
	private void cargarBotones() {
		jButtonReproducir.addActionListener(new JButtonReproducirActionListener());		
		boxListaBotones.add(jButtonReproducir);
	}
	
	class JButtonReproducirActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			crearLista();	
			try {
				sequencer.setSequence(sequence);
				sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
				sequencer.start();
				sequencer.setTempoInBPM(120);
			} catch (InvalidMidiDataException e1) {
				e1.printStackTrace();
			}		
		}
		
	}
	
	class JButtonEnviarActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			enviarTexto();
		}
		
	}
	
	class JTextFieldEnviarKeyListener implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {
			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == 10) {
				enviarTexto();
			}			
		}
		
	}
	
	private void enviarTexto() {
		enviarMensaje.println(username + ": " + textField.getText());
		enviarMensaje.flush();
		textField.setText("");
		textField.requestFocus();
	}
	
	private void crearLista() {
		sequence.deleteTrack(trackList);
		trackList = sequence.createTrack();
		jChecksActivos = new ArrayList<>();
		int lista[] = null;
		for (int i = 0; i < 5; i++) {
			lista = new int[5];
			int ins = idInstrumento[i];
			
			for (int j = 0; j < 5; j++) {
				JCheckBox jc = (JCheckBox) combos.getComponent(j+(i*5));
				jChecksActivos.add(jc.isSelected());
				if (jc.isSelected()) {
					lista[j] = ins;
				} else {
					lista[j] = 0;
				}
			}
			
			crearFila(lista);
			trackList.add(createEvent(176, 1, 127, 0, 5));
		}

		trackList.add(createEvent(192, 9, 1, 0, 4));
	}
	
	private void crearFila(int[] lista) {
		for (int i = 0; i < 5; i++) {
			int tecla = lista[i];
			if (tecla != 0) {
				trackList.add(createEvent(144,9,tecla,100,i));
				trackList.add(createEvent(128,9,tecla,100,i+1));
				
			}
		}
	}
	
	class ReceptorMensaje implements Runnable {

		@Override
		public void run() {
			String mensaje;
			try {
				while ((mensaje = recibirMensaje.readLine()) != null) {
					textAreaChat.append(mensaje + "\n");
				}
			} catch (IOException io) {
				io.printStackTrace();
			}
		}
		
		
		
	}
	
	public static MidiEvent createEvent(int command, int channel, int nota, int volumen, int tick) {
		MidiEvent event = null;
		try {
			ShortMessage m = new ShortMessage(command, channel, nota, volumen);
			event = new MidiEvent(m, tick);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		return event;
	}
	
	public static void main(String[] args) {
		new MusicaGUI(args[0]);
	}

}
