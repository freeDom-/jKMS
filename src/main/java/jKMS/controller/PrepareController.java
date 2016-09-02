package jKMS.controller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jKMS.Amount;
import jKMS.Kartoffelmarktspiel;
import jKMS.LogicHelper;
import jKMS.exceptionHelper.CreateFolderFailedException;
import jKMS.exceptionHelper.EmptyFileException;
import jKMS.exceptionHelper.FalseLoadFileException;
import jKMS.exceptionHelper.InvalidStateChangeException;
import jKMS.exceptionHelper.NoIntersectionException;
import jKMS.exceptionHelper.WrongAssistantCountException;
import jKMS.exceptionHelper.WrongFirstIDException;
import jKMS.exceptionHelper.WrongPlayerCountException;
import jKMS.exceptionHelper.WrongRelativeDistributionException;

/**
 * Class for all preparing game functions
 * @author siegmund42
 *
 */
@Controller
public class PrepareController extends AbstractServerController {
	
	@Autowired
	ServletContext servletContext;
	
	/**
	 *  Get Requests to first Site of Preparation - Metadata-Input
	 *  
	 *  @param 	model Model of Springs MVC
	 *  @return view name to be handled by Spring
	 *  @throws InvalidStateChangeException thrown by invalid change of State
	 */
	@RequestMapping(value = "/prepare1", method = RequestMethod.GET)
	public String prepare1(Model model) throws InvalidStateChangeException	{
		
		// STATE-CHANGE
		if(ControllerHelper.stateHelper(kms, "prepare"))	{
			// Add Metadata if already set
			model.addAttribute("firstID", kms.getConfiguration().getFirstID());
			model.addAttribute("lastID", kms.getLastId());
			model.addAttribute("numberOfPlayers", kms.getPlayerCount());
			model.addAttribute("numberOfAssistants", kms.getAssistantCount());
			return "prepare1";
		}	else	{
			return "redirect:/reset";
		}

	}
	
	/**
	 *  Handling of Metadata - POST Requests on first Site of Preparation
	 *  
	 *  @param 	model 	Model of Springs MVC
	 *  @param 	ra		RedirectAttributes injection for redirecting after errors
	 *  @param	numberOfPlayers
	 *  @param	numberOfAssistants
	 *  @param	configuration	determines if user wanted to use standardconfig/create empty only redirect
	 *  @return 		view name to be handled by Spring
	 */
	@RequestMapping(value = "/prepare1", method = RequestMethod.POST)
	public String processPrepare1(Model model, RedirectAttributes ra,
		 	@RequestParam(value="players", required = true) String numberOfPlayers, 
	        @RequestParam(value="assistants", required = true) String numberOfAssistants,
	        @RequestParam(value="c", required = false) String configuration)	{
		
		// Check some things to ensure Data are valid
		if(numberOfPlayers != "" && numberOfAssistants != "")	{
			int players, assistants;
			try	{
				players = Integer.parseInt(numberOfPlayers);
				assistants = Integer.parseInt(numberOfAssistants);
			}	catch(NumberFormatException e)	{
				e.printStackTrace();
				ra.addFlashAttribute("error", "fraction");
				return "redirect:/prepare1";
			}
			
			if(players > 0 && players <= 8999 && players % 2 == 0 && 
					assistants > 0 && assistants <= 26 && assistants % 1 == 0)	{
				// Store Metadata in Logic
				kms.getState().setBasicConfig(players, assistants);
				ra.addFlashAttribute("c", configuration);
				return "redirect:/prepare2";
			}
			
		}
		
		// Add given Values to model if something was wrong
		ra.addFlashAttribute("numberOfPlayers", numberOfPlayers);
		ra.addFlashAttribute("numberOfAssistants", numberOfAssistants);
		ra.addFlashAttribute("error", "config");
		return "redirect:/prepare1";
	}

