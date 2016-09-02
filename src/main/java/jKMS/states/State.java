package jKMS.states;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;

import au.com.bytecode.opencsv.CSVWriter;
import jKMS.Contract;
import jKMS.Kartoffelmarktspiel;
import jKMS.exceptionHelper.EmptyFileException;
import jKMS.exceptionHelper.FalseLoadFileException;
import jKMS.exceptionHelper.NoContractsException;
import jKMS.exceptionHelper.NoIntersectionException;
import jKMS.exceptionHelper.WrongAssistantCountException;
import jKMS.exceptionHelper.WrongFirstIDException;
import jKMS.exceptionHelper.WrongPackageException;
import jKMS.exceptionHelper.WrongPlayerCountException;
import jKMS.exceptionHelper.WrongRelativeDistributionException;

public abstract class State {
	protected Kartoffelmarktspiel kms;
	
	public void loadStandardDistribution() throws IllegalStateException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}

	public void generateCards() throws WrongFirstIDException, WrongAssistantCountException, WrongPlayerCountException, WrongRelativeDistributionException, IllegalStateException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}
	
	public void newGroup(boolean isBuyer, int price, int relativeNumber, int absoluteNumber) throws IllegalStateException{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}

	public void setBasicConfig(int playerCount, int assistantCount) throws IllegalStateException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}

	public void createPdf(boolean isBuyer, Document doc) throws DocumentException,IOException,IllegalStateException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}

	public boolean removeCard(char pack, int lastId) throws WrongPackageException, WrongPlayerCountException, WrongAssistantCountException, WrongFirstIDException, WrongRelativeDistributionException{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}

	public boolean removeContract(int id1, int id2, int price)	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}

	public int addContract(int id1, int id2, int price, String uri) throws IllegalStateException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}
															
	public float[] getEquilibrium() throws NoIntersectionException, IllegalStateException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}

	public Map<String,Float> getStatistics() throws NoContractsException, NoIntersectionException, IllegalStateException{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}

	public Contract pickWinner(boolean repeat) throws NoContractsException,IllegalStateException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}
	
	public int getHypBenefits() throws IllegalStateException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}
	
	public int getRealBenefits() throws IllegalStateException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}

	public int buyerProfit(Contract contract) throws IllegalStateException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}

	public int sellerProfit(Contract contract) throws IllegalStateException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}
	
	public boolean save(OutputStream o) throws IOException, IllegalStateException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}
	
	public void load(MultipartFile file) throws NumberFormatException, IOException, EmptyFileException, IllegalStateException, FalseLoadFileException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}
	
	public void generateCSV(CSVWriter writer) throws IllegalStateException	{
		throw new IllegalStateException("It seems you did not follow the workflow correctly. Please try again from the beginning!");
	}
}   
