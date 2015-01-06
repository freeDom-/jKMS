package jKMS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jKMS.controller.ControllerHelper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@EnableWebMvcSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
    @SuppressWarnings("deprecation")
	static PasswordEncoder pe = new Md5PasswordEncoder();
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/", "/css/**", "/js/**").permitAll()
                .antMatchers("/contract").hasRole("USER")
                .anyRequest().hasRole("PROF");
        http
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }

    @SuppressWarnings("deprecation")
    protected static class UDS implements UserDetailsService	{

		@Override
		public UserDetails loadUserByUsername(String username)
				throws UsernameNotFoundException {

			// Array With first dimension for Users and second for their attributes.
			String[][] defaults = new String[2][3];
			defaults[0][0] = "professor";
			defaults[0][1] = "prof";
			defaults[0][2] = "ROLE_PROF";
			defaults[1][0] = "assistant";
			defaults[1][1] = "assi";
			defaults[1][2] = "ROLE_USER";
			
        	boolean found = true, noFile = false;
        	
        	User user = null;
        	
			// Path to password-config-file
        	String path = ControllerHelper.getApplicationFolder() + "bin/config.txt";
        	BufferedReader br = null;
        	
        	try	{
    			br = new BufferedReader(new FileReader(path));
    		}  catch(FileNotFoundException e)	{
    			// ################## Create Config File ############################
        		LogicHelper.print("No config File found. Trying to create...", 1);
        		
        		try	{
        			FileOutputStream fos = new FileOutputStream(path);
	             	String ln = System.getProperty("line.separator");
	     			StringBuffer str = new StringBuffer();
	     			// Write to String Buffer
	     			str.append(defaults[0][0] + ":").append(pe.encodePassword(defaults[0][1], null)).append(":" + defaults[0][2]).append(ln)
	     			   .append(defaults[1][0] + ":").append(pe.encodePassword(defaults[1][1], null)).append(":" + defaults[1][2]).append(ln);
	     			// Write to OutputStream
	     			fos.write(str.toString().getBytes());
	     			fos.close();
	     			LogicHelper.print("Wrote auth to config.txt");
	     			// Instantiate Reader
	    			br = new BufferedReader(new FileReader(path));
        		}	catch(IOException ioe)	{
        			// Something went wrong during writing to file
        			ioe.printStackTrace();
        			LogicHelper.print("Unable to create config.txt", 2);
        			// File could not have been created
            		noFile = true;
        		}
     			
        	}
        	
        	// ############## Read from config File ################
        	try {
        		LogicHelper.print("Reading Login data from file.");
                String line = br.readLine();
                // Every line is a User
        		while(line != null)	{
        			// Found the right User?
        			if(line.substring(0, line.indexOf(":")).equals(username))	{
        				// Create a User Object
        				Set<SimpleGrantedAuthority> set = new HashSet<>();
        				set.add(new SimpleGrantedAuthority(line.substring(line.lastIndexOf(":") + 1)));
        				user = new User(line.substring(0, line.indexOf(":")), 
        						line.substring(line.indexOf(":") + 1, line.lastIndexOf(":")), 
        						set);
            			LogicHelper.print("Username: " + line.substring(0, line.indexOf(":")));
            			LogicHelper.print("Password: " + line.substring(line.indexOf(":") + 1, line.lastIndexOf(":")));
            			LogicHelper.print("Role: " + line.substring(line.lastIndexOf(":") + 1));
        				break;
        			}
        			// next line
        			line = br.readLine();
        		}
        	
            } catch(IOException e)	{
            	// Something went wrong during reading from file
    			LogicHelper.print("Unable to read from config.txt", 2);
    			found = false;
            } finally {
            	if(!noFile)	{
            		LogicHelper.print("Finished reading.");
            		try	{
            			br.close();
            		} catch(IOException e)	{
            			e.printStackTrace();
            		}
            	}
    		}
        	
        	if(noFile || !found)	{
        		// File not created/found
        		LogicHelper.print("File could not have been created or found.");
        		int i = 2;
        		if(username == defaults[0][0])	{
        			i = 0;
        		}
        		if(username == defaults[1][0])	{
        			i = 1;
        		}
				Set<SimpleGrantedAuthority> set = new HashSet<>();
				set.add(new SimpleGrantedAuthority(defaults[i][2]));
				user = new User(defaults[i][0], 
						defaults[i][1], 
						set);
        	}
        	
        	if(user == null)	{
        		LogicHelper.print("Wasn't able to find the username: " + username);
        		throw new UsernameNotFoundException("Wasn't able to find the username: " + username);
        	}
        	
			return user;
		}
    	
    }
    
    @Configuration
    protected static class AuthenticationConfiguration extends
            GlobalAuthenticationConfigurerAdapter	{    	
    	
		@Override
        public void init(AuthenticationManagerBuilder auth) throws Exception {
        	
            DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
            authProvider.setPasswordEncoder(pe);
            UserDetailsService uds = new UDS();
            authProvider.setUserDetailsService(uds);
        	auth.authenticationProvider(authProvider);
        }

    }

}