	/**
	 *  GET Requests on Site for Distribution
	 *  @param	model	Model injection
	 *  @param	c		determines if user wanted to use standardconfig/create empty
	 *  				Values: load, standard, own
	 *  @throws			IllegalStateException
	 *  @throws			InvalidStateChangeException
	 */
	@RequestMapping(value = "/prepare2", method = RequestMethod.GET)
	public String prepare2(Model model,
			@RequestParam(value = "c", required = false) String c) throws IllegalStateException, InvalidStateChangeException	{
		
		String configuration;
		
		if(c == null)
			configuration = (String) model.asMap().get("c");
		else
			configuration = c;
		
		// STATE-CHANGE
		if(ControllerHelper.stateHelper(kms, "prepare"))	{
			
			// Only show the already stored values
			if(configuration == null || configuration.equals("null"))	{
				configuration = "load";
			}
			
			// Load values if should be loaded/standard configuration or some values are already stored
			if(configuration.equals("load") || configuration.equals("standard"))	{
				if(configuration.equals("standard"))	{
					// Load Standard Distribution
					kms.getState().loadStandardDistribution();
					model.addAttribute("isStandard", true);
				}	else	{
					model.addAttribute("isStandard", false);
				}
				// Add stored values to model
				model.addAttribute("customerConfiguration", kms.getbDistribution());
				model.addAttribute("salesmanConfiguration", kms.getsDistribution());
				model.addAttribute("sGroupQuantity", kms.getGroupCount("s"));
				model.addAttribute("cGroupQuantity", kms.getGroupCount("b"));
			}	else	{
				// Create own configuration - Fields are empty
				kms.getbDistribution().clear();
				kms.getsDistribution().clear();
				model.addAttribute("addEmptyRows", 1);
				model.addAttribute("sGroupQuantity", 0);
				model.addAttribute("cGroupQuantity", 0);
			}
			
			model.addAttribute("numberOfPlayers", kms.getPlayerCount());
			model.addAttribute("numberOfAssistants", kms.getAssistantCount());
			model.addAttribute("firstID", kms.getConfiguration().getFirstID());
			model.addAttribute("lastID", kms.getLastId());
			model.addAttribute("falseContentType", LogicHelper.getLocalizedMessage("error.load.falseContentType"));
			model.addAttribute("currency", Kartoffelmarktspiel.getCurrency());
	
			return "prepare2";
		}	else	{
			return "redirect:/reset";
		}
	}
	
	/**
	 *  POST Request on Distribution-Site -&gt; Loading values from File, 
	 *  display them by redirecting to "prepare2"
	 *  @param	ra		RedirectAttributes injection for redirecting
	 *  @param	file	config file to load in preparation
	 *  @throws			IllegalStateException
	 *  @throws			InvalidStateChangeException
	 */
	@RequestMapping(value = "/prepare2", method = RequestMethod.POST)
	public String loadConfig(RedirectAttributes ra,
			@RequestParam("input-file") MultipartFile file) throws IllegalStateException, InvalidStateChangeException	{
		if(ControllerHelper.stateHelper(kms, "prepare"))	{
			if(file.getContentType().equals("text/plain"))	{
				try {
					kms.getState().load(file);
				} 	catch(NumberFormatException | IOException | EmptyFileException | FalseLoadFileException e)	{
					// File empty/broken/something went wrong
					e.printStackTrace();
					ra.addFlashAttribute("error", "load.message");
				}
			}	else	{
				ra.addFlashAttribute("error", "load.falseContentType");
			}
			return "redirect:/prepare2";
		}	else	{
			return "redirect:/reset";
		}
	}

	/**
	 * Get Requests on save site
	 *  @param	model	Model injection
	 *  @return	Template name
	 * 
	 */
	@RequestMapping(value = "save", method = RequestMethod.GET)
	public String getSave(Model model)	{
		String path = ControllerHelper.getFolderPath("config");

		// Add path to model
		if(!model.containsAttribute("configSavePath"))
			model.addAttribute("configSavePath", path);
		return "save";
	}
	
