//package UDP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GBNSRSender {
  
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		FileInputStream fsStream;
		
		// reading the input commands from the given file	
		//System.getProperty("user.dir")+"\\"
		int portNo = Integer.parseInt(args[1]);
		int numPackets = Integer.parseInt(args[2]);
		File file = new File(System.getProperty("user.dir")+"\\"+args[0]+".txt");
	    fsStream = new FileInputStream(file);
			
		BufferedReader reader = new BufferedReader(new InputStreamReader(fsStream, "UTF-8"));
		String line;
		ArrayList<String> list = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			
		    list.add(line);
		}
		String[] stringArray = list.get(1).split(" ");
		int windowSize = Integer.parseInt(stringArray[1]);
		System.out.println(list.get(0));
		if(!list.isEmpty()) {
		if(list.get(0).equalsIgnoreCase("Gbn")) {
		System.out.println("********GO BACK N Protocol******************");
		System.out.println("**Window size =>"+windowSize+"**Segment Size =>"+list.get(3)+"***Timeout =>"+list.get(2)+"**listening at "+portNo);
		GoBackNSender gbnSender = new GoBackNSender(list.get(0),Integer.parseInt(list.get(2)),windowSize,
				Integer.parseInt(list.get(3)),portNo);
		//GoBackNSender goBackSender = new GoBackNSender(protocol, timeout, windowSize, segmentSize, portNo);
		gbnSender.processFile();
		
		}else {
			
			System.out.println("********SR Protocol******************");
			System.out.println("**Window size =>"+windowSize+"**Segment Size =>"+list.get(3)+"***Timeout =>"+list.get(2)+"**listening at "+portNo);
			SelectiveRepeatSender srSender = new SelectiveRepeatSender(list.get(0),Integer.parseInt(list.get(2)),windowSize,
					Integer.parseInt(list.get(3)),portNo);
			//GoBackNSender goBackSender = new GoBackNSender(protocol, timeout, windowSize, segmentSize, portNo);
			srSender.processFile();
			
		}
	}else {
		System.out.println("*****Input list is empty****Error in input parameters or input file********");
	}
}
}
