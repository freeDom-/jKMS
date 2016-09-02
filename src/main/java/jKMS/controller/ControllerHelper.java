package jKMS.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletRequest;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import jKMS.Amount;
import jKMS.Contract;
import jKMS.Kartoffelmarktspiel;
import jKMS.LogicHelper;
import jKMS.exceptionHelper.CreateFolderFailedException;
import jKMS.exceptionHelper.InvalidStateChangeException;
import jKMS.states.Evaluation;
import jKMS.states.Load;
import jKMS.states.Play;
import jKMS.states.Preparation;

/**
 * Some static help functions
 * @author Quiryn
 * @author siegmund42
 */
public class ControllerHelper extends AbstractController {
	
	private static ReloadableResourceBundleMessageSource messageSource;
	private static Map<String, String> folders = new HashMap<>();
	
	public static void init(ReloadableResourceBundleMessageSource ms) throws CreateFolderFailedException	{
		folders.put("config", "config");
		folders.put("export", "data");
		folders.put("settings", "settings");
		checkFolders();
		messageSource = ms;
	}
	
	/**
	 * gets the name of the Folder folder.
	 * 
	 * @param 	folder internally handled names
	 * @return 	name of the actual folder
	 */
	public static String getFolderName(String folder)	{
		return folders.get(folder);
	}
	
	/**
	 * Gets the absolute Path of the Folder Folder folder.
	 * @param  folder a string contained in the folders Map
	 * @return an absolute path to the folder, e.g: /media/user/42/KMS/settings/
	 */
	public static String getFolderPath(String folder)	{
		String appFolder = getApplicationFolder();
		String folderName = folders.get(folder);
		return appFolder.concat(folderName).concat(File.separator);
	}
	
	/**
	 * Gets the name of a File (without Extension!)
	 * by concatenating the localized message to a formatted timestamp.
	 * @param  message message key out of .properties file for i18n
	 * @return localized @param message + well formatted timestamp 
	 */
	public static String getFilename(String message)	{
		return LogicHelper.getLocalizedMessage(message) + "_" + getNiceDate();
	}
	
	/**
	 * Responsible for state setting. Checks if a state change from actual state of @param kms 
	 * to @param requestedState is valid.
	 * @param  kms				giving the running Kartoffelmarktspiel because were in a static class here
	 * @param  requestedState 	the state we want to switch to
	 * @return true 			if state setting done or not necessary.
	 * @return false			if requested changing of state requires deletion of data, 
	 * 							note that this would be a valid state change though
	 * @throws InvalidStateChangeException if State-changing is invalid or State is undefined
	 */
	public static boolean stateHelper(Kartoffelmarktspiel kms, String requestedState) throws InvalidStateChangeException	{
		// We are actual in Preparation
		if(kms.getState() instanceof Preparation)	{
			// We want to switch to prepare
			if(requestedState.equals("prepare"))	{
				return true;
			}
			// We want to switch to load
			if(requestedState.equals("load"))	{
				kms.load();
				return true;
			}
			// We want to switch to play
			if(requestedState.equals("play"))	{
				throw new InvalidStateChangeException("Statechange Preparation -> Play is invalid!");
			}
			// We want to switch to evaluate
			if(requestedState.equals("evaluate"))	{
				throw new InvalidStateChangeException("Statechange Preparation -> Evaluation is invalid!");
			}
		}
		// We are actual in Load
		if(kms.getState() instanceof Load)	{
			// We want to switch to prepare
			if(requestedState.equals("prepare"))	{
				return false;
			}
			// We want to switch to load
			if(requestedState.equals("load"))	{
				return true;
			}
			// We want to switch to play
			if(requestedState.equals("play"))	{
				// Check if configuration is ok at this moment
				if(kms.getAssistantCount() > 0 && kms.getPlayerCount() > 0 
						&& kms.getGroupCount("s") > 0 && kms.getGroupCount("b") > 0
						&& kms.getCards().size() > 0)	{
					kms.play();
				}	else	{
					throw new RuntimeException(LogicHelper.getLocalizedMessage("error.uncompleteConfiguration"));
				}
				return true;
			}
			// We want to switch to evaluate
			if(requestedState.equals("evaluate"))	{
				throw new InvalidStateChangeException("Statechange Load -> Evaluation is invalid!");
			}
		}
		// We are actual in Play
		if(kms.getState() instanceof Play)	{
			// We want to switch to prepare
			if(requestedState.equals("prepare"))	{
				return false;
			}
			// We want to switch to load
			if(requestedState.equals("load"))	{
				return false;
			}
			// We want to switch to play
			if(requestedState.equals("play"))	{
				return true;
			}
			// We want to switch to evaluate
			if(requestedState.equals("evaluate"))	{
				kms.evaluate();
				return true;
			}
		}
		// We are actual in Evaluation
		if(kms.getState() instanceof Evaluation)	{
			// We want to switch to prepare
			if(requestedState.equals("prepare"))	{
				return false;
			}
			// We want to switch to load
			if(requestedState.equals("load"))	{
				kms.load();
				return true;
			}
			// We want to switch to play
			if(requestedState.equals("play"))	{
				throw new InvalidStateChangeException("Statechange of Evaluation -> Play is invalid!");
			}
			// We want to switch to evaluate
			if(requestedState.equals("evaluate"))	{
				return true;
			}
		}
		// Nothing done?
		throw new IllegalStateException("The game has no or an invalid State.");
		
	}
	
