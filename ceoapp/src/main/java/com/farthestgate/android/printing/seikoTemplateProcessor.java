package com.farthestgate.android.printing;

import android.util.Base64;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.helper.DBHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class seikoTemplateProcessor {

	public String PCN = "";
	public String barCode = "";
	public String contraventionDate = "";
	public String contraventionTime = "";
	public String contraventionLocation = "";
	public String contraventionDiscountFee = "";
	public String contraventionFullFee = "";
	public String VRM = "";
	public String contraventionDescription = "";
	public String contraventionCode = "";
	public String contraventionSuffix = "";
	public String vehicleMake = "";
	public String vehicleModel = "";
	public String vehicleColour = "";
	public String shoulderNumber = "";
	public String panddExpiryDate = "";
	public String panddExpiryTime = "";
	public String printedDate = "";
	public String printedTime = "";
	public String obsStartDate = "";
	public String obsStartTime = "";
	public String obsEndDate = "";
	public String obsEndTime = "";
	public String obsDuration = "";
	public String hhID = "";
	public int printCount = 0;
	public String templatePath = "";
	public int pageWidth = 0;
	public boolean setTestData = false;
	public String warningnotice = "";
	public String exactLocation;

	private HashMap<String, String> valuesMap;

	private static HashMap<String, byte[]> pcMap;
	
	static {
		   pcMap = new HashMap<String, byte[]>();
		   pcMap.put("pageLengthLines",new byte[] {(byte) (char) 27, (byte) "C".charAt(0)});
		   pcMap.put("pageLengthInches",new byte[] {(byte) (char) 27, (byte) "C".charAt(0), (byte) (char) 0});
		   pcMap.put("setBottomMargin",new byte[] {(byte) (char) 27, (byte) "N".charAt(0)});
		   pcMap.put("cancelBottomMargin",new byte[] {(byte) (char) 27, (byte) "O".charAt(0)});
		   pcMap.put("setRightMargin",new byte[] {(byte) (char) 27, (byte) "Q".charAt(0)});
		   pcMap.put("setLeftMargin",new byte[] {(byte) (char) 27, (byte) "l".charAt(0)});
		   pcMap.put("setOneEigthLineSpacing",new byte[] {(byte) (char) 27, (byte) "0".charAt(0)});
		   pcMap.put("setOneSixthLineSpacing",new byte[] {(byte) (char) 27, (byte) "2".charAt(0)});
		   pcMap.put("setNDotLineSpacing",new byte[] {(byte) (char) 27, (byte) "3".charAt(0)});
		   pcMap.put("cr",new byte[] {(byte) (char) 13});
		   pcMap.put("lf",new byte[] {(byte) (char) 10});
		   pcMap.put("ff",new byte[] {(byte) (char) 12});
		   pcMap.put("printAndFeedPaper",new byte[] {(byte) (char) 27, (byte) "J".charAt(0)});
		   pcMap.put("markedPaperFormFeed",new byte[] {(byte) (char) 29, (byte) "<".charAt(0)});
		   pcMap.put("setAbsolutePosition",new byte[] {(byte) (char) 27, (byte) "$".charAt(0)});
		   pcMap.put("setRelativePosition",new byte[] {(byte) (char) 27, (byte) "\\".charAt(0)});
		   pcMap.put("setInternationalCharacterSet",new byte[] {(byte) (char) 27, (byte) "R".charAt(0)});
		   pcMap.put("setUKCharacterSet",new byte[] {(byte) (char) 27, (byte) "R".charAt(0), (byte) (char) 3});
		   pcMap.put("selectCharacterCodeTable",new byte[] {(byte) (char) 27, (byte) "t".charAt(0)});
		   pcMap.put("specifyEuroCharacter",new byte[] {(byte) (char) 18, (byte) "y".charAt(0)});
		   pcMap.put("selectExpandedCharacterModeAutoCancel",new byte[] {(byte) (char) 14});
		   pcMap.put("cancelExpandedCharacterModeAutoCancel",new byte[] {(byte) (char) 24});
		   pcMap.put("selectOrCancelExpandedCharacterMode",new byte[] {(byte) (char) 27, (byte) "W".charAt(0)});
		   pcMap.put("selectOrCancelDoubleHeightMode",new byte[] {(byte) (char) 27, (byte) "w".charAt(0)});
		   pcMap.put("selectEmphasizedPrintMode",new byte[] {(byte) (char) 27, (byte) "E".charAt(0)});
		   pcMap.put("cancelEmphasizedPrintMode",new byte[] {(byte) (char) 27, (byte) "F".charAt(0)});
		   pcMap.put("selectDoublePrintMode",new byte[] {(byte) (char) 27, (byte) "G".charAt(0)});
		   pcMap.put("cancelDoublePrintMode",new byte[] {(byte) (char) 27, (byte) "H".charAt(0)});
		   pcMap.put("selectOrCancelUnderlineMode",new byte[] {(byte) (char) 27, (byte) "-".charAt(0)});
		   pcMap.put("setPrintMode",new byte[] {(byte) (char) 27, (byte) "!".charAt(0)});
		   pcMap.put("setCharacterRotation",new byte[] {(byte) (char) 18, (byte) "Y".charAt(0)});
		   pcMap.put("setCharacterSpacing",new byte[] {(byte) (char) 27, (byte) (char) 32}); // Space
		   pcMap.put("setBitImageMode",new byte[] {(byte) (char) 27, (byte) "m".charAt(0)});
		   pcMap.put("rasterBitImagePrint",new byte[] {(byte) (char) 29, (byte) "v".charAt(0), (byte) "0".charAt(0)});
		   pcMap.put("cancelPrintDataInBuffer",new byte[] {(byte) (char) 24});
		   pcMap.put("rulerLineOn",new byte[] {(byte) (char) 19, (byte) "+".charAt(0)});
		   pcMap.put("rulerLineOff",new byte[] {(byte) (char) 19, (byte) "-".charAt(0)});
		   pcMap.put("rulerLineBufferA",new byte[] {(byte) (char) 19, (byte) "A".charAt(0)});
		   pcMap.put("rulerLineBufferB",new byte[] {(byte) (char) 19, (byte) "B".charAt(0)});
		   pcMap.put("rulerLineBufferClear",new byte[] {(byte) (char) 19, (byte) "C".charAt(0)});
		   pcMap.put("defineRulerLineByDot",new byte[] {(byte) (char) 19, (byte) "D".charAt(0)});
		   pcMap.put("defineRulerLineWithRepeatingPatterns",new byte[] {(byte) (char) 19, (byte) "F".charAt(0)});
		   pcMap.put("defineRulerLineByLine",new byte[] {(byte) (char) 19, (byte) "D".charAt(0)});
		   pcMap.put("rulerLineLSBOrMSBImage",new byte[] {(byte) (char) 19, (byte) "V".charAt(0)});
		   pcMap.put("printOneDotLineAfterPrintingLineBufferData",new byte[] {(byte) (char) 19, (byte) "P".charAt(0)});
		   pcMap.put("selectHRICharacterPrintPosition",new byte[] {(byte) (char) 29, (byte) "H".charAt(0)});
		   pcMap.put("setHRICharacterFont",new byte[] {(byte) (char) 29, (byte) "f".charAt(0)});
		   pcMap.put("setBarCodeHeight",new byte[] {(byte) (char) 29, (byte) "h".charAt(0)});
		   pcMap.put("setBarCodeWidth",new byte[] {(byte) (char) 29, (byte) "w".charAt(0)});
		   pcMap.put("setBarCodePrintPosition",new byte[] {(byte) (char) 29, (byte) "P".charAt(0)});
		   pcMap.put("nominalFineElementWidth",new byte[] {(byte) (char) 29, (byte) "n".charAt(0)});
		   pcMap.put("PDFRowHeight",new byte[] {(byte) (char) 29, (byte) "o".charAt(0)});
		   pcMap.put("printBarCode",new byte[] {(byte) (char) 29, (byte) "k".charAt(0)});
		   pcMap.put("PDF417Print",new byte[] {(byte) (char) 29, (byte) "p".charAt(0), (byte) (char) 0});
		   pcMap.put("QRCodeAndDataMatrixModuleSizes",new byte[] {(byte) (char) 18, (byte) ";".charAt(0)});
		   pcMap.put("QRCodePrint",new byte[] {(byte) (char) 29, (byte) "p".charAt(0), (byte) (char) 1});
		   pcMap.put("dataMatrixPrint",new byte[] {(byte) (char) 29, (byte) "p".charAt(0), (byte) (char) 2});
		   pcMap.put("maxiCodePrint",new byte[] {(byte) (char) 29, (byte) "p".charAt(0), (byte) (char) 3});
		   pcMap.put("pageModeSelect",new byte[] {(byte) (char) 18, (byte) "z".charAt(0), (byte) (char) 0});
		   pcMap.put("pageModePrint",new byte[] {(byte) (char) 18, (byte) "z".charAt(0), (byte) (char) 1});
		   pcMap.put("pageModeVerticalPositionSpecify",new byte[] {(byte) (char) 18, (byte) "z".charAt(0), (byte) (char) 2});
		   pcMap.put("pageModeDataRegistration",new byte[] {(byte) (char) 18, (byte) "z".charAt(0), (byte) (char) 4});
		   pcMap.put("pageModeDataCalling",new byte[] {(byte) (char) 18, (byte) "z".charAt(0), (byte) (char) 5});
		   pcMap.put("rectanglePrint",new byte[] {(byte) (char) 18, (byte) "$".charAt(0)});
		   pcMap.put("lineTypeProperty",new byte[] {(byte) (char) 18, (byte) "$".charAt(0), (byte) "2".charAt(0)});
		   pcMap.put("lineWidthProperty",new byte[] {(byte) (char) 18, (byte) "$".charAt(0), (byte) "3".charAt(0)});
		   pcMap.put("fillProperty",new byte[] {(byte) (char) 18, (byte) "$".charAt(0), (byte) "4".charAt(0)});
		   pcMap.put("selectCharcterFontSize",new byte[] {(byte) (char) 18, (byte) "F".charAt(0)});
		   pcMap.put("selectPaper",new byte[] {(byte) (char) 18, (byte) "!".charAt(0)});
		   pcMap.put("selectPrintDensity",new byte[] {(byte) (char) 18, (byte) "~".charAt(0)});
		   pcMap.put("selectOverlapMode",new byte[] {(byte) (char) 18, (byte) "#".charAt(0)});
		   pcMap.put("selectImageLSBOrMSB",new byte[] {(byte) (char) 18, (byte) "=".charAt(0)});
		   pcMap.put("initializePrinter",new byte[] {(byte) (char) 27, (byte) "@".charAt(0)});		   
		   pcMap.put("raw",new byte[] {});
		}
	
	public boolean printTemplate(OutputStream outputStream) {
		boolean retVal = false;
		byte[] bufferToPrint = null;
		bufferToPrint = returnTemplate();
		if(bufferToPrint != null) {
			retVal = true;
			// write the bytes to the output stream
			try {
				outputStream.write(bufferToPrint);
			} catch (IOException e) {
	        	e.printStackTrace();				
			}
		}
		return retVal;
	}
	
	public byte[] returnTemplate() {
		byte[] templateBuffer = null;
		if(setTokenMap()) {
			templateBuffer = processXMLTemplate();
		}
		return templateBuffer;
	}
	
	private boolean setTokenMap() {
		boolean retVar = true;
		// First make sure the template file exists
		File f = new File(templatePath);
		if(!(f.exists() && f.isFile())) {
			retVar = false;
			System.out.println("stp.setTokenMap template file " + templatePath + " does not exist or is not a file");					
		}
		if(pageWidth == 0) {
			retVar = false;
			System.out.println("stp.setTokenMap page width is not set. You MUST set a page width");								
		}
		if(retVar) {
			// Then set some random data if asked to do so
			if(setTestData) {
				String datePattern = "dd/MM/yyyy";
				String timePattern = "HH:mm";
				SimpleDateFormat dateOnly = new SimpleDateFormat(datePattern);
				SimpleDateFormat timeOnly = new SimpleDateFormat(timePattern);
				// Create some different but coherent dates & times
				// Right now
				Date rightNow = new Date();
				// Contravention date / time is current dates & times
				Date cDateTime = new Date(rightNow.getTime());
				contraventionDate = dateOnly.format(cDateTime);
				contraventionTime = timeOnly.format(cDateTime);
				// P&D expiry will be 15 minutes in the past
				Date panddDate = new Date(rightNow.getTime() - 15 * 60 * 1000L);
				panddExpiryDate = dateOnly.format(panddDate);
				panddExpiryTime = timeOnly.format(panddDate);
				// Observation start 30 minutes ago
				Date obsStartDateTime = new Date(rightNow.getTime() - 30 * 60 * 1000L);
				obsStartDate = dateOnly.format(obsStartDateTime);
				obsStartTime = timeOnly.format(obsStartDateTime);
				// Observation ends 6 minutes ago (just before the contravention)
				Date obsEndDateTime = new Date(rightNow.getTime() - 6 * 60 * 1000L);
				obsEndDate = dateOnly.format(obsEndDateTime);
				obsEndTime = timeOnly.format(obsEndDateTime);
				// Observation ends 6 minutes ago
				PCN = "TEST PRINT PCN";
				VRM = "TEST";
				barCode = "TEST PRINT PCN";
				exactLocation="TEST EXACTLOCATION";
				// Make one long string that will be wrapped
				contraventionLocation = "In sem mauris, imperdiet a mauris a, ";
				contraventionLocation += "sagittis consequat ligula. Vivamus viverra ";
				contraventionLocation += "lacus vitae ex cursus, in lobortis augue imperdiet. ";
				contraventionLocation += "Maecenas euismod nulla erat, ut fermentum massa eleifend et. ";
				contraventionLocation += "Aliquam.";
				contraventionDescription = "Vivamus blandit ipsum quis quam fermentum pharetra ";
				contraventionDescription += "nec et quam. Aliquam eu eleifend nisl, nec placerat ";
				contraventionDescription += "ipsum. Vivamus vitae porta elit. Etiam scelerisque ";
				contraventionDescription += "vitae tellus.";
				contraventionDiscountFee = "0.00";
				contraventionCode = "TEST";
				contraventionSuffix = "";
				vehicleMake = "TEST MAKE";
				vehicleModel = "TEST MODEL";
				vehicleColour = "TEST COLOUR";
				shoulderNumber = DBHelper.getCeoUserId();//CeoApplication.CEOLoggedIn.userId;
				hhID = "FG1234567890123";
				printCount = 9;
				obsDuration = "24 minutes";
			}
			// Now set the map
			valuesMap = new HashMap<String, String>();
			valuesMap.put("PCN", PCN);
			valuesMap.put("WARNINGNOTICE", warningnotice);
			valuesMap.put("EXACTLOCATION",exactLocation);
			valuesMap.put("PCNBARCODE", barCode);
			valuesMap.put("CONTDATE", contraventionDate);
			valuesMap.put("CONTTIME", contraventionTime);
			String cLocWrapped = wrapString(contraventionLocation, pageWidth, "lcn");
			String[] cLocLines = cLocWrapped.split("\\r?\\n");
			for(int i=0;i<cLocLines.length;i++) {
				String variableName = String.format("LOCATION%d", i);
				valuesMap.put(variableName, cLocLines[i]);			 
			}
			valuesMap.put("FULL", contraventionFullFee);
			valuesMap.put("DISCOUNT", contraventionDiscountFee);
			valuesMap.put("VRM", VRM);
			String cDescWrapped = wrapString(contraventionDescription, pageWidth, "cd");
			String[] cDescLines = cDescWrapped.split("\\r?\\n");
			for(int i=0;i<cDescLines.length;i++) {
				String variableName = String.format("CONTRAVENTIONDESC%d", i);
				valuesMap.put(variableName, cDescLines[i]);			 
			}
			valuesMap.put("CONTCODE", contraventionCode);
			valuesMap.put("CONTSFX", contraventionSuffix);
			valuesMap.put("VEHICLEMAKE", vehicleMake);
			valuesMap.put("VEHICLEMODEL", vehicleModel);
			valuesMap.put("VEHICLECOLOUR", vehicleColour);
			valuesMap.put("ISSUEDBY", shoulderNumber);
			valuesMap.put("PANDDEXPIRYDATE", panddExpiryDate);
			valuesMap.put("PANDDEXPIRYTIME", panddExpiryTime);
			valuesMap.put("PRINTEDDATE", printedDate);
			valuesMap.put("PRINTEDTIME", printedTime);
			valuesMap.put("DEVICEID", hhID);
			valuesMap.put("PRINTSEQ", String.format("%d", printCount));
			valuesMap.put("OBSSTARTDATE", obsStartDate);
			valuesMap.put("OBSSTARTTIME", obsStartTime);
			valuesMap.put("OBSENDDATE", obsEndDate);
			valuesMap.put("OBSENDTIME", obsEndTime);
			valuesMap.put("OBSDURATION", obsDuration);
		}
		return retVar;
	}

	private String wrapString(String unWrapped, int pWidth, String whatToWrap) {
		StringBuilder sb = new StringBuilder(unWrapped);
		int i = 0;
		int lclPwidth = pWidth;
		/***************************************************
		** there are 2 fonts, 24 dots & 16 dots
		** Location is always assumed to be in 24 dot
		** Contravention description is always assumed to be
		** in 16 dot so if its a contravention description you 
		** can get 33% more characters on a line but we allow
		** more on the assumption that the location will be 
		** prefixed with the work "Location:" but the contravention
		** will only have the 2 character code in front of it
		****************************************************/
		if(whatToWrap == "cd") {
			lclPwidth = (int) ((double) pWidth * 1.58);
		}
		while (i + lclPwidth < sb.length() && (i = sb.lastIndexOf(" ", i + lclPwidth)) != -1) {
		    sb.replace(i, i + 1, "\n");
		}
		return sb.toString();
	}

	private byte[] processXMLTemplate() {
		byte[] outputBuffer = null;
		try {
	    	File fXmlFile = new File(templatePath);
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	Document doc = dBuilder.parse(fXmlFile);
	    	//optional, but recommended
	    	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	    	doc.getDocumentElement().normalize();
	    	NodeList nList = doc.getElementsByTagName("line");
		    	for (int temp = 0; temp < nList.getLength(); temp++) {	
		    		Node nNode = nList.item(temp);	    				
		    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		    			Element eElement = (Element) nNode;
		    			// Write the control command
		    			byte[] pComm = createControlCommand(eElement.getAttribute("command"), eElement.getAttribute("params"));
		    			if(outputBuffer == null) {
			    			outputBuffer = pComm;	    				
		    			} else {
			    			outputBuffer = concat(outputBuffer, pComm);
		    			}
		    			// Now write the text
		    			// First see if it is in fact an image
		    			if(eElement.getAttribute("format").toLowerCase() == "base64") {
		    				byte[] decoded = Base64.decode(eElement.getTextContent(),0);
			    			if(outputBuffer == null) {
				    			outputBuffer = decoded;	    				
			    			} else {
			    				outputBuffer = concat(outputBuffer, decoded);
			    			}
		    			} else {
		    				byte[] decoded = replaceTokens(eElement.getTextContent(), valuesMap).getBytes();	
			    			if(outputBuffer == null) {
				    			outputBuffer = decoded;	    				
			    			} else {
			    				outputBuffer = concat(outputBuffer, decoded);
			    			}
		    			}
		    		}
		    	}
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }	
    	return outputBuffer;
	}

	public static String replaceTokens(String text, HashMap<String, String> replacements) {
		Pattern pattern = Pattern.compile("\\{(.+?)\\}");
		Matcher matcher = pattern.matcher(text);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String replacement = replacements.get(matcher.group(1));
			if (replacement != null) {
				matcher.appendReplacement(buffer, "");
				buffer.append(replacement);
			} else {
				matcher.appendReplacement(buffer, "");				
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	private byte[] createControlCommand(String commandName, String paramsString) {
		byte[] printCommandString = new byte[0];
		printCommandString = concat(getCommandText(commandName), encodeParams(paramsString));
		return printCommandString;
	}

	private byte[] getCommandText( String commandName) {
	    return pcMap.get(commandName);
	}
	
	private byte[] encodeParams(String paramsString) {
		byte[] printCommandParams = new byte[0];
		// Split on space
		String[] params = paramsString.split("\\s+");
		// Char encode
		for(int p = 0; p < params.length; p++) {
			if(params[p].matches("\\d+")) { // if its an integer
				byte pcp = (byte) Integer.parseInt(params[p]);
				printCommandParams = append(printCommandParams, pcp);
			} else { // So its a string
				if(params[p].length() == 1) {// If its not 1 char its a set up error
					byte pcp = (byte) params[p].charAt(0);
					printCommandParams = append(printCommandParams, pcp);
				}
			}
		}
		return printCommandParams;
	}

	byte[] append(byte[] a, byte b)
	{
	    // create the result array
	    byte[] result = new byte[a.length + 1];
	    // copy the source arrays into the result array
	    int currentIndex = 0;
        System.arraycopy(a, 0, result, currentIndex, a.length);
        currentIndex += a.length;
        result[currentIndex] = b;
	    return result;
	}

	byte[] concat(byte[]...arrays)
	{
	    // Determine the length of the result array
	    int totalLength = 0;
	    for (int i = 0; i < arrays.length; i++) {
	        totalLength += arrays[i].length;
	    }
	    // create the result array
	    byte[] result = new byte[totalLength];
	    // copy the source arrays into the result array
	    int currentIndex = 0;
	    for (int i = 0; i < arrays.length; i++) {
	        System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].length);
	        currentIndex += arrays[i].length;
	    }
	    return result;
	}
}
