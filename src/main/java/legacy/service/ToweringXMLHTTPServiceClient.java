package legacy.service;

import legacy.dto.InputEvent;
import legacy.hedge.HedgingPosition;

public class ToweringXMLHTTPServiceClient {

	public HedgingPosition sendTicketToTowering(InputEvent event) {
		for (int i = 1; i <= 100; i++) {
			buildHttpRequestAndSendToTowering(event);
			System.out.println("[remote] Sending HedgingPosition chunk #"+i+" to Towering");
		}
		return new HedgingPosition();
	}





















	private static void buildHttpRequestAndSendToTowering(InputEvent event) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
