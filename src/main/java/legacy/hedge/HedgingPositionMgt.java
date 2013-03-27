package legacy.hedge;

import legacy.error.CheckResult;
import legacy.dto.InputEvent;
import legacy.service.ToweringXMLHTTPServiceClient;
import org.apache.commons.lang3.SerializationUtils;

public class HedgingPositionMgt {

    private ToweringXMLHTTPServiceClient client = new ToweringXMLHTTPServiceClient();

	public CheckResult<HedgingPosition> hedgingPositionMgt(HedgingPosition hp) {
		CheckResult<HedgingPosition> result = new CheckResult<HedgingPosition>();
        sendHp(hp);
		result.setCheckIsOk(true);
		result.setResult(SerializationUtils.clone(hp));
		return result;
	}

    protected void sendHp(HedgingPosition hp) {
        InputEvent event = new InputEvent(hp);
        client.sendTicketToTowering(event);
    }

}
