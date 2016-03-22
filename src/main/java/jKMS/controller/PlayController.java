package jKMS.controller;

import jKMS.Amount;
import jKMS.Contract;
import jKMS.exceptionHelper.InvalidStateChangeException;

import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author	Quiryn
 *
 */
@Controller
public class PlayController extends AbstractServerController {
	
	@RequestMapping("/getData")
	@ResponseBody
	/**
	 * Catches Ajax-Request, converts the set of contracts into a String with the help of the ControllerHelper
	 */
	public String insertData(){
		//String with current data of contracts
		Set<Contract> contracts = kms.getContracts();
		String str = ControllerHelper.setToString(contracts);
		
		//get min and max values of the distributions to limit the chart
		TreeMap<Integer, Amount> sDistribution = (TreeMap<Integer, Amount>) kms.getsDistribution();
		TreeMap<Integer, Amount> bDistribution = (TreeMap<Integer, Amount>) kms.getbDistribution();
		int[] minMax = ControllerHelper.getMinMax(contracts, sDistribution, bDistribution);
		
		str = str.concat(";" + minMax[0] + ";" + minMax[1]);
		
		return str;
	}
	
	/**
	 * Just displaying chart -> Everything javascript (;
	 * @param 	s	 if game is stopped this parameter is 'stop', else false
	 */
	@RequestMapping(value = "/play")
	public String play(Model model) throws InvalidStateChangeException	{

		boolean stateChangeSuccessful = true;
		
		stateChangeSuccessful = ControllerHelper.stateHelper(kms, "play");
		
		if(stateChangeSuccessful)	{
			return "play";
		}	else	{
			return "redirect:/reset";
		}
	}
}
