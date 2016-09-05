package jKMS;

import java.awt.Desktop;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application extends WebMvcConfigurerAdapter {
	
	public static AppGui gui;

	public static void main(String[] args) {
		gui = new AppGui();
		
		if(portAvailable()) {
				
			URI index = null;

			SpringApplication.run(Application.class, args);
			
			try {
				index = new URI("http://localhost:8080/index");
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
	
			try {
				Desktop.getDesktop().browse(index);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			System.out.println();
			System.out.println("     _  ___  ____   ____    ____   ______   ");
			System.out.println("    (_)|_  ||_  _| |_   \\  /   _|.' ____ \\ ");
			System.out.println("    __   | |_/ /     |   \\/   |  | (___ \\_|");
			System.out.println("   [  |  |  __'.     | |\\  /| |  `_.____`. ");
			System.out.println(" _  | | _| |  \\ \\_  _| | \\/_| |_ | \\____) |");
			System.out.println("[ \\_| ||____||____||_____||_____| \\______.'");
		    System.out.println(" \\____/ 01101010 01001011 01001101 01010011");

			System.out.println();
			System.out.println("Pit Market 2.0 by Quiryn, freeDom, Konrad Kugelblitz, yangxinyu and siegmund42.");
			System.out.println();

		}
		else {
			LogicHelper.print("Port 8080 is already in use!", 2);
			gui.setError(LogicHelper.getLocalizedMessage("GUI.lblErrorPort"));
		}
		
	}
	
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }
 
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }
    
    @Bean 
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("256KB");
        factory.setMaxRequestSize("256KB");
        return factory.createMultipartConfig();
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
    
    public static boolean portAvailable(){
    	ServerSocket ss = null;
	    DatagramSocket ds = null;
	    
	    try {
	        ss = new ServerSocket(8080);
	        ss.setReuseAddress(true);
	        ds = new DatagramSocket(8080);
	        ds.setReuseAddress(true);
	        return true;
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }

	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                //s should not be thrown here
	            }
	        }
	    }

	    return false;
    }
    
}
