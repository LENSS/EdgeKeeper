package edu.tamu.cse.lenss.edgeKeeper.zk;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerMain;

import edu.tamu.cse.lenss.edgeKeeper.utils.Terminable;

import org.apache.zookeeper.server.quorum.QuorumPeer.ServerState;

public class ZKServMulti  extends QuorumPeerMain implements  Terminable {
    static final Logger logger = Logger.getLogger(ZKServMulti.class);
	QuorumPeerConfig qConfig;
	
	private static AtomicInteger restartSeq = new AtomicInteger(0);
	
	public ZKServMulti(QuorumPeerConfig qConfig) {
		this.qConfig = qConfig;
	}
	
	public void terminate() {
		if(this.quorumPeer!=null) {
			this.quorumPeer.shutdown();
			logger.log(Level.ALL, "Shutdown Zookeeper server. parallel run = "+restartSeq.decrementAndGet());
		}
		logger.info("Terminated"+this.getClass().getName());
	}
	public ServerState getServerState() {
		return (this.quorumPeer==null)? null: this.quorumPeer.getPeerState();
	}
	
	public void restart() {
		new Thread() {
			public void run() {
				try {
					terminate();
					logger.log(Level.ALL, "Starting Zookeeper server. parallel run = "+restartSeq.incrementAndGet());
					runFromConfig(qConfig);
				} catch (IOException e) {
					logger.fatal("Problem in running Zookeeper server", e);
		            terminate();
				}
			}
		}.start();
		
	}

	@Override
	public void run() {
		restart();
		
	}

	
}
