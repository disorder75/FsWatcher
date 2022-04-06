package eu.europa.acer.fswatcher.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ProcessExecuterTest {

	private static final String SCRIPT_NAME = "junit_script.sh";
	private static String path;
	
	@Autowired
	ResourceLoader resourceLoader;

	@BeforeAll
	static public void setUp() throws URISyntaxException {
		
		/*
		 *		This is for test only, DO NOT TRY to execute a script
		 *		inside your packaged jar, you can't.
		 *		If you really need to execute scripts from jar then unzip the 
		 *		target resource into the tmp folder of the guest filesystem 
		 */
		
		URL urlRes = ProcessExecuterTest.class.getClassLoader().getResource(SCRIPT_NAME);
		File fres = Paths.get(urlRes.toURI()).toFile();
		fres.setExecutable(true, false);
		path = fres.toURI().getPath();
		
	}
	
	@Test
	public void contextLoads() {
		System.out.println("context loaded");
	}

	@Test
	public void test01_script_execute_and_print() throws IOException {
		String fpath = path;
		System.out.println("executing " + fpath);
		ProcessBuilder pb = new ProcessBuilder().command(fpath);
		pb.redirectErrorStream(true);
		Process proc = pb.start();
		ProcessHandle h = proc.toHandle();
		System.out.println("process in execution has pid " + h.pid());			
		
        try (InputStream processStdOutput = proc.getInputStream();
        	 Reader r = new InputStreamReader(processStdOutput);
        	 BufferedReader br = new BufferedReader(r)) {
            String line;
            while ((line = br.readLine()) != null) {
            	System.out.println(line);
            }
   	    }
		System.out.println("process completed: " + h.pid());			
	}

	@Test
	public void test02_script_execute_and_wait() throws IOException, InterruptedException {
		String fpath = path;
		System.out.println("executing " + fpath);
		ProcessBuilder pb = new ProcessBuilder().command(fpath);
		pb.redirectErrorStream(true);
		Process proc = pb.start();
		ProcessHandle h = proc.toHandle();

		/*
		 *		consume on separate thread the output to avoid script hang 
		 */
		 new Thread(new Runnable() {
			public void run() {
				try (InputStream processStdOutput = proc.getInputStream();
		           	 Reader r = new InputStreamReader(processStdOutput);
		           	 BufferedReader br = new BufferedReader(r)) {
		               String line;
		               while ((line = br.readLine()) != null) {
		            	   System.out.println(line);
		               }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
		 }).start();
		
		int ret = proc.waitFor();
		System.out.println("process exit status: " + ret);
		System.out.println("process completed: " + h.pid());
	}			
	
}
