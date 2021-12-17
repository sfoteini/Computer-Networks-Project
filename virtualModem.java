package networks;

import java.util.Scanner;
import java.lang.Math;
import java.io.FileOutputStream;
import java.io.IOException;
import ithakimodem.Modem;

/* 
* 
* Δίκτυα Υπολογιστών I 
* 
* Experimental Virtual Lab 
* 
* Java virtual modem communications seed code 
* 
*/
public class virtualModem {
	
	private static String codeEcho= "E4350";
	private static String codeImageErrorFree="M6747CAM=PTZ";
	private static String codeImageWithErrors="G2189CAM=PTZ";
	private static String codeGPS="P7171";
	private static String codeACK="Q5233";
	private static String codeNACK="R9945";
	
	public static void main(String[] param) throws IOException {
		
		System.out.println("USER APPLICATION MENU");
		System.out.println("1. Echo\n2. Error Free Image\n3. Image With Errors\n4. GPS\n5. ARQ\n");
		
		Scanner in = new Scanner (System.in);
		int opt = in.nextInt();
		in.close();
		
		if (opt == 1) {	
			(new virtualModem()).echo();
		}
		else if (opt == 2) {
			(new virtualModem()).getImage(virtualModem.codeImageErrorFree, "ErrorFreeImage.jpg");
		}
		else if (opt == 3) {
			(new virtualModem()).getImage(virtualModem.codeImageWithErrors, "ImageWithErrors.jpg");
		}
		else if (opt == 4) {
			(new virtualModem()).getGps();
		}
		else if (opt == 5) {
			(new virtualModem()).arq();
		}
		else {
			System.out.println("Try again!");
		}
	}
 
	
	/**
	 * Echo Function
	 */
	public void echo() throws IOException {
		
		int k;
		Modem modem;
		modem=new Modem();
		modem.setSpeed(9000);
		modem.setTimeout(2000);
		modem.open("ithaki");
		
		
		for (;;) {
			try {
				k=modem.read();
				if (k==-1) {break;}
				System.out.print((char)k);
			} catch (Exception x) {
				break;
			}
		}
		
		FileOutputStream echoTimeWriter = new FileOutputStream("echoTimes.txt", false);
		FileOutputStream echoCounterWriter = new FileOutputStream("echoCounter.txt", false);

		int runtime = 5, cntPackets = 0; //runtime 5mins
		String str = "";
		System.out.printf("Runtime (minutes) = " + runtime + "\n");
		long startTime = System.currentTimeMillis();
		long stopTime = startTime + 60*1000*runtime; // 60 seconds * 1000 ms/sec
		long sendTime=0, receiveTime=0;
		
		String time = "", ClockTime = "";
		echoTimeWriter.write("Clock Time\tSystem Time\r\n".getBytes());
		
		while (System.currentTimeMillis() <= stopTime) {
			cntPackets++;
			sendTime=System.currentTimeMillis();
			modem.write((virtualModem.codeEcho + "\r").getBytes());
			
			for (;;) {
				try {
					k = modem.read();
					System.out.print((char)k);
					str+=(char)k;
					if (k==-1){
						System.out.println("\nConnection closed.");
						return;
					}
				
					if (str.endsWith("PSTOP")){
						receiveTime=System.currentTimeMillis();
						ClockTime=str.substring(18, 26)+"\t";
						time = String.valueOf((receiveTime-sendTime)+ "\r\n");
						echoTimeWriter.write(ClockTime.getBytes());
						echoTimeWriter.write(time.getBytes());
						echoTimeWriter.flush();
						str="";
						break;
					}
				}
				catch (Exception x) {
					break;
				}
			}
			
			System.out.println("");
		}
		modem.close();

		echoCounterWriter.write(("\r\nRuntime: " + String.valueOf(runtime)).getBytes());
		echoCounterWriter.write(("\r\nPackets Received: " + String.valueOf(cntPackets)).getBytes());
		echoCounterWriter.close();
		echoTimeWriter.close();
		 
		System.out.println("Packets Received: " + cntPackets);
		System.out.println("\n\nFiles created!");
	}
	
