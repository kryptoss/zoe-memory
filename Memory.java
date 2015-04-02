import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

		message.remove("command");
		boolean success =false;
		HashMap<String, String> answer=new HashMap<String, String>();
		
		switch(command){

		case "note":
			success=this.note(message);
			answer.put("success",Boolean.toString(success));
			break;
		case "remind":
			answer=this.remind(message);
			
			break;
		case "forget":
			success=this.forget(message);		
			answer.put("success",Boolean.toString(success));
			break;

		}

	 
		answer.put("dst",message.get("src"));
		answer.put("src","memory");		
		answer.put("_cid",message.get("src"));	



	}


	private boolean tableExistsOrCreate(Statement statement, String table, boolean create, HashMap<String,String> message){

		boolean exists=true;

		try{

			ResultSet TablesWithRequiredName = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' and name="+table);
			if(!TablesWithRequiredName.next()){
				exists=false;
			}


			if(create){
				String sql="create table"+message.get("src")+" (";
				Iterator<Entry<String, String>> messageFields =message.entrySet().iterator();
				if (messageFields.hasNext()){
					Map.Entry<String,String> item = (Map.Entry<String,String>)messageFields.next();
					sql=sql+item.getKey()+" text ";

				}
				while(messageFields.hasNext()){
					sql=sql+", ";
					Map.Entry<String,String> item = (Map.Entry<String,String>)messageFields.next();
					sql=sql+item.getKey()+" text ";
				}
				sql=sql+")";
				statement.executeUpdate(sql);




			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			exists=false;
		}

		return exists;

	}


	private ArrayList<String> CheckFieldsExists(HashMap<String,String> message,String table,Statement statement ){

		Iterator<Entry<String, String>> messageFields =message.entrySet().iterator();

		ArrayList<String> fields= this.getFieldsFromBBDD(table, statement);


		ArrayList<String> fieldsToAdd = new ArrayList<String>();
		while(messageFields.hasNext()){
			String field = messageFields.next().getKey();
			if(!fields.contains(field)) fieldsToAdd.add(field);

		}

		return fieldsToAdd;

	}


	private ArrayList<String>  getFieldsFromBBDD(String table, Statement statement){

		String getFieldsStm = "select * from "+table+" LIMIT 0";
		ResultSet rs;
		ArrayList<String> fields = new ArrayList<String>();
		try{
			rs = statement.executeQuery(getFieldsStm);

			ResultSetMetaData tableFieldsData = rs.getMetaData();

			for(int i = 1; i <= tableFieldsData.getColumnCount(); i++)
			{
				fields.add(tableFieldsData.getColumnLabel(i));
			}


		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return fields;
	}




	private HashMap<String,String> remind(HashMap<String,String> message){
		HashMap<String, String> result = new HashMap<String, String>();
		Statement statement;
		boolean success=false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);// set timeout to 30 sec.
		} catch (SQLException e) {
			
			result.put("success",Boolean.toString(success));
			return result;
		}  


		String table=message.get("src");

		boolean createIfNotExists=false;
		//if table exists
		if(this.tableExistsOrCreate(statement, table, createIfNotExists, message)){		
			Iterator<Entry<String, String>> insertItr = message.entrySet().iterator();
			Map.Entry<String,String> item = (Map.Entry<String,String>)insertItr.next();
			String queryDataSQL="SELECT * FROM "+table+" WHERE "+item.getKey()+"= "+item.getValue();	
			if(insertItr.hasNext()) queryDataSQL= queryDataSQL+", ";
			while(insertItr.hasNext()){
				item = (Map.Entry<String,String>)insertItr.next();
				queryDataSQL=queryDataSQL+item.getKey()+"="+item.getValue();
				if(insertItr.hasNext()){
					queryDataSQL= queryDataSQL+", ";
				}
			}
		}	
		
		success=true;
		result.put("success",Boolean.toString(success));
		return result;
		
		
		
		
	}
	

	private boolean forget(HashMap<String,String> message){
		boolean success = true;
		Statement statement;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);// set timeout to 30 sec.
		} catch (SQLException e) {
			success=false;
			return success;
		}  


		String table=message.get("src");

		boolean createIfNotExists=false;
		//if table exists
		if(this.tableExistsOrCreate(statement, table, createIfNotExists, message)){		
			Iterator<Entry<String, String>> insertItr = message.entrySet().iterator();
			Map.Entry<String,String> item = (Map.Entry<String,String>)insertItr.next();
			String deleteDataSQL="DELETE FROM "+table+" WHERE "+item.getKey()+"= "+item.getValue();	
			if(insertItr.hasNext()) deleteDataSQL= deleteDataSQL+", ";
			while(insertItr.hasNext()){
				item = (Map.Entry<String,String>)insertItr.next();
				deleteDataSQL=deleteDataSQL+item.getKey()+"="+item.getValue();
				if(insertItr.hasNext()){
					deleteDataSQL= deleteDataSQL+", ";
				}
			}
		}	

		return success;
	}




	private boolean note(HashMap<String,String> message){
		boolean success =true;
		Statement statement;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);// set timeout to 30 sec.
		} catch (SQLException e) {
			success=false;
			return success;
		}  


		String table=message.get("src");


		//if table exists
		try {

			boolean createIfNotExists=true;
			this.tableExistsOrCreate(statement, table, createIfNotExists, message);

			int returnValue=0;


			//check all fields exists

			ArrayList<String> fieldsToAdd= this.CheckFieldsExists(message, table, statement);

			//add missing fields


			Iterator<String> fieldsItr = fieldsToAdd.iterator();

			while(fieldsItr.hasNext()){
				String addColumnSQL = "ALTER TABLE"+ table+ "ADD COLUMN "+fieldsItr.next()+" text";
				returnValue = statement.executeUpdate(addColumnSQL);
			}




			//insert data


			Iterator<Entry<String, String>> insertItr = message.entrySet().iterator();
			Map.Entry<String,String> item = (Map.Entry<String,String>)insertItr.next();
			String insertDataSQL="INSERT INTO "+table+" ("+item.getKey();

			String values =(String) item.getValue();	
			if(insertItr.hasNext()){
				insertDataSQL= insertDataSQL+", ";
				values=values+", ";
			}
			while(insertItr.hasNext()){
				item = (Map.Entry<String,String>)insertItr.next();
				insertDataSQL=insertDataSQL+item.getKey();
				values=values+item.getValue();
				if(insertItr.hasNext()){
					insertDataSQL= insertDataSQL+", ";
					values=values+", ";
				}
			}

			insertDataSQL= insertDataSQL+") + VALUES ("+values+");";

			returnValue = statement.executeUpdate(insertDataSQL);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			success=false;
		}


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
			connection = DriverManager.getConnection("jdbc:sqlite:memory.db");
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
