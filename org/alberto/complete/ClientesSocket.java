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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;

/**
 * @author jamartin
 *
 */
public class ClientesSocket {
	
	private static HashMap<Socket, PrintWriter> clientes = new HashMap<>();
	
	private ClientesSocket() {		
	}
	
	public static int nuevoCliente(Socket socket) {
		try {
			PrintWriter writer = new PrintWriter(socket.getOutputStream());
			clientes.put(socket, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return clientes.size();
	}
	
	public static int borrarCliente(Socket socket) {
		clientes.remove(socket);
		return clientes.size();
	}
	
	public static void enviarMensajeALosClientes(Socket socket, String mensaje) {
		Set<Socket> s = clientes.keySet();
		for(Socket so : s) {
			PrintWriter write = clientes.get(so);
			write.println(mensaje);
			write.flush();
		}
	}

}
