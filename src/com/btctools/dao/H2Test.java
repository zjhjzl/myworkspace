package com.btctools.dao;

import org.h2.tools.Server;  
import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.ResultSet;  
import java.sql.SQLException;  
import java.sql.Statement;

public class H2Test {

	public static void main(String[] args) {
		try {  
            Class.forName("org.h2.Driver");  
            Connection conn = DriverManager.getConnection("jdbc:h2:c:/test/test",  
                    "root", "");  
            Statement stat = conn.createStatement();  
            // insert data  
            //stat.execute("DROP TABLE IF EXISTS TEST");  
            //stat.execute("CREATE TABLE TEST(NAME VARCHAR)");  
            stat.execute("INSERT INTO TEST(ID, NAME)VALUES('3','Hello World')");  
            // use data  
            ResultSet result = stat.executeQuery("select name from test ");  
            int i = 1;  
            while (result.next()) {  
                System.out.println(i++ + ":" + result.getString("name"));  
            }  
            result.close();  
            stat.close();  
            conn.close();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  

	}

}
