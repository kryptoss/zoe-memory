package zoe;

import java.util.HashMap;

public interface Module {

	public void onMessage(HashMap<String,String> message);
	
}
