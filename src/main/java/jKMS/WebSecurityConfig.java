package jKMS;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jKMS.controller.ControllerHelper;
import jKMS.exceptionHelper.CreateFolderFailedException;

@Configuration
@EnableWebMvcSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	static PasswordEncoder bpe = new BCryptPasswordEncoder();
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
                .antMatchers("/contract**").hasRole("USER")
                .anyRequest().hasRole("PROF");
        http
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }

    protected static class UDS implements UserDetailsService	{

		@Override
		public UserDetails loadUserByUsername(String username)
				throws UsernameNotFoundException {

			// Array With first dimension for Users and second for their attributes.
			String[][] defaults = new String[2][3];
			defaults[0][0] = "Professor";
			defaults[0][1] = "Prof";
			defaults[0][2] = "ROLE_PROF";
			defaults[1][0] = "Assistant";
			defaults[1][1] = "Assi";
			defaults[1][2] = "ROLE_USER";
			
			try {
				ControllerHelper.createFolders();
			} catch (CreateFolderFailedException e1) {
				e1.printStackTrace();
			}
			
        	boolean found = true, noFile = false;
        	
        	User user = null;
        	
			// Path to password-config-file
        	String path = ControllerHelper.getFolderPath("settings") + LogicHelper.getLocalizedMessage("filename.settings") + ".txt";
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
	     			str.append(defaults[0][0] + ":").append(bpe.encode(defaults[0][1])).append(":" + defaults[0][2]).append(ln)
	     			   .append(defaults[1][0] + ":").append(bpe.encode(defaults[1][1])).append(":" + defaults[1][2]).append(ln);
	     			// Write to OutputStream
	     			fos.write(str.toString().getBytes());
	     			fos.close();
	     			LogicHelper.print("Wrote auth to config File");
	     			// Instantiate Reader
	    			br = new BufferedReader(new FileReader(path));
        		}	catch(IOException ioe)	{
        			// Something went wrong during writing to file
        			ioe.printStackTrace();
        			LogicHelper.print("Unable to create config File", 2);
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
            			LogicHelper.print("MATCH! Username: " + line.substring(0, line.indexOf(":")) + 
            					" Role: " + line.substring(line.lastIndexOf(":") + 1));
        				break;
        			}
        			// next line
        			line = br.readLine();
        		}
        	
            } catch(IOException e)	{
            	// Something went wrong during reading from file
    			LogicHelper.print("Unable to read from config File", 2);
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
        		LogicHelper.print("Wasn't able to find the user: " + username);
        		throw new UsernameNotFoundException("Wasn't able to find the user: " + username);
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
            authProvider.setPasswordEncoder(bpe);
            UserDetailsService uds = new UDS();
            authProvider.setUserDetailsService(uds);
        	auth.authenticationProvider(authProvider);
        }

    }

}
