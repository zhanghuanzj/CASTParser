package storage.testDB;

import java.util.ArrayList;
import java.util.List;
/**
 * Test List<Class> toString()
 * @author think
 *
 */
public class TestElmts {

	public static void main(String[] args) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		List<Class> Parameter  = new ArrayList<Class>();
		Parameter.add(Class.forName("java.util.ArrayList"));
		Parameter.add(Class.forName("storage.testDB.TestElmts"));
		System.out.println(Parameter.get(0).getName());
		String s = Parameter.toString();
		System.out.println(s);
				
	}

}
