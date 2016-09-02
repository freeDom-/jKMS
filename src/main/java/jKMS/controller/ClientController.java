package jKMS.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jKMS.states.Evaluation;

/**
 * Client Controller for all pages on the client side [contract].
 * @author siegmund42
 */
@Controller
public class ClientController extends AbstractController {
	
	/**
	 * Redirect to /contract Site
	 * @return Template name
	 */
	@RequestMapping("/")
	public String redirect() {
		return "redirect:/contract";
	}
	
	/**
	 * Display the contract Form
	 * @param	model 	The given Model
	 * @return 			Template name
	 */
	@RequestMapping(value = "/contract", method = RequestMethod.GET)
	public String contract(Model model)	{
		model.addAttribute("firstID", kms.getConfiguration().getFirstID());
		model.addAttribute("numberOfPlayers", kms.getConfiguration().getPlayerCount());
		model.addAttribute("lastID", kms.getLastId());
		model.addAttribute("languages", ControllerHelper.getLanguages());
		return "contract";
	}
	
	/**
	 * Process the contract Request
	 * @param	model 	The given Model
	 * @param	ra		RedirectAttributes injection for next page
	 * @param	id1		ID of first trade partner
	 * @param	id2		ID of second trade partner
	 * @param	price	of the contract
	 * @param	c		indicates if we want to correct a contract
	 * @param	a		[correct candidate] ID of first trade partner
	 * @param	b		[correct candidate] ID of second trade partner
	 * @param	p		[correct candidate] of the contract
	 * @param	request	Request object for setting IP address
	 * @return 			Template name
	 */
	@RequestMapping(value = "/contract", method = RequestMethod.POST)
	public String contract(Model model, RedirectAttributes ra,
		 	@RequestParam(value = "id1", required = true) String id1, 
	        @RequestParam(value = "id2", required = true) String id2,
	        @RequestParam(value = "price", required = true) String price,
		 	@RequestParam(value = "c", required = false) String c, 
		 	@RequestParam(value = "a", required = false) String a, 
	        @RequestParam(value = "b", required = false) String b,
	        @RequestParam(value = "p", required = false) String p,
	        HttpServletRequest request)	{
		
		try	{
			// Check if we need to correct [remove a contract]
			if(c != null)	{
				// Remove the Contract
				if(!kms.getState().removeContract(Integer.parseInt(a), Integer.parseInt(b), Integer.parseInt(p)))	{
					// It didnt work
			    	ra.addFlashAttribute("id1", id1);
			    	ra.addFlashAttribute("id2", id2);
			    	ra.addFlashAttribute("price", price);
			    	ra.addFlashAttribute("error", "correct");
			    	return "redirect:/contract";
				}
			}
			
			// Check if Fields empty
			if(id1 != "" && id2 != "" && price != "")	{
				// Convert Strings to Integer
				int idOne = Integer.parseInt(id1);
				int idTwo = Integer.parseInt(id2);
				int cost = Integer.parseInt(price);
				// Add the Contract
				int add = kms.getState().addContract(idOne, idTwo, cost, request.getRemoteAddr());
				
			    if(add == 0)	{
			    	// Succeeded
			    	ra.addFlashAttribute("success", 42);
			    	ra.addFlashAttribute("a", idOne);
			    	ra.addFlashAttribute("b", idTwo);
			    	ra.addFlashAttribute("p", cost);
			    	return "redirect:/contract.html";
			    }	else	{
			    	// Failed - add Attributes to model
			    	ra.addFlashAttribute("id1", id1);
			    	ra.addFlashAttribute("id2", id2);
			    	ra.addFlashAttribute("price", price);
			    	ra.addFlashAttribute("error", add);
			    }
			}	else	{
				// Fields where empty - add Attributes to model
		    	ra.addFlashAttribute("id1", id1);
		    	ra.addFlashAttribute("id2", id2);
		    	ra.addFlashAttribute("price", price);
		    	ra.addFlashAttribute("error", "empty");
			}
	    	return "redirect:/contract";
			
		}	catch(NumberFormatException e)	{
			// Number seems to be fractional
			e.printStackTrace();
			ra.addFlashAttribute("error", "fraction");
			return "redirect:/contract";
		}	catch(IllegalStateException e)	{
			// Game not running
			e.printStackTrace();
			if(kms.getState() instanceof Evaluation)	{
				// Guess Game stopped
				ra.addFlashAttribute("error", "stopped");
			}	else	{
				// Guess Game not running
				ra.addFlashAttribute("error", "notRunning");
			}
			return "redirect:/contract";
		}
	}

}
