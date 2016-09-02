package jKMS.states;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import au.com.bytecode.opencsv.CSVWriter;
import jKMS.Amount;
import jKMS.Contract;
import jKMS.Csv;
import jKMS.Kartoffelmarktspiel;
import jKMS.LogicHelper;
import jKMS.controller.ControllerHelper;
import jKMS.exceptionHelper.NoContractsException;
import jKMS.exceptionHelper.NoIntersectionException;

public class Evaluation extends State{
	
	private Contract winner = null;
	
	public Evaluation(Kartoffelmarktspiel kms){
		this.kms = kms;
	}
	
	/**
	 * Returns statistic data of all contracts
	 * min, max, average, standard deviation, quantity of contracts
	 */
	@Override
	public Map<String,Float> getStatistics() throws NoContractsException, NoIntersectionException{ 
		Set<Contract> contracts = kms.getContracts();
		Map<String,Float> statistics = new HashMap<String, Float>();
		float sum = 0;
		float max = 0;
		float tempPrice = 0;
		
		if(contracts.size() == 0) throw new NoContractsException();
		
		Iterator<Contract> i = contracts.iterator();
		while(i.hasNext()){
			Contract contract = i.next();
			tempPrice = contract.getPrice();
			
			//maximum
			if(tempPrice > max){
				max = tempPrice;
			}
			
			//sum
			sum += tempPrice;
		}
		
		float averagePrice = sum/contracts.size();
		statistics.put("maximum", max);
		statistics.put("averagePrice",averagePrice);
		statistics.put("contractsSize", (float) contracts.size());
		statistics.put("realBen", (float) kms.getState().getRealBenefits());
		statistics.put("hypBen", (float) kms.getState().getHypBenefits());
		
		float min = max;
		float sumOfSquares = 0;
		float variance = 0;
		double standardDeviation = 0;
		
		Iterator<Contract> k = contracts.iterator();
		while(k.hasNext()){
			tempPrice = k.next().getPrice();
			
			//minimum
			if(tempPrice < min){
				min = tempPrice;
			}
			
			//variance
			sumOfSquares += (tempPrice - averagePrice)*(tempPrice-averagePrice);
		}
		
		variance = sumOfSquares/contracts.size();
		standardDeviation = Math.sqrt(variance);
		statistics.put("minimum", min);
		//statistics.put("variance", variance);
		statistics.put("standardDeviation",(float) standardDeviation);
		
		float[] equilibrium = getEquilibrium();
		if(equilibrium  == null) throw new NoIntersectionException();
		statistics.put("eqPrice",equilibrium[0]);
		statistics.put("eqQuantity",equilibrium[1]);
		return statistics; 
	}
	
	/**
 	 * Calculate equilibrium price and equilibrium quantity
	 * return null or throws NoIntersectionException, if there is no intersection between supply and demand function
 	 */
	public float[] getEquilibrium() throws NoIntersectionException{
 		TreeMap<Integer,Amount> sDistr = (TreeMap<Integer, Amount>) kms.getsDistribution();
 		TreeMap<Integer, Amount> bDistr = (TreeMap<Integer, Amount>) kms.getbDistribution();
 		
 		int bCount = 0, sCount = 0, max = 0, bTemp = 0, sTemp = 0;	
 		int bStaticCount = 0, sStaticCount = 0;
 		boolean bIsInFront = false;
 			
 		Map.Entry<Integer, Amount> sEntry = sDistr.firstEntry();
 		Map.Entry<Integer, Amount> bEntry = bDistr.lastEntry();
 		int sPrice = sEntry.getKey();
 		int bPrice = bEntry.getKey();
	
 		max = sDistr.size() + bDistr.size();
 		
 		bStaticCount += bEntry.getValue().getAbsolute();
 		sStaticCount += sEntry.getValue().getAbsolute();
 		
 		for(int i = 0; i < max; i++){
 			//yippee...for that case, we've found an intersection
 		 	if(bPrice < sPrice){
 		 		if(bIsInFront){
 		 			float[] result = {sPrice, sCount};
 		 			return result;
 		 		}else{
 		 			float[] result = {bPrice, bCount};
 					return result;
 				}
				
			}
			//if there are many points of intersection, the last point is the right one :)
			else if(bPrice == sPrice){
				bTemp = bStaticCount  -  bCount;
				sTemp = sStaticCount  -  sCount;
				if(bTemp < sTemp){
					float[] result = {bPrice, bCount + bTemp};
					return result;
				}
				else{
					float[] result = {sPrice, sCount + sTemp};
					return result;
				}
			}
			/*
			 * For that case we have to keep on searching
			 * We search the next point, where one of the functions does the next "step".
			 * We move both counters to that point(quantity) and repeat the loop.
			 */			
			else if(bPrice > sPrice) {			
				
				bTemp = bStaticCount  -  bCount;
				sTemp = sStaticCount  -  sCount;
				
				if(bTemp < sTemp){
					bEntry = bDistr.lowerEntry(bPrice);
					if(bEntry == null) throw new NoIntersectionException(); 
					bPrice = bEntry.getKey();
					bCount += bTemp;
					bStaticCount += bEntry.getValue().getAbsolute();
					
					bIsInFront = true;	
					
					sCount = bCount;
				}else if(bTemp > sTemp){
					sEntry = sDistr.higherEntry(sPrice);
					if(sEntry == null) throw new NoIntersectionException();
					sPrice = sEntry.getKey();
					sCount += sTemp;
					sStaticCount += sEntry.getValue().getAbsolute();
					
					bIsInFront = false;
						
					bCount = sCount;
				}else if(bTemp == sTemp){
					bEntry = bDistr.lowerEntry(bPrice);
					if(bEntry == null) throw new NoIntersectionException();
					bPrice = bEntry.getKey();
					bCount += bTemp;
					bStaticCount += bEntry.getValue().getAbsolute();
					
					sEntry = sDistr.higherEntry(sPrice);
					if(sEntry == null) throw new NoIntersectionException();
					sPrice = sEntry.getKey();
					sCount += sTemp;
					sStaticCount += sEntry.getValue().getAbsolute();
					
					//This increment is needed, because the maximum of the for-loop is the sum of the sizes 
					//of both distributions and here we iterate both distributions.
					i++;
				}
			}
 		}
			
		return null;
	}
	
