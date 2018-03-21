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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author jamartin
 *
 */
public class ServidorChat {	
	
	ServerSocket server;	
	
	public ServidorChat() {
		try {
			start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	private void start() throws IOException {
		server = new ServerSocket(5555);
			while(true) {
				Socket socket = server.accept();
				int total = ClientesSocket.nuevoCliente(socket);
				System.out.println("Cantidad de clientes: " + total);
				Runnable buzon = new BuzonDeEntrada(socket);
				Thread threadBuzon = new Thread(buzon);
				threadBuzon.start();
			}
		
	}
	
	class BuzonDeEntrada implements Runnable {
		
		InputStreamReader in;
		BufferedReader reader;
		Socket socket;
		
		public BuzonDeEntrada (Socket socket) throws IOException {
			in = new InputStreamReader(socket.getInputStream());
			reader = new BufferedReader(in);
			this.socket = socket;
		}

		@Override
		public void run() {
			String mensaje;
			try {
				while((mensaje = reader.readLine()) != null) {
					ClientesSocket.enviarMensajeALosClientes(socket, mensaje);
				}
			} catch (SocketException e) {
				int clientes = ClientesSocket.borrarCliente(socket);
				System.out.println("Total de clientes: " + clientes);
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args) {
		new ServidorChat();
	}	

}
