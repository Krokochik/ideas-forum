package com.krokochik.CampfireGallery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class veryHardSite {


	public static void main(String[] args) {
		SpringApplication.run(veryHardSite.class, args);
		InetAddress ip ;
		String hostname ;
		try {
			ip = InetAddress.getByName("ap-plication.herokuapp.com");
			StringBuilder builder = new StringBuilder();
			for( byte el : ip.getAddress() ) {
				builder.append(el);
			}
			String ipAddress = builder.toString();
			hostname =  ip.getHostName();
			System.out.println ( "Your current IP address: " + ipAddress + ip ) ;
			System.out.println("Your current hostname: " + hostname) ;
		} catch(UnknownHostException e) {
			System.out.println("exc");
		}

	}
}