	/**
	 * Image Function
	 */
	public void getImage(String code, String fileName) throws IOException {
		
		int k;
		Modem modem;
		modem=new Modem();
		modem.setSpeed(9000);
		modem.setTimeout(2000);
		modem.open("ithaki");
		
		
		for (;;) {
			try {
				k=modem.read();
				if (k == -1) {break;}
				System.out.print((char)k);
			} 
			catch (Exception x) {
				break;
			}
		}
		
		
		boolean check;
		FileOutputStream writer = new FileOutputStream(fileName, false);
		check = modem.write((code+"\r").getBytes());
		System.out.println("\nReceiving " + fileName + "...");

		if (check == false) {
			System.out.println("Connestion closed");
			writer.close();
			return;
		}

		for (;;){
			try {
				k=modem.read();
				writer.write((char)k);
				writer.flush();
				
				if (k == -1){
					System.out.println("Finished!");
					break;
				}
			}
			catch (Exception x) {
				break;
			}

		}
		writer.close();
		 
		modem.close();
	}
	
	
	/**
	 * GPS function
	 */
	public void getGps() throws IOException {
		
		int k;
		Modem modem;
		modem=new Modem();
		modem.setSpeed(9000);
		modem.setTimeout(2000);
		modem.open("ithaki");
		
		
		for (;;) {
			try {
				k=modem.read();
				if (k==-1) {break;}
				System.out.print((char)k);
			} catch (Exception x) {
				break;
			}
		}
		
		int lines = 99;
		String R_code = "";
		R_code = virtualModem.codeGPS + "R=10200" + lines;
		System.out.println("Executing R parameter = " + R_code + " (XPPPPLL)");

		modem.write((R_code+"\r").getBytes());
		
		String str = "";
				
		for (;;) {
			try {
				k=modem.read();
				if (k == -1) {break;}
				System.out.print((char)k);
				str+=(char)k;
			} 
			catch (Exception x) {
				break;
			}
		}

		String[] strLines = str.split("\r\n");
		if (strLines[0].equals("n.a")){
			System.out.println("Error: Receiving GPS packets failed");
			return; 
		}
		 
		System.out.println("**TRACES**\n");
		float time1=0,time2=0; // times for the traces
		int diff=8; // set the difference between traces
		diff=diff*100/60;
		int tracesNum=7; // set the number of traces to be taken
		String[] traces = new String[tracesNum+1];
		int tracesCnt=0, flag=0;

		for (int i=0; i<lines; i++){
			if (strLines[i].startsWith("$GPGGA")){
				if (flag==0){
					String temp = strLines[i].split(",")[1];
					time1= Integer.valueOf(temp.substring(0, 6))*100/60;
					flag=1;
				}
				String temp = strLines[i].split(",")[1];
				time2= Integer.valueOf(temp.substring(0, 6))*100/60;

				if (Math.abs(time2-time1)>=diff){
					traces[tracesCnt]=strLines[i];
					if (tracesCnt==tracesNum)
						break;
					tracesCnt++;
					time1=time2;
				}
			}
		}

		// Print Traces
		for (int i=0; i<tracesNum; i++){
			System.out.println(traces[i]);
		}

		String T_code = "", T_code_final = virtualModem.codeGPS +"T=";
		System.out.println();
		for (int i=0; i<tracesNum; i++){
			String[] strSplit = traces[i].split(",");
			System.out.print("T parameter = ");
			String aa = strSplit[4].substring(1,3);
			String bb = strSplit[4].substring(3,5);
			String cc = String.valueOf(Integer.parseInt(strSplit[4].substring(6,10))*60/100).substring(0,2);
			String dd = strSplit[2].substring(0,2);
			String ee = strSplit[2].substring(2,4);
			String zz = String.valueOf(Integer.parseInt(strSplit[2].substring(5,9))*60/100).substring(0,2);
			T_code = aa+bb+cc+dd+ee+zz + "T";
			//n=n+5;
			System.out.println(T_code);//.substring(0,T_code.length()));
			T_code_final = T_code_final + T_code + "=";
		}
		T_code_final=T_code_final.substring(0,T_code_final.length()-2);
		System.out.println("\nSending code: "+T_code_final);
		modem.close();
		
		getImage(T_code_final, "GPS Traces.jpg");
	}
	
	/**
	 * ARQ function
	 */
	public void arq() throws IOException {
		FileOutputStream arqTimeWriter= new FileOutputStream("ARQtimes.txt", false); 
		FileOutputStream arqCounterWriter= new FileOutputStream("ARQcounter.txt", false);
		
		int k;
		Modem modem;
		modem=new Modem();
		modem.setSpeed(9000);
		modem.setTimeout(2000);
		modem.open("ithaki");
		
		
		for (;;) {
			try {
				k=modem.read();
				if (k==-1) {break;}
				System.out.print((char)k);
			} catch (Exception x) {
				break;
			}
		}

		int runtime=5, xor=1, fcs=1, cntTotalPackages=0, cntRepeatsTotal=0;
		int[] cntNackTimes= new int[15];
		int cntNackPerPackage=0;
		String time="", ClockTime="", str="";
		long startTime = System.currentTimeMillis();
		long stopTime = startTime + 60*1000*runtime; // 60 seconds * 1000 ms/sec
		long sendTime=0, receiveTime=0;
		System.out.printf("Runtime (minutes) = "+ runtime+"\n");

		arqTimeWriter.write("Clock Time\tSystem Time\tPacket Resends\r\n".getBytes());
		
		while (System.currentTimeMillis() <= stopTime) {
			if (xor==fcs) {
				cntTotalPackages++;
				cntNackTimes[cntNackPerPackage]++;
				cntNackPerPackage=0;
				sendTime = System.currentTimeMillis();
				modem.write((virtualModem.codeACK + "\r").getBytes());
			}
			else{
				cntRepeatsTotal++;
				cntNackPerPackage++;
				modem.write((virtualModem.codeNACK + "\r").getBytes());
			}

			for (;;){
				try {
					k = modem.read();
					System.out.print((char)k);
					str+=(char)k;
					if (k==-1){
						System.out.println("\nConnection TIMED OUT");
						return;
					}
					if (str.endsWith("PSTOP")){
						receiveTime=System.currentTimeMillis();
						break;
					}
				}
				catch (Exception x) {
					break;
				}
			}

			System.out.println("");
			String[] s = str.split("<");
			s = s[1].split(">");
			fcs = Integer.parseInt(s[1].substring(1, 4));
			xor = s[0].charAt(0)^s[0].charAt(1);
			for (int i=2; i<16; i++){
				xor=xor^s[0].charAt(i);
			}
			if (xor==fcs) {
				System.out.println("Packet OK!");
				receiveTime = System.currentTimeMillis();
				time = String.valueOf((receiveTime - sendTime)+"\t");
				ClockTime = str.substring(18, 26)+"\t";
				arqTimeWriter.write(ClockTime.getBytes());
				arqTimeWriter.write(time.getBytes());

				arqTimeWriter.write((String.valueOf(cntNackPerPackage)+"\r\n").getBytes());
				arqTimeWriter.flush();
			}
			else {
				xor=0;
			}
		 
			str="";
		}
		modem.close();

		arqCounterWriter.write(("\r\nRuntime: "+String.valueOf(runtime)).getBytes());
		arqCounterWriter.write("\r\nPackets Received (ACK): ".getBytes());
		arqCounterWriter.write(String.valueOf(cntTotalPackages).getBytes());
		arqCounterWriter.write("\r\nPackets Resent (NACK): ".getBytes());
		arqCounterWriter.write(String.valueOf(cntRepeatsTotal).getBytes());
		arqCounterWriter.write("\r\nNACK Time Details...".getBytes());
		
		for (int i=0; i<cntNackTimes.length; i++){
			arqCounterWriter.write(("\r\n"+i+":\t"+cntNackTimes[i]).getBytes());
		}
		
		arqCounterWriter.close();
		arqTimeWriter.close();
		System.out.println("Packets Received: "+cntTotalPackages);
		System.out.println("Packets Resent: "+cntRepeatsTotal);
		System.out.println("\n\nFile arqTimes.txt created succesfuly!!");
		System.out.println("File arqCounter.txt created succesfuly!!");
	}

}
