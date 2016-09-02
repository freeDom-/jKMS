package jKMS.states;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import au.com.bytecode.opencsv.CSVWriter;
import jKMS.Amount;
import jKMS.Application;
import jKMS.Kartoffelmarktspiel;
import jKMS.LogicHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class EvaluationTest {
	@Autowired
	Kartoffelmarktspiel kms;
	
	@Before
	public void setUp(){
		
		//Preparation
		kms.prepare();
		kms.getCards().clear();
		
		kms.getConfiguration().setbDistribution(new TreeMap<Integer, Amount>());
		kms.getConfiguration().setsDistribution(new TreeMap<Integer, Amount>());
		
		kms.getState().setBasicConfig(800, 10);
		//kms.getState().loadStandardDistribution();
		
		//STANDARD DISTRIB:
		// Load Buyer Distribution
		Map<Integer, Amount> bDistribution = new TreeMap<>();
		bDistribution.put(70, new Amount(20, 80));
		bDistribution.put(65, new Amount(16, 64));
		bDistribution.put(60, new Amount(16, 64));
		bDistribution.put(55, new Amount(16, 64));
		bDistribution.put(50, new Amount(16, 64));
		bDistribution.put(45, new Amount(16, 64));
		kms.getConfiguration().setbDistribution(bDistribution);
		
		// Load Seller Distribution
		Map<Integer, Amount> sDistribution = new TreeMap<>();
		sDistribution.put(63, new Amount(10, 40));
		sDistribution.put(58, new Amount(18, 72));
		sDistribution.put(53, new Amount(18, 72));
		sDistribution.put(48, new Amount(18, 72));
		sDistribution.put(43, new Amount(18, 72));
		sDistribution.put(38, new Amount(18, 72));
		kms.getConfiguration().setsDistribution(sDistribution);
		
		LogicHelper.print("Loaded Standard Distribution.");
		
		try{
			kms.getState().generateCards();
			}catch (Exception e) {
				e.printStackTrace();	
			}
		
		
		//Playthrough
		kms.load();
		kms.play();
		
		/*c = new LinkedHashSet<Card>(kms.getCards());
		
		for(Card id1 : kms.getCards()){
			if(id1 instanceof BuyerCard){
				for (Card id2 : c){
					if(id2 instanceof SellerCard){
						// TODO Simulate multiple Stations
						kms.getState().addContract(id1.getId(), id2.getId(), Math.min(id1.getValue(), id2.getValue()) + Math.abs(id1.getValue() - id2.getValue())/2, "TEST");
						c.remove(id2);
						
						break;
					}
				}
			}
		}*/
		
		kms.evaluate();
	}
	
	@Test
	//TODO testGetStatistics
	public void testGetStatistics(){
		
	}
	
	@Test
	//TODO testPickWinner
	public void testPickWinner(){
		
	}
	
	@Test
	//CSV
	public void testCsv() throws IOException{
		
		CSVWriter writer = new CSVWriter(new FileWriter("src/test/java/jKMS/states/test.csv"));
		kms.getState().generateCSV(writer);
		writer.close();
	}
}
