/*
 * photo-manager is a program to manage and organize your photos; Copyright (C) 2010 Dietrich Hentschel
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package pm.utilities;

import java.io.*;
import java.net.*;

/**
 * Daemon to provide a socket connection to a client.
 * 
 * <pre>
 * 
 *   From remote:    <method name>;<argument><new line>
 * 
 *   To remote:      <answer><new line>
 * 
 * 
 * 
 * </pre>
 * 
 * 
 * 
 * 
 */
public class PM_Demon {

	private Thread thread;
	private ServerSocket server;
	private final int PORT = 37433;
	private PM_RemoteAccess remote;
	private boolean readyForUse = false;
	private BufferedWriter out;
	private BufferedReader in;
	private boolean stop = false;
	private File topLevelPictureDirectory = null;
	private String albumName = "not-yet";
	/**
	 * Create the demon as singleton.
	 * 
	 */
	public PM_Demon() {

		init();
	}

	
	/**
	 * initConfigDone()
	 * 
	 * The configfiles init is done.
	 * You can get now the TLPD.
	 */
	synchronized public void initConfigDone() {
		albumName = PM_Configuration.getInstance().getAlbumName();
		topLevelPictureDirectory = PM_Configuration.getInstance().getTopLevelPictureDirectory();
	}
	
	
	/**
	 * Set ready for use
	 */
	public void setReadyForUse(boolean readyForUse) {

		System.out.println("Ready for use = " + readyForUse);
		this.readyForUse = readyForUse;
		if (readyForUse) {
			remote.init();
			
		}
	}

	/**
	 * Test if end
	 */
	public boolean getStop() {
		return stop;
	}

	/**
	 * Initialize the instance.
	 * 
	 */
	private void init() {
		remote = new PM_RemoteAccess();
		thread = new Thread() {
			public void run() {
				try {
					server = new ServerSocket(PORT);
				} catch (IOException e) {
					System.out
							.println("ERROR: daemon cant open ServerSocket port "
									+ PORT);
					return;
				}
				while (true) {
					Socket socket;
					try {
//						System.out.println("waiting for a connection on port "
//								+ PORT);
						// wait for a socket connection
						socket = server.accept();
						
						// busy waiting until the configfiles inti is done
						while (topLevelPictureDirectory == null) {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {}	
						}
					 
						
						
						
					} catch (IOException ex) {
						System.out.println("ex: " + ex);
						ex.printStackTrace();
						System.out.println("Close Server Socket");
						break;
					}
//					System.out.println("Server socket accepted. InetAdress = "
//							+ socket.getInetAddress() + ", localPort = "
//							+ socket.getLocalPort());
					try {
						out = new BufferedWriter(new OutputStreamWriter(socket
								.getOutputStream()));
						in = new BufferedReader(new InputStreamReader(socket
								.getInputStream()));
					} catch (IOException ex) {
						System.out.println("ex: " + ex);
						ex.printStackTrace();
						return;
					}
					acceptClient(in, out);
					try {
						in.close();
						out.close();
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} // while
			} // run
		}; // thread

		thread.start();
	}

	/**
	 * acceptClient()
	 * 
	 * Now the client can send a question and PM should responds it immediately
	 * 
	 * return: false --> close the s	boolean batch = PM_Configuration.getInstance().getBatch(); erverSocket due no accept messages from a
	 * client (i.e. terminate)
	 */
	private boolean acceptClient(BufferedReader in, BufferedWriter out) {
		try {
			String line = in.readLine();
			if (line == null) {
				System.out.println("ERROR: acceptClient. line = null");				
				return false;
			}

			if (line.indexOf("terminate") != -1) {
				System.out.println("Client terminate");
				sendMessage(out, "terminate received");
				stop = true;
				return false;
			}
			
			if (line.indexOf("status") != -1) {
//				System.out.println(line);
				if (readyForUse) {
					sendMessage(out, "up");
				} else {
					sendMessage(out, "loading");
				}
				return true;
			}

			if (line.indexOf("mode") != -1) {
//				System.out.println(line);
				
				boolean batch = PM_Configuration.getInstance().getBatch(); 
				
				if (batch) {
					sendMessage(out, "batch");
				} else {
					sendMessage(out, "gui");
				}
				return true;
			}
			

			if (!readyForUse) {
				sendMessage(out, "loading");
				return true;
			}
			sendMessage(out, getMessage(line));
		} catch (IOException ex) {
			System.out.println("ex: " + ex);
			ex.printStackTrace();
			return false;
		}

		return true;

	}

	/**
	 * Get a message from remote.
	 * 
	 * <pre>
	 *    <method name>;<argument>
	 * </pre>
	 * 
	 * Split the method name and call PM_RemoteAccess's invoke.
	 * 
	 */
	private String getMessage(String message) {
	//	System.out.println("..........  getMessage: >" + message + "<");
		String[] sa = message.split(";", 2);
		if (sa.length < 2) {
			String error =  "ERROR: delimeter ';' not found: message from remote: " + message;
			System.out.println(error);
			return error;
		}

		String ret = remote.invoke(sa[0], sa[1]);

		if (ret == null) {
			String error = "ERROR: unknown method name '" + sa[0] + "'";
			System.out.println(error);
			return error;
		}
		return ret;
	}

	/**
	 * Send a message to remote.
	 * 
	 * <pre>
	 *   <length (8 Bytes)><answer> 
	 * </pre>
	 * 
	 */
	private void sendMessage(BufferedWriter out, String message) {

		System.out.println("To remote: " + message);

		
		String to = albumName + ";" + message;
		
		int lg = to.length(); 
		String l = PM_Utils.stringToString("000000000000"
				+ Integer.toString(lg), 8);
		
		
		try {
			out.write(l);
			out.write(to);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}