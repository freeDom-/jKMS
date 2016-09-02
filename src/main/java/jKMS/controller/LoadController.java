package jKMS.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jKMS.Kartoffelmarktspiel;
import jKMS.LogicHelper;
import jKMS.exceptionHelper.EmptyFileException;
import jKMS.exceptionHelper.FalseLoadFileException;
import jKMS.exceptionHelper.InvalidStateChangeException;
import jKMS.exceptionHelper.WrongAssistantCountException;
import jKMS.exceptionHelper.WrongFirstIDException;
import jKMS.exceptionHelper.WrongPackageException;
import jKMS.exceptionHelper.WrongPlayerCountException;
import jKMS.exceptionHelper.WrongRelativeDistributionException;
/**
 * Class for game load/exclude/start
 * @author siegmund42
 *
 */
@Controller
public class LoadController extends AbstractServerController {
	
	/**
	 * Displaying the IP/loadConfirm site
	 * @param	model	Model injection for displaying page
	 * @param	request	Request for determining Client's IP(s)
	 * @throws	InvalidStateChangeException
	 */
	@RequestMapping(value = "/load1", method = RequestMethod.GET)
	public String load1(Model model, ServletRequest request) throws InvalidStateChangeException	{
		
		if(ControllerHelper.stateHelper(kms, "load"))	{
			// Add some Attributes to display the Configuration of the game again
			model.addAttribute("customerConfiguration", kms.getbDistribution());
			model.addAttribute("salesmanConfiguration", kms.getsDistribution());
			model.addAttribute("cGroupQuantity", kms.getGroupCount("b"));
			model.addAttribute("sGroupQuantity", kms.getGroupCount("s"));
			
			// IP and Port for Client connection
			List<String> IPs = ControllerHelper.getIP();
			// Got Multiple IPs
			if(IPs.size() > 1)	{
				model.addAttribute("ipError", "multipleIP");
			}
			// Got No IP
			if(IPs.size() == 0)	{
				model.addAttribute("ipError", "noIP");
			}
			model.addAttribute("IPs", IPs);
			model.addAttribute("port", ControllerHelper.getPort(request));

			// Javascript - For number of Exclude Fields
			model.addAttribute("numberOfAssistants", kms.getAssistantCount());
			model.addAttribute("numberOfPlayers", kms.getPlayerCount());
			
			// JavaScript - For Quick-checking the given IDs
			model.addAttribute("firstID", kms.getConfiguration().getFirstID());
			
			model.addAttribute("currency", Kartoffelmarktspiel.getCurrency());
			
			return "load1";
		}	else	{
			return "redirect:/reset";
		}
		
	}
	
	/**
	 * For loading an existing Config.txt File
	 * @param	model	Model injection for displaying page
	 * @param	ra		RedirectAttributes injection for redirecting page
	 * @param	file	config file to load
	 * @throws	IllegalStateException
	 * @throws	InvalidStateChangeException
	 */
	@RequestMapping(value = "/load1", method = RequestMethod.POST)
	public String processIndex(Model model, RedirectAttributes ra, 
			@RequestParam("input-file") MultipartFile file) throws IllegalStateException, InvalidStateChangeException	{
		// State Change
		
		if(ControllerHelper.stateHelper(kms, "load"))	{
			if(file.getContentType().equals("text/plain"))	{
				
				// Load from File
				try {
					kms.getState().load(file);
				} 	catch(NumberFormatException | IOException | EmptyFileException | FalseLoadFileException e)	{
					// File empty/broken/something went wrong
					e.printStackTrace();
					model.addAttribute("message", LogicHelper.getLocalizedMessage("error.load.message"));
					model.addAttribute("error", LogicHelper.getLocalizedMessage("error.load.error"));
					return "standardException";
				}
				kms.getContracts().clear();
				// Everything loaded - Load Data into GUI
				return "redirect:/load1";
			}	else	{
				ra.addFlashAttribute("error", LogicHelper.getLocalizedMessage("error.load.falseContentType"));
				return "redirect:/index";
			}
		}	else	{
			return "redirect:/reset";
		}
		
	}
	
	/**
	 * Page for excluding Cards
	 * @param	model	Model injection for displaying page
	 * @throws	InvalidStateChangeException
	 */
	@RequestMapping(value = "/load2", method = RequestMethod.GET)
	public String load(Model model) throws InvalidStateChangeException	{

		// State Change
		if(ControllerHelper.stateHelper(kms, "load"))	{

			// Javascript - For number of Exclude Fields
			model.addAttribute("numberOfAssistants", kms.getAssistantCount());
			model.addAttribute("numberOfPlayers", kms.getPlayerCount());
			
			// JavaScript - For Quick-checking the given IDs
			model.addAttribute("firstID", kms.getConfiguration().getFirstID());
			model.addAttribute("lastID", kms.getLastId());
			model.addAttribute("currency", Kartoffelmarktspiel.getCurrency());
			
			return "load2";
		}	else	{
			return "redirect:/reset";
		}
		
	}
	
	/**
	 * Exclude Cards POST Request
	 * @param	model	Model injection for displaying page
	 * @param	ra		RedirectAttributes injection for redirecting page
	 * @param	exclude	Array of all IDs to exclude
	 * @param	check	List of all checkboxes
	 */
	@RequestMapping(value = "/load2", method = RequestMethod.POST)
	public String start(Model model, RedirectAttributes ra, 
			@RequestParam(value = "exclude[]", required = true) String exclude[], 
			@RequestParam(value = "check[]", required = false) List<String> check)	{
		
		// Check the validity BEFORE start of removing!! "Dry run"
		for(int i = 0; i < exclude.length; i++)	{
			// Check if not all cards where given out [Checkbox]
			if(check == null || check.indexOf(Integer.toString(i)) == -1)	{
				try	{
					if(exclude[i] != "")	{
						int number = Integer.parseInt(exclude[i]);
						// Check some things..
						if(number % 1 == 0 && number >= kms.getConfiguration().getFirstID() && 
								number <= kms.getLastId())	{
							// Exclude Card from Package
							if(!kms.getPackage(LogicHelper.IntToPackage(i)).contains(number))	{
								LogicHelper.print("Card with ID: " + number + " doesn't belongs to Package " + LogicHelper.IntToPackage(i));
								// Failed - ID not in assigned package 
								ra.addFlashAttribute("error", "exclude.package");
								return "redirect:/load2";
							}
						}	else	{
							// Checking Failed - number invalid
							ra.addFlashAttribute("error", "exclude.oob");
							return "redirect:/load2";
						}
					}	else	{
						// Field Empty
						ra.addFlashAttribute("error", "exclude.empty");
						return "redirect:/load2";
					}
				}	catch(NumberFormatException e)	{
					e.printStackTrace();
					ra.addFlashAttribute("error", "exclude.fraction");
					return "redirect:/load2";
				}
			}
		}
		
		// ######## REAL Excluding Part ###############
		for(int i = 0; i < exclude.length; i++)	{
			// Check if not all cards where given out [Checkbox]
			if(check == null || check.indexOf(Integer.toString(i)) == -1)	{
				LogicHelper.print("Excluding Cards from Package " + LogicHelper.IntToPackage(i));
				try	{
					if(exclude[i] != "")	{
						int number = Integer.parseInt(exclude[i]);
						// Check some things..
						if(number % 1 == 0 && number >= kms.getConfiguration().getFirstID() && 
								number <= kms.getLastId())	{
							// Exclude Card from Package
							try {
								kms.getState().removeCard(LogicHelper.IntToPackage(i), number);
							} catch (WrongPackageException e) {
								e.printStackTrace();
								// Failed - ID not in assigned package 
								ra.addFlashAttribute("error", "exclude.package");
								return "redirect:/load2";
							}
						}	else	{
							// Checking Failed - number invalid
							ra.addFlashAttribute("error", "exclude.oob");
							return "redirect:/load2";
						}
					}	else	{
						// Field Empty
						ra.addFlashAttribute("error", "exclude.empty");
						return "redirect:/load2";
					}
				// Catch Exceptions from removeCard() 
				}	catch (WrongPlayerCountException | WrongAssistantCountException
						| WrongFirstIDException
						| WrongRelativeDistributionException e) {
					e.printStackTrace();
					model.addAttribute("message", LogicHelper.getLocalizedMessage("error.exclude.exception.message"));
					model.addAttribute("error", LogicHelper.getLocalizedMessage("error.exclude.exception.error"));
					return "error";
				// Got a fractional Number?
				}	catch(NumberFormatException e)	{
					e.printStackTrace();
					ra.addFlashAttribute("error", "exclude.fraction");
					return "redirect:/load2";
				}
			}	else	{
				// Nothing to exclude
				LogicHelper.print("Nothing to exclude in Package " + LogicHelper.IntToPackage(i));
			}
		}
		// Finished all excluding -> Go to play
		return "redirect:/play";
	}
	
}