	/**
	 * Gets the IPs of the users active network adapters
	 * @return IPs of the User in a List of Strings.
	 */
	public static List<String> getIP()	{
		
		List<String> IPs = new LinkedList<>();
		
		Enumeration<NetworkInterface> ifaces = null;
		// Pick up all Network Interfaces
		try {
			ifaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		// Go through the Network Interfaces
		while (ifaces.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface)ifaces.nextElement();
		    try {
				if(ni.isUp() && !ni.isVirtual())	{
					// Pick up all Addresses of the Network Interface
				    Enumeration<InetAddress> addrs = ni.getInetAddresses();
					// Go through the Addresses
					while (addrs.hasMoreElements()) {
						InetAddress ia = (InetAddress)addrs.nextElement();
						// Only display an Address, which is not localhost and has no ":" in it [Filter for MAC Adresses]
						if(!ia.getHostAddress().toString().equals("127.0.0.1") && ia.getHostAddress().toString().indexOf(":") == -1)	{
							IPs.add(ia.getHostAddress().toString());
					    }
					}
				}
			} catch (SocketException e) {
				LogicHelper.print("Was unable to access Socket for getting the IP.");
				e.printStackTrace();
			}
		}
		return IPs;
	}
	
	/**
	 * Gets the port of the given @param request
	 * @param 	request	the request through the port
	 * @return 			Port of the given request
	 */
	public static int getPort(ServletRequest request)	{
		return request.getServerPort();
	}
	
