import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import zoe.Module;


public class Memory implements zoe.Module {

    Connection connection = null;
	
	
	@Override
	public void onMessage(HashMap<String, String> message) {
		
		String command= message.get("command");
		
		switch(command){
		
		case "note":
			break;
		case "remind":
			break;
		case "forget":
			break;
		
		}
		
		
		
		
		
		
		
	}


	private boolean note(HashMap<String,String> message){
		boolean success =true;
		Statement statement;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);// set timeout to 30 sec.
		} catch (SQLException e) {
			success=false;
		}  
		
		ResultSet rs;
		//if table exists
		try {
			rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' and name="+message.get("src"));
			if(!rs.next()){
				String sql="create table"+message.get("src")+" (";
				Iterator<Entry<String, String>> itr =message.entrySet().iterator();
				if (itr.hasNext()){
					Map.Entry item = (Map.Entry)itr.next();
					sql=sql+item.getKey()+" text ";
							itr.remove();
					
				}
				while(itr.hasNext()){
					sql=sql+", ";
					Map.Entry item = (Map.Entry)itr.next();
					sql=sql+item.getKey()+" text ";
					itr.remove();
				}
				sql=sql+")";
				try {
					statement.executeUpdate(sql);
				} catch (SQLException e) {
					success=false;
				}
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		
		//si existen todos los campos
		
		
		
		
		//se inserta
		
		return success;
	}
	
	
	public Memory(){
		this.initDB();
		
		
	}

private boolean initDB(){
	boolean success =true;
	try
	{
		Class.forName("org.sqlite.JDBC");



		// create a database connection
		connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
	}
	catch(SQLException e)
	{
		// if the error message is "out of memory", 
		// it probably means no database file is found
		System.err.println(e.getMessage());
	} catch (ClassNotFoundException e) {
		success=false;
	}
	finally
	{
		try
		{
			if(connection != null)
				connection.close();
		}
		catch(SQLException e)
		{
			success =false;
		}
	}

	return success;


}
}


class Main{
	
	public static void main(String args[]){
		int listenport=9999;
		String zoeaddress = "";
		int zoeport =999;
		zoe.Adapter adapter = new zoe.Adapter( listenport, zoeaddress, zoeport);
		
		

		Module mod = new Memory();
		adapter.registerListener(mod);
		
		
	}
	
	
	

}