	/**
	 * Chooses a random winner-contract
	 * 
	 * @return 	object of contract
	 */
	@Override
	public Contract pickWinner(boolean repeat) throws NoContractsException{ 
		if(winner == null || repeat == true){
			Set<Contract> contracts = kms.getContracts();
			
			if(contracts == null) throw new NoContractsException();
			
			Random rand = new Random();
			int randomInt = rand.nextInt(contracts.size());
			int i = 0;
			Iterator<Contract> k = contracts.iterator();
			
			while(k.hasNext() && i < randomInt){
				k.next();
				i++;
			}
			
			winner = k.next();
			
		}
		
		return winner;
	}
	
	@Override
	public int getHypBenefits()	{
		int hypBenefit = 0, help = 0;
		int[] buyerArray = new int[kms.getConfiguration().getPlayerCount("b")];
		for(int a : kms.getbDistribution().keySet())	{
			for(int i = 1; i <= kms.getbDistribution().get(a).getAbsolute(); help++, i++)	{
				buyerArray[help] = a;
			}
		}
		int[] sellerArray = new int[kms.getConfiguration().getPlayerCount("s")];
		help = 0;
		for(int a : kms.getsDistribution().keySet())	{
			for(int i = 1; i <= kms.getsDistribution().get(a).getAbsolute(); help++, i++)	{
				sellerArray[help] = a;
			}
		}
		try	{
			for(int i = 0; i < buyerArray.length && buyerArray[buyerArray.length - 1 - i] > sellerArray[i]; i++)	{
				hypBenefit += buyerArray[buyerArray.length - 1 - i] - sellerArray[i];
			}
		} catch(IndexOutOfBoundsException e)	{
			if(kms.getPlayerCount() % 2 == 0)	{
				// Error, IndexOutOfBounds can only happen if number of buyers and sellers is not equal (odd player count)
				LogicHelper.print(LogicHelper.getLocalizedMessage("error.evaluation.hypBenefits"), 2);
			}
		}
		return hypBenefit;
	}
	
	@Override
	public int getRealBenefits()	{
		return ControllerHelper.getRealBenefits(kms.getContracts());
	}
	
	@Override
	public int buyerProfit(Contract con){ 
		int wtp = con.getBuyer().getValue();
		return (wtp - con.getPrice()); 
	}
	
	@Override
	public int sellerProfit(Contract con){ 
		int cost = con.getSeller().getValue();
		return (con.getPrice() - cost); 
	}
	
	@Override
	public void generateCSV(CSVWriter writer){
		Csv csv = new Csv();
		
		csv.generateCSV(writer, kms.getCards(), kms.getContracts(), kms.getBegin(),kms.getEnd());
		
	}
	
}
