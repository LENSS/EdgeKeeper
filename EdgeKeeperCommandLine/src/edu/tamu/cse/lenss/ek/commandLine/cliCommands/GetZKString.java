package edu.tamu.cse.lenss.ek.commandLine.cliCommands;

import java.io.IOException;
import java.util.concurrent.Callable;

import edu.tamu.cse.lenss.edgeKeeper.client.EKClient;
import edu.tamu.cse.lenss.ek.commandLine.MasterCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "get_zookeeper", 
        description = "Fetches the the locak Zookeeper connecting string")
public class GetZKString implements Callable<Void> {
	
    @ParentCommand MasterCommand parent;
    
    public GetZKString(){}

    public Void call() throws IOException {
    	parent.out.println(EKClient.getZooKeeperConnectionString());
        return null;
    }
}
