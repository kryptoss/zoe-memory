import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;




public class MemoryTest {

	 BufferedReader fromModule;
	 DataOutputStream toModule;
	 
	 ServerSocket srvr;
	 Socket clientSocket;
	int listenport= 999;
	int zoeport=9999;
	String zoeaddress="localhost";
	
	@Before
	public void setUp() throws Exception {
		 try {
			 srvr = new ServerSocket(listenport);
	         Socket skt = srvr.accept();
	         fromModule =
	         new BufferedReader(new InputStreamReader(skt.getInputStream()));
	 		 clientSocket = new Socket(zoeaddress, zoeport);
	 		 toModule = new DataOutputStream(clientSocket.getOutputStream());
		 }catch(Exception e){
			 return;
		 }
		 	
		
		
	}

	@After
	public void tearDown() throws Exception {
		
		srvr.close();
		clientSocket.close();
		
	}

	@Test
	public void testEcho() {
		String message="src=mt,dst=memory,command=remind";
		try {
			toModule.write(message.getBytes());
		} catch (IOException e) {
	
			e.printStackTrace();
		}
		String read="";
		String messageRet ="";
		try {
			while(((read=fromModule.readLine()) != null) && !srvr.isClosed()) {
			      messageRet=messageRet+read;
				
			}
			
		assertTrue(messageRet.contains("src=memory"));
			
		}catch(Exception e){
		fail("Not yet implemented");
		}
	}

	
	
}
