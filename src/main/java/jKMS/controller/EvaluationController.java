package jKMS.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.bytecode.opencsv.CSVWriter;
import jKMS.Amount;
import jKMS.Contract;
import jKMS.Kartoffelmarktspiel;
import jKMS.LogicHelper;
import jKMS.exceptionHelper.CreateFolderFailedException;
import jKMS.exceptionHelper.InvalidStateChangeException;
import jKMS.exceptionHelper.NoContractsException;
import jKMS.exceptionHelper.NoIntersectionException;

/**
 * Everything round about Evaluation
 * @author Quiryn
 * @author siegmund42
 */
@Controller
public class EvaluationController extends AbstractServerController {
	
	class ReverseIntegerComparator implements Comparator<Integer>	{
		public int compare(Integer o1, Integer o2){
			return o2.compareTo(o1);
		}
	}
	
	@RequestMapping("/getEvaluation")
	@ResponseBody
	/**
	 * catches AjaxRequest, concatenates data of contracts, data of supply and demand function and min/max values for y-axis
	 * 
	 * @return		string in json-like format, this string is needed for flot-library to plot the chart
	 */
	public String evaluationChart() throws IllegalStateException, NoContractsException, NoIntersectionException{
		//String of current play data
		Set<Contract> contracts = kms.getContracts();
		String playData = ControllerHelper.setToString(contracts);
		
		//String of expected supply and demand
		TreeMap<Integer, Amount> sDistribution = (TreeMap<Integer, Amount>) kms.getsDistribution();
		String expectedSupply = ControllerHelper.mapToString(sDistribution);
		
		TreeMap<Integer, Amount> bDistribution = (TreeMap<Integer, Amount>) kms.getbDistribution();
		String expectedDemand = ControllerHelper.mapToString(bDistribution.descendingMap());
		
		//String of renten
		String renten = ControllerHelper.distributionToFlot(kms.getbDistribution(), kms.getsDistribution(), kms.getContracts());
		
		//min and max values for the chart
		int[] minMax = ControllerHelper.getMinMax(contracts, sDistribution, bDistribution);
		
		String benefits = ControllerHelper.getBenefits(kms.getContracts());
		
		//concatenate return string
		String str = playData.concat(";" + minMax[0] + ";" + minMax[1] + ";" + expectedSupply + ";" + expectedDemand + ";" + renten + ";" + benefits);
		
		return str;
		
	}
	
	/**
	 * gets all attributes of "winner contract" and adds them to the model
	 * @param 	repeat 	define if the lottery should be repeated
	 * @param 	model	Model injection for building the page
	 * @return			Template name
	 */
	@RequestMapping(value = "/lottery")
	public String lottery(@RequestParam(value="repeat", defaultValue = "false") boolean repeat,
						  Model model) throws NoContractsException, InvalidStateChangeException	{
		
		if(ControllerHelper.stateHelper(kms, "evaluate"))	{
		
			Contract winner = kms.getState().pickWinner(repeat);
			int bProfit = kms.getState().buyerProfit(winner);
			int sProfit = kms.getState().sellerProfit(winner);
			
			model.addAttribute("buyerID", winner.getBuyer().getId());
			model.addAttribute("wtp", winner.getBuyer().getValue());
			model.addAttribute("sellerID", winner.getSeller().getId());
			model.addAttribute("cost", winner.getSeller().getValue());
			model.addAttribute("price", winner.getPrice());
			model.addAttribute("bprofit", bProfit);
			model.addAttribute("sprofit", sProfit);
			model.addAttribute("currency", Kartoffelmarktspiel.getCurrency());
			
			return "lottery";
		}	else	{
			return "redirect:/reset";
		}
		
	}
	
	/**
	 * Evaluation Site - also stores the .csv automatically
	 * @param 	model	Model injection for building the page
	 * @return			Template name
	 */
	@RequestMapping(value = "/evaluate")
	public String evaluate(Model model) throws InvalidStateChangeException, IllegalStateException, NoIntersectionException, CreateFolderFailedException	{
		// State Change
		if(ControllerHelper.stateHelper(kms, "evaluate"))	{
			
			if(ControllerHelper.checkFolders())	{
				// Build path for storing the .csv automatically
				String path = ControllerHelper.getFolderPath("export") + ControllerHelper.getFilename("filename.csv") + ".csv";
		    	
				try {
					// Save the .csv automatically
			    	CSVWriter writer = new CSVWriter(new FileWriter(path));
					kms.getState().generateCSV(writer);
					writer.close();
					LogicHelper.print("Saved the .csv in: " + path);
				} catch (IOException e) {
					// Error during CSV generation
					e.printStackTrace();
					model.addAttribute("message", LogicHelper.getLocalizedMessage("error.csv.message"));
					model.addAttribute("error", LogicHelper.getLocalizedMessage("error.csv.error"));
					return "standardException";
				}
				
			}
			// Get statistics
			Map<String, Float> stats = null;
			try {
				stats = kms.getState().getStatistics();
			} catch (NoContractsException e) {
				e.printStackTrace();
				model.addAttribute("message", LogicHelper.getLocalizedMessage("error.noContracts.message"));
				model.addAttribute("error", LogicHelper.getLocalizedMessage("error.noContracts.error"));
				return "standardException";
			}
			
			model.addAttribute("average", String.format("%.2f", Math.round(stats.get("averagePrice")*100)/100.0));
			model.addAttribute("size", Math.round(stats.get("contractsSize")));
			model.addAttribute("min", Math.round(stats.get("minimum")));
			model.addAttribute("max", Math.round(stats.get("maximum")));
			model.addAttribute("standardDeviation", String.format("%.2f",Math.round(stats.get("standardDeviation")*100)/100.0));
			model.addAttribute("eqPrice",Math.round(stats.get("eqPrice")));
			model.addAttribute("eqQuantity", Math.round(stats.get("eqQuantity")));
			model.addAttribute("realBenefits", kms.getState().getRealBenefits());
			model.addAttribute("hypBenefits", kms.getState().getHypBenefits());
			model.addAttribute("currency", Kartoffelmarktspiel.getCurrency());
			
			
			return "evaluate";
		}	else	{
			return "redirect:/reset";
		}
	}
}