	/**
	 *  Processes Posted Values from Distribution-Site
	 *  @param	model	Model injection
	 *  @param	ra		RedirectAttributes injection for redirecting
	 *  @param	cRelativeQuantity	Array for all relative Quantities of Customers
	 *  @param	cPrice				Array for all relative Quantities of Customers
	 *  @param	cAbsoluteQuantity	Array for all relative Quantities of Customers
	 *  @param	sRelativeQuantity	Array for all relative Quantities of Salesman
	 *  @param	sPrice				Array for all relative Quantities of Salesman
	 *  @param	sAbsoluteQuantity	Array for all relative Quantities of Salesman
	 *  @throws	CreateFolderFailedException
	*/
	@RequestMapping(value = "save", method = RequestMethod.POST)
	public String save(	Model model, RedirectAttributes ra,
							@RequestParam(value = "cRelativeQuantity[]") String[] cRelativeQuantity,
							@RequestParam(value = "cPrice[]") String[] cPrice,
							@RequestParam(value = "cAbsoluteQuantity[]") String[] cAbsoluteQuantity,
							@RequestParam(value = "sRelativeQuantity[]") String[] sRelativeQuantity,
							@RequestParam(value = "sPrice[]") String[] sPrice,
							@RequestParam(value = "sAbsoluteQuantity[]") String[] sAbsoluteQuantity)
						throws CreateFolderFailedException	{
		
		int i;
		String error = "";
		
		// Delete current Maps so they can be completely rewritten.
		kms.getbDistribution().clear();
		kms.getsDistribution().clear();
		
		// Register all buyer
		for(i = 0; i < cRelativeQuantity.length; i++)	{
			// Check some things to ensure that data are valid
			if(cPrice[i] != "" && cRelativeQuantity[i] != "" && cAbsoluteQuantity[i] != "")	{
			
				try	{
					int cP = Integer.parseInt(cPrice[i]);
					int cR = Integer.parseInt(cRelativeQuantity[i]);
					int cA = Integer.parseInt(cAbsoluteQuantity[i]);
					
					// Checking again....
					if(	cP > 0 && cP % 1 == 0 && 
							cR > 0 && cR <= 100 && cR % 1 == 0 &&
							cA > 0 && cA <= kms.getPlayerCount() && cA % 1 == 0)	{
						
						// Data valid -> store in Logic
						kms.getState().newGroup(true, cP, cR, cA);
					
					}	else	{
						error = "save";
						break;
					}
					
				}	catch(NumberFormatException e)	{
					e.printStackTrace();
					error = "save.fraction";
					break;
				}
				
			}	else	{
				error = "save.empty";
				break;
			}
		}
		
		if(error != "")
			error = "buyer." + error;
		else	{	
			// Register all Seller
			for(i = 0; i < sRelativeQuantity.length; i++)	{
				
				// Check some things to ensure that data are valid
				if(sPrice[i] != "" && sRelativeQuantity[i] != "" && sAbsoluteQuantity[i] != "")	{
				
					try	{
						int sP = Integer.parseInt(sPrice[i]);
						int sR = Integer.parseInt(sRelativeQuantity[i]);
						int sA = Integer.parseInt(sAbsoluteQuantity[i]);
						
						// Checking again....
						if(sP > 0 && sP % 1 == 0 && 
								sR > 0 && sR <= 100 && sR % 1 == 0 &&
								sA > 0 && sA <= kms.getPlayerCount() && sA % 1 == 0)	{
							
							// Data valid -> store in Logic
							kms.getState().newGroup(false, sP, sR, sA);
						
						}	else	{
							error = "save";
							break;
						}
						
					}	catch(NumberFormatException e)	{
						e.printStackTrace();
						error = "save.fraction";
						break;
					}
					
				}	else	{
					error = "save.empty";
					break;
				}
			}
			if(error != "")
				error = "seller." + error;
		}
		
		// Check if actual amount of players is exceeding 8999
		if((LogicHelper.getAbsoluteSum(kms.getbDistribution()) + LogicHelper.getAbsoluteSum(kms.getsDistribution())) > 8999)	{
			error = "config.playerCountOV";
		}
	
		// ######################################### Intersection-check BEGIN ############################################
		// Check if there is an intersection of supply and demand function, if not -> user is requested to correct his distributions
		try {
			TreeMap<Integer,Amount> sDistr = (TreeMap<Integer, Amount>) kms.getsDistribution();
			TreeMap<Integer, Amount> bDistr = (TreeMap<Integer, Amount>) kms.getbDistribution();
			
			int bCount = 0, sCount = 0, bTemp = 0, sTemp = 0;	
			int bStaticCount = 0, sStaticCount = 0;
				
			Map.Entry<Integer, Amount> sEntry = sDistr.firstEntry();
			Map.Entry<Integer, Amount> bEntry = bDistr.lastEntry();
			int sellerPrice = sEntry.getKey();
			int buyerPrice = bEntry.getKey();
			
			bStaticCount += bEntry.getValue().getAbsolute();
			sStaticCount += sEntry.getValue().getAbsolute();
			
			for(int k = 0; k < (sDistr.size() + bDistr.size()); k++){ 			
			 	if(buyerPrice <= sellerPrice){
			 		//yippee...for that case, we've found an intersection - all is fine :)
			 		break;		
				}
			 	
							
				else if(buyerPrice > sellerPrice) {			
				/*
				 * For that case we have to keep on searching
				 * We search the next point, where one of the functions does the next "step".
				 * We move both counters to that point(quantity) and repeat the loop.
				 * xTemp = difference between current point and next step
				 * xCount = current point
				 * xStaticCount = next "step"
				 */	
					bTemp = bStaticCount  -  bCount;
					sTemp = sStaticCount  -  sCount;
					
					if(bTemp < sTemp){
						bEntry = bDistr.lowerEntry(buyerPrice);
						if(bEntry == null)
							throw new NoIntersectionException(); 
						buyerPrice = bEntry.getKey();
						bCount += bTemp;
						bStaticCount += bEntry.getValue().getAbsolute();
						
						sCount = bCount;
					}else if(bTemp > sTemp){
						sEntry = sDistr.higherEntry(sellerPrice);
						if(sEntry == null)
							throw new NoIntersectionException();
						sellerPrice = sEntry.getKey();
						sCount += sTemp;
						sStaticCount += sEntry.getValue().getAbsolute();

						bCount = sCount;
					}else if(bTemp == sTemp){
						bEntry = bDistr.lowerEntry(buyerPrice);
						if(bEntry == null)
							throw new NoIntersectionException();
						buyerPrice = bEntry.getKey();
						bCount += bTemp;
						bStaticCount += bEntry.getValue().getAbsolute();
						
						sEntry = sDistr.higherEntry(sellerPrice);
						if(sEntry == null)
							throw new NoIntersectionException();
						sellerPrice = sEntry.getKey();
						sCount += sTemp;
						sStaticCount += sEntry.getValue().getAbsolute();
						
						//This increment is needed, because the maximum of the for-loop is the sum of the sizes 
						//of both distributions and here we iterate both distributions.
						k++;
					}
				}
			}
		} catch (NoIntersectionException e1) {
			e1.printStackTrace();
			error = "save.noIntersection";
		}
		// ######################################### Intersection-check END ############################################		
		
		
		if(error == "")	{
			// Set new PlayerCount
			kms.getConfiguration().setPlayerCount(LogicHelper.getAbsoluteSum(kms.getbDistribution()) + LogicHelper.getAbsoluteSum(kms.getsDistribution()));
			
			// Generate Cards-Set
			try	{
				kms.getState().generateCards();
			}	catch (WrongFirstIDException | WrongAssistantCountException | WrongPlayerCountException | WrongRelativeDistributionException e) {
				e.printStackTrace();
				model.addAttribute("error", LogicHelper.getLocalizedMessage("error.PDF.error"));
				model.addAttribute("message", LogicHelper.getLocalizedMessage("error.PDF.message"));
				return "standardException";
			}
			
			if(ControllerHelper.checkFolders())	{
				// Set File Path
				String path = ControllerHelper.getFolderPath("config") + ControllerHelper.getFilename("filename.config") + ".txt";
				
				// Save Config File automatically
				try	{
					FileOutputStream fos = new FileOutputStream(path);
					kms.getState().save(fos);
					LogicHelper.print("Saved Config File in: " + path);
				} catch(IOException e){
					e.printStackTrace();
					model.addAttribute("error", LogicHelper.getLocalizedMessage("error.config.save.error"));
					model.addAttribute("message", LogicHelper.getLocalizedMessage("error.config.save.message"));
					return "standardException";	
				}
				
				// Add path to model
				ra.addFlashAttribute("configSavePath", path);
				
			}
			return "redirect:/save";
			
		}	else	{
			// Build a new Map for giving it to the model to display the mistakes stupid deactivated-javascript-User made.
			if(error != "save.fraction")	{
				Map<Integer, Amount> cConf = new HashMap<>();
				Map<Integer, Amount> sConf = new HashMap<>();
				for(int a = 0; a < cRelativeQuantity.length; a++)	{
					cConf.put(Integer.parseInt(cPrice[a]), new Amount(Integer.parseInt(cRelativeQuantity[a]), Integer.parseInt(cAbsoluteQuantity[a])));
					sConf.put(Integer.parseInt(sPrice[a]), new Amount(Integer.parseInt(sRelativeQuantity[a]), Integer.parseInt(sAbsoluteQuantity[a])));
				}
				ra.addFlashAttribute("customerConfiguration", cConf);
				ra.addFlashAttribute("salesmanConfiguration", sConf);
				ra.addFlashAttribute("cGroupQuantity", cConf.size());
				ra.addFlashAttribute("sGroupQuantity", sConf.size());
			}	else	{
				ra.addFlashAttribute("cGroupQuantity", 0);
				ra.addFlashAttribute("sGroupQuantity", 0);
			}
			ra.addFlashAttribute("error", error);
			ra.addFlashAttribute("isStandard", false);
			return "redirect:/prepare2";
		}
	}
	
	/**
	 * Processes Posted Values from Distribution-Site
	 * @return	Name of template
	 * @throws	InvalidStateChangeException
	 */
	@RequestMapping(value = "generate", method = RequestMethod.GET)
	public String generate() throws InvalidStateChangeException	{
		// STATE-CHANGE
		if(ControllerHelper.stateHelper(kms, "prepare"))	{
			return "generate";
		}	else	{
			return "redirect:/reset";
		}
	}
	
}
