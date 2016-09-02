package jKMS.controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jKMS.Application;
import jKMS.Kartoffelmarktspiel;
import jKMS.LogicHelper;
import jKMS.exceptionHelper.CreateFolderFailedException;
import jKMS.states.Evaluation;
import jKMS.states.Load;
import jKMS.states.Play;
import jKMS.states.Preparation;
/**
 * 
 * Class for non categorizable server side requests
 * @author freeDom
 * @author siegmund42
 *
 */
@Controller
public class ServerController extends AbstractServerController	{
	
	/**
	 * Display main menu
	 * @param	model	Model injection
	 * @param	lang	language param if set
	 * @return	Name of template
	 * @throws	CreateFolderFailedException
	 */
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index(Model model,
			@RequestParam(value = "lang", required = false) final String lang) throws CreateFolderFailedException	{
		
		if(lang != null) Application.gui.changeLanguage();
		ControllerHelper.checkFolders();
		model.addAttribute("languages", ControllerHelper.getLanguages());
		model.addAttribute("falseContentType", LogicHelper.getLocalizedMessage("error.load.falseContentType"));
		return "index";
	}
	
	/**
	 * Display options page
	 * @param	model	Model injection
	 * @param	lang	language param if set
	 * @return	Name of template
	 * @throws	IOException
	 */
	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	public String settigs(Model model, @RequestParam(value = "lang", required = false) final String lang) throws IOException	{
		// Check if language was changed
		if(lang != null) Application.gui.changeLanguage();
		
		model.addAttribute("users", ControllerHelper.getUsers());
		model.addAttribute("languages", ControllerHelper.getLanguages());
		model.addAttribute("currCurrency", Kartoffelmarktspiel.getCurrency());
		model.addAttribute("currencies", kms.getCurrencys());
		return "settings";
	}
	
	@RequestMapping(value = "/currencySettings", method = RequestMethod.POST)
	public String processSettings(Model model, RedirectAttributes ra,
			@RequestParam(value = "currencies") int currency)	{
		kms.setCurrency(currency);
		ra.addFlashAttribute("success", 42);
		return "redirect:/settings";
	}
	
	/**
	 * Process values from options page
	 * @param	model	Model injection
	 * @param	ra		RedirectAttributes injection for redirecting if error
	 * @return	Name of template
	 * @throws	IOException
	 */
	@RequestMapping(value = "/settings", method = RequestMethod.POST)
	public String processSettings(Model model, RedirectAttributes ra, 
			@RequestParam(value = "users") String username,
			@RequestParam(value = "oldPass") String oldPass,
			@RequestParam(value = "pass1") String pass1,
			@RequestParam(value = "pass2") String pass2) throws IOException	{

		// Path to password-config-file
		String path = ControllerHelper.getFolderPath("settings") + LogicHelper.getLocalizedMessage("filename.settings") + ".txt";
    	FileReader fr = null;
		try {
			fr = new FileReader(path);
		} catch (FileNotFoundException e) {
			LogicHelper.print("Config file not found.", 2);
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		
		PasswordEncoder bpe = new BCryptPasswordEncoder();
		
		LogicHelper.print("Reading Login data from file.");
		StringBuffer str = new StringBuffer();
     	String ln = System.getProperty("line.separator");
        String line = br.readLine();
        String error = "";
        
        // Every line is a User
		while(line != null)	{
			// Found the right User?
			if(line.substring(0, line.indexOf(":")).equals(username))	{
				// Only if inserted the right password
				if(bpe.matches(oldPass, line.substring(line.indexOf(":") + 1, line.lastIndexOf(":"))))	{
					// Only if password and repeat of it are equal
					if(pass1.equals(pass2))	{
						str.append(username).append(":")
							.append(bpe.encode(pass1)).append(":")
							.append(line.substring(line.lastIndexOf(":") + 1))
							.append(ln);
		    			LogicHelper.print("EDIT: Username = " + username + " Password = " + bpe.encode(pass1) + " Role = " + line.substring(line.lastIndexOf(":") + 1));
					}	else	{
						error = "password.unequal";
					}
				}	else	{
					error = "password.invalid";
				}
			}	else	{
				str.append(line).append(ln); 
			}
			// next line
			line = br.readLine();
		}
		
		if(str.length() == 0)
			error = "user.notFound";
		
		br.close();
		
		if(error == "")	{
			// Write to OutputStream [File]
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(str.toString().getBytes());
			fos.close();
			ra.addFlashAttribute("success", 42);
			LogicHelper.print("Updated auth in config.txt");
		}	else	{
			ra.addFlashAttribute("error", error);
		}
		
		return "redirect:/settings";
	}
	
	/**
	 * Display a reset page [e.g. State change Evaluate -> Prepare]
	 * @return	Name of template
	 */
	@RequestMapping(value = "/reset", method = RequestMethod.GET)
	public String reset()	{
		return "reset";
	}
	
	/**
	 * Do the reset
	 * @return	Name of template
	 */
	@RequestMapping(value = "/reset", method = RequestMethod.POST)
	public String processReset()	{
		kms.prepare();
		LogicHelper.print("Reseted all data.");
		return "redirect:/prepare1";
	}
	
	/**
	 * Auto redirect method when coming from GUI by pressing opne browser button
	 * @return	the correct template redirect
	 */
	@RequestMapping("/autoRedirect")
	public String autoRedirect(){
		if(kms.getState() instanceof Preparation) return "redirect:/prepare1";
		else if(kms.getState() instanceof Load) return "redirect:/load1";
		else if(kms.getState() instanceof Play) return "redirect:/play";
		else if(kms.getState() instanceof Evaluation) return "redirect:/evaluate";
		else return "redirect:/index";
	}
}
