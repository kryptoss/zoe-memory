package zoe;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Adapter {
	

	 BufferedReader fromZoe;
	 DataOutputStream toZoe;
	 
	 ServerSocket srvr;
	 Socket clientSocket;
	
	 Object module;
	 

	


	private boolean connect(int listenport,String zoeaddress,int zoeport){
		 boolean success=true;
		 try {
			 srvr = new ServerSocket(listenport);
	         Socket skt = srvr.accept();
	         fromZoe =
	         new BufferedReader(new InputStreamReader(skt.getInputStream()));
	 		 clientSocket = new Socket(zoeaddress, zoeport);
	 		 toZoe = new DataOutputStream(clientSocket.getOutputStream());
		 }catch(Exception e){
			 success=false;
		 }
		 	
		 return success;
	}
	
	
	private boolean disconnect(){
		boolean success=true;
		try {
			srvr.close();
			clientSocket.close();
		} catch (IOException e) {
			success =false;
		}
		return success;
	}
	
	
	public boolean send(HashMap<String,String> message){
		boolean success =true;
		Iterator<Entry<String, String>> it = message.entrySet().iterator();
		String string="";
	    while (it.hasNext()) {
	        Map.Entry<String,String> pair = (Entry<String, String>)it.next();
	        string=string+";"+pair.getKey()+"="+pair.getValue(); 
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	    
	    try {
			toZoe.write(string.getBytes());
		} catch (IOException e) {
			success=false;
		}
	    
		return success;
	}
	
	
	public boolean registerListener(Module module){
		
		boolean success=true;
		
		this.module=module;
		
		String read;
		HashMap<String,String> message=new HashMap<String,String>();
	    try {
			while(((read=fromZoe.readLine()) != null) && !srvr.isClosed()) {
			      
				
				//dividir la cadena
				
				module.onMessage(message);
			}
		} catch (IOException e) {
			success =false;
		};
		
		return success;
	}
	
	
	
	
	public Adapter(int listenport, String zoeaddress, int zoeport){
		
		this.connect(listenport, zoeaddress, zoeport);
		
		

		
	}
	
	protected void finalize() throws Throwable {
		this.disconnect();
		
	}
	

}
