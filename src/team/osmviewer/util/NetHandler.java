package team.osmviewer.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import android.os.AsyncTask;

public class NetHandler {
	
	private static String IP = "192.168.1.2";
	private static int PORT = 43521;
	
//	b"HELLO"        :HELLO,
//  b"BYE"          :BYE,
//  b"RENAME"       :RENAME,
//  b"CREATE"       :CREATE,
//  b"REMOVE"       :REMOVE,
//  b"JOIN"         :JOIN,
//  b"LEAVE"        :LEAVE,
//  b"SET"          :SET,
//  b"START"        :START,
//  b"STOP"         :STOP,
//  b"READY"        :READY,
//  b"UNREADY"      :UNREADY,
//  b"RECONNECT"    :RECONNECT,
//  b"SEND"         :SEND,
//  b"GETGAMESETS"  :GETGAMESETS,
//  b"GETSETOPTIONS":GETSETOPTIONS,
//  b"GET"          :GET,
//  b"USERS"        :USERS,
//  b"DISCONNECT"   :DISCONNECT,
//  b"_USERS"       :_USERS,
//  b"_GAMES"       :_GAMES,
//  b"_TERMINATE"   :_TERMINATE
	
	private Socket socket = null;
	private DataOutputStream dataOutputStream = null;
	private DataInputStream dataInputStream = null;

	public NetHandler() {

		new Connect().execute();
	} 
	
	public int sendCommand(String command){
		new SendMessage(command).execute();
		return 0;
	}
	
	private class Connect extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				socket = new Socket(IP, PORT);
				dataOutputStream = new DataOutputStream(socket.getOutputStream());
				dataInputStream = new DataInputStream(socket.getInputStream());
			} catch (Exception e) {
				e.printStackTrace();
			} 
			return null;
		}
		
	}
	
	@SuppressWarnings("unused")
	private class Disconnect extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (socket != null)
					socket.close();
				
				if (dataInputStream != null)
					dataInputStream.close();
				
				if (dataOutputStream != null)
					dataOutputStream.close();
				
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
			return null;
		}
		
	}
	
	@SuppressWarnings("unused")
	private class SendMessage extends AsyncTask<Void, Void, String> {
		
		final String command;
		String result;
				
		public SendMessage(String command){
			this.command = command;
			System.out.println(command);
		}

		@Override
		protected String doInBackground(Void... params) {
			
			if (socket != null)
				if (dataInputStream != null)
					if (dataOutputStream != null){
						try {
							byte[] b = (command+"\r\n").getBytes("UTF-8");
							dataOutputStream.write(b);
							result = dataInputStream.readLine();
							System.out.println(result);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
			
			
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}

	}


}