	/**
	 * Returns the path to the Application Folder, which holds the jar
	 * @return 	the path to Application folder, e.g. /media/user/jKMS/
	 */
	public static String getApplicationFolder()	{
		
		URL url = AbstractController.class.getProtectionDomain().getCodeSource().getLocation();
		String path = url.getPath();
		boolean isWin;
		// Check if we have Windows OS
		if(File.separator.equals("\\"))	{
			// We need to replace every "/" with a "\"
			path = path.replace("/", File.separator);
			isWin = true;
		}	else	{
			isWin = false;
		}
		
		// remove "file:" part of the URL if existing
		if(path.substring(0, 5).equals("file:"))	{
			path = path.substring(path.indexOf(File.separator));
		}
		
		if(isWin && path.substring(0, 1) == File.separator)
			path = path.substring(1);
		
		// Go to parent Folder
		String folderPath = null;
		try {
			folderPath = URLDecoder.decode(path.substring(0, path.lastIndexOf(File.separator, path.length() - 2) + 1), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return folderPath;
	}
	
	/**
	 * Creates folders from folders Map if they aren't existing.
	 * @return true  if all folders exist/where created 
	 * 		   false if something went wrong
	 * @throws CreateFolderFailedException thrown when the folder could not be created (e.g. no write-rights)
	 */
	public static boolean createFolders() throws CreateFolderFailedException	{

		String path = getApplicationFolder();
		boolean fine = true;
		
		// Get all folders to check
		for(String folder : folders.keySet())	{
			File tempFolder = new File(path + getFolderName(folder));
			// Check if existing
			if(!tempFolder.exists())	{
				// Build directory
				if(tempFolder.mkdir())	{
					LogicHelper.print("Created Folder: " + tempFolder.getAbsolutePath());
				}	else	{
					throw new CreateFolderFailedException("Failed to generate folder \"" + tempFolder.getAbsolutePath() + "\".");
				}
			}
			// Set variable if erverything worked
			fine = fine && tempFolder.exists();
		}
		return fine;
	}
	
	/**
	 * Checks and, if not yet existing, creates the folder structure
	 * @return 	true if everything created/here now
	 * 			false if not
	 * @throws	CreateFolderFailedException thrown 
	 * 			when the folder could not be created (e.g. no write-rights)
	 */
	public static boolean checkFolders() throws CreateFolderFailedException	{
		
		String path = getApplicationFolder();
		boolean fine = true;
		
		for(String folder : folders.keySet())	{
			File tempFolder = new File(path + getFolderName(folder));
			if(!(fine = fine && tempFolder.exists()))
				break;
		}
		
		if(!fine)	{
			// Some directories are missing
			if(createFolders())	{
				// Add succeed
				LogicHelper.print("Adding folders succeeded.");
				return true;
			}	else	{
				// Add fail
				LogicHelper.print("Adding folders failed. Please try again by re-installing KMS", 2);
				return false;
			}
		}	else	{
			return true;
		}
		
	}
	
	/**
	 * Gets all the users written in settings file.
	 * @return users from settings file in a Set&lt;String&gt;
	 * @throws IOException thrown if file does not exist
	 */
	public static Set<String> getUsers() throws IOException	{
		// Path to password-config-file
		String path = ControllerHelper.getFolderPath("settings") + LogicHelper.getLocalizedMessage("filename.settings") + ".txt";
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(path));
				
		String line = br.readLine();
		// Every line is a User
		HashSet<String> string = new HashSet<String>();
		while(line != null)	{
			string.add(line.substring(0, line.indexOf(":")));
			line = br.readLine();
		}
		br.close();
		return string;
	}
	
	/**
	 * Get a well formatted timestamp for adding it to auto saved files
	 * @return well formatted timestamp as String
	 */
	public static String getNiceDate()	{
		Date dNow = new Date( );
	    SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMdd_HHmmss");
	    return ft.format(dNow);
	}
	
	/**
	 * Gets all available .properties Files,
	 * each of them must define a currentLanguage property,
	 * which must be equal to the output of the Locale.getDisplayLanguage() method.
	 * @return 	Map&lt;String, String&gt; where keys are the locales [de, en, ...]<br>
	 * 			and values the written name of the locale in its own language<br>
	 * 			Note that .properties files must define a currentLocale=__ for that.
	 */
	public static Map<String, String> getLanguages()	{
		
		HashMap<String, String> languages = new HashMap<>();
	    
	    for (Locale locale : Locale.getAvailableLocales()) {
	        String msg = messageSource.getMessage("currentLanguage", null, locale);  
	        if (locale.getDisplayLanguage(locale).equals(msg) && !languages.containsKey(locale.getLanguage())){  
	        	languages.put(locale.getLanguage(), locale.getDisplayLanguage(locale));
	        }  
	    }
		
		return languages;
	}
	
	/**
	 * Gets the set of contracts and converts it to a string for the javascript flot library
	 * 
	 * @param 	contracts Set of contracts as part of the Kartoffelmarktspiel class
	 * @return 	string holding the contract information
	 */
	public static String setToString(Set<Contract> contracts){
		if(contracts.isEmpty()) return "[]";
		
		String str = "[";
		int i = 0;
		
		for(Contract c : contracts){
			str = str.concat("["+i+","+c.getPrice()+"],");
			i++;
			if(i == 1){
				str = str.concat("["+i+","+c.getPrice()+"],");
				i++;
			}
		}
		
		str = str.substring(0, str.length()-1).concat("]");
		
		return str;		
	}
	
	/**
	 * Gets the distribution maps, the set of contracts and converts it to a string 
	 * readable by flot library for drawing the total rents.
	 * 
	 * @param 	bmap Map of buyer distribution
	 * @param	smap Map of seller distribution
	 * @param	contracts Set of contracts
	 * @return 	string holding the total rents information
	 */
	public static String distributionToFlot(Map<Integer, Amount> bmap, Map<Integer, Amount> smap, Set<Contract> contracts){

		String bstr = "[";
		String sstr = "[";
		Set<Integer> newbSet;
		Set<Integer> newsSet;
		newbSet = new TreeSet<>(Collections.reverseOrder());
		newsSet = new TreeSet<>();
		newbSet.addAll(bmap.keySet());
		newsSet.addAll(smap.keySet());

		Iterator<Integer> bit = newbSet.iterator();
		Iterator<Integer> sit = newsSet.iterator();
		double curRent = 0, b = 0, s = 0, a = 0;
		int bprice = bit.next();
		int sprice = sit.next();
		double p = 0.01;
		
		while(curRent < ControllerHelper.getRealBenefits(contracts))	{
			// go to next step
			if(b >= bmap.get(bprice).getAbsolute())	{
				bprice = bit.next();
				b = 0;
			}
			if(s >= smap.get(sprice).getAbsolute())	{
				sprice = sit.next();
				s = 0;
			}
			// Stop after intersection
			if(bprice - sprice <= 0)
				break;
			// Add the current area to the rents
			curRent += (bprice - sprice)*p;
			bstr = bstr.concat("[" + a + "," + bprice + "],");
			sstr = sstr.concat("[" + a + "," + sprice + "],");
			a += p;
			s += p;
			b += p;
		}
		if(bstr.length() > 1)	{
			bstr = bstr.substring(0, bstr.length()-1);
			sstr = sstr.substring(0, sstr.length()-1);
		}
		bstr = bstr.concat("]");
		sstr = sstr.concat("]");
		
		return bstr.concat(";" + sstr);		
	}
	
	/**
	 * Returns the total rents that where achieved.
	 * @param contracts The set of contracts
	 * @return the total rents
	 */
	public static int getRealBenefits(Set<Contract> contracts)	{
		int realBenefits = 0;
		for(Contract contract : contracts){
			realBenefits += contract.getBuyer().getValue() - contract.getSeller().getValue();
		}
		return realBenefits;
	}
	
	/**
	 * Returns a string readable by the flot library for displaying the rent of every contract
	 * @param contracts The set of contracts
	 * @return A string for displaying benefit curve
	 */
	public static String getBenefits(Set<Contract> contracts)	{
		if(contracts.isEmpty()) 
			return "[]";
		String str = "[";
		int i = 1;
		for(Contract contract : contracts)	{
			str = str.concat("[" + i + "," + (contract.getBuyer().getValue() - contract.getSeller().getValue()) + "],");
			i++;
		}
		str = str.substring(0, str.length()-1).concat("]");
		return str;
	}
	
	/**
	 * Gets a map of distribution and converts it to a string for the javascript flot library
	 * 
	 * @param 	distribution one of the distribution Map as part of the Configuration class
	 * @return 	string holding the distribution information
	 */
	public static String mapToString(Map<Integer,Amount>  distribution){
		String str = "[";
		int absolute = 0;
		int lastKey = 0;
		
		for(Map.Entry<Integer, Amount> entry : distribution.entrySet()){
			
			str = str.concat("[" + absolute + "," + entry.getKey() + "],");
			absolute += entry.getValue().getAbsolute();
			
			lastKey = entry.getKey();
		}
		
		str = str.concat("[" + absolute + "," + lastKey + "]]");
		
		return str;
	}
	
	/**
	 * gets the minimum and maximum values of the distributions and compares it to the min and max of the contracts set. 
	 * With these values we can limit the chart on 20% difference to the highest and lowest possible value, if a contract price is much to high or much to low .
	 * 
	 * @param 	contracts Set of contracts as part of the Kartoffelmarktspiel class
	 * @param 	sDistribution sDistribution Map as part of the Configuration class
	 * @param 	bDistribution bDistribution Map as part of the Configuration class
	 * @return 	Array holding the min [0] and max [1] price
	 */
	public static int[] getMinMax(Set<Contract> contracts, TreeMap<Integer,Amount> sDistribution, TreeMap<Integer,Amount> bDistribution) {
		int smin = sDistribution.firstKey();
		int smax = sDistribution.lastKey();
		int bmin = bDistribution.firstKey();
		int bmax = bDistribution.lastKey();
		int min, max, tempPrice;
		
		//get the min and max values (+20%) of the distributions
		if(smin < bmin){
			min = smin - smin/5;
		}else {
			min = bmin - bmin/5;
		}
		
		if(smax > bmax) {
			max = smax + smax/5;
		}else{
			max = bmax + bmax/5;
		}
		
		//if there are no contracts yet, we will limit the y-axis scale. otherwise there is a weird scale with negatives...
		if(contracts.isEmpty()){
			int[] result = {min,max};
			return result;
		}
		
		//get the min and max prices of the contracts
		int maxPrice = 0;
		int minPrice = max;
		Iterator<Contract> i = contracts.iterator();
		while(i.hasNext()){
			tempPrice = i.next().getPrice();
			
			if(tempPrice < minPrice){
				minPrice = tempPrice;
			}
			if(tempPrice > maxPrice){
				maxPrice = tempPrice;
			}
		}
		
		//cases in which we don't have to limit the chart with the distribution values
		if(maxPrice < max){
			max = 0;
		}
		if(minPrice > min){
			min = 0;
		}
		
		int[] result = {min,max};
		
		return result;
	}
}
