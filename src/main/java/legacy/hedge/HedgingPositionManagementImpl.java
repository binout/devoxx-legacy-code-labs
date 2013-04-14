package legacy.hedge;

import legacy.dto.Modif;
import legacy.error.ARPSystemException;
import legacy.error.CheckResult;
import legacy.persistence.StorageActionEnum;
import legacy.service.DataAccessService;
import legacy.service.IHedgingPositionDataAccessService;
import legacy.service.ITradingDataAccessService;
import legacy.service.ITransactionManagerService;
import org.apache.commons.lang3.SerializationUtils;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HedgingPositionManagementImpl implements IHedgingPositionManagement {

	private static int MAX_DECIMALS = 4;
	private static Logger LOGGER = Logger.getLogger(HedgingPositionManagementImpl.class.getName());

    ITransactionManagerService transactionManagerService;
    HedgingPositionMgt headingPositionMgt;
    ITradingDataAccessService trading;
    IHedgingPositionDataAccessService hpdas;
    HedgingPositionInitializer initializer;

    public HedgingPositionManagementImpl() {
        transactionManagerService = DataAccessService.getTransactionManagerService();
        headingPositionMgt = new HedgingPositionMgt();
        trading = DataAccessService.getTradingDataAccessService();
        hpdas = DataAccessService.getHedgingPositionDataAccessService();
        initializer = new HedgingPositionInitializer(trading, hpdas);
    }

    @Override
	public CheckResult<HedgingPosition> initAndSendHedgingPosition(HedgingPosition refHp) throws ARPSystemException {
		CheckResult<HedgingPosition> result = new CheckResult<HedgingPosition>();
        HedgingPosition hp = SerializationUtils.clone(refHp);

		try {
			hp = initializer.initHedgingPosition(hp);
		} catch (Exception e) {
			String errorMsg = "TECHNICAL ERROR, cannot initialize HP to send";
			LOGGER.log(Level.SEVERE, errorMsg, e);
			String msg = hp.getErrorLevel().createHMsgFromError();
			hp.setHedgeMsg(msg);
			result.setCheckIsOk(false);
			try {
				updateHedgingPosition(hp);
			} catch (ARPSystemException e1) {
				LOGGER.log(Level.SEVERE, errorMsg, e1);
			}
			return result;
		}

		try {
			result = hedgePositionBySendTo3rdParty(hp);
            hp = result.getResult();
            setStatus(result);
            updateHedgingPosition(hp);
        } catch(ARPSystemException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(), e);
		}
		return result;
	}

    private void setStatus(CheckResult<HedgingPosition> result) {
        HedgingPosition hp = result.getResult();
        if (result.isCheckIsOk()) {
            hp.setStatus(HedgingPositionStatusConst.HEDGED);
        } else {
            switch (hp.getErrorLevel()) {
                case FUNCTIONAL_ERROR: {
                    hp.setStatus(HedgingPositionStatusConst.REJECTED);
                    break;
                }
                case CONNECT_ERROR: {
                    hp.setStatus(HedgingPositionStatusConst.REJECTED);
                    break;
                }
                case BOOKING_MALFUNCTION: {
                    //TO DO
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private CheckResult<HedgingPosition> hedgePositionBySendTo3rdParty(HedgingPosition hp) {
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.log(Level.FINEST,"Begin 3r party processing. stand by");
		}
		CheckResult<HedgingPosition> result = headingPositionMgt.hedgingPositionMgt(hp);
		if (LOGGER.isLoggable(Level.FINEST)) {
			LOGGER.log(Level.FINEST,"3r party processing is now finished, thank you for your patience"); // t'es con michel
		}
		return result;
	}

	private HedgingPosition updateHedgingPosition(HedgingPosition hp) {
		try {
			if (hp.getType().equals(HedgingPositionTypeConst.INI)) {
                hp.setTransactionId(hp.getTransactionId());
				Modif modif = new Modif();
				modif.setCreDate(new Date());
				hp.setLastModification(modif);
				hp.setStorageUpdate(StorageActionEnum.CREATE);
                hp = transactionManagerService.classStorageAction(hp);
			} else {
				hp.setStorageUpdate(StorageActionEnum.UPDATE);
                hp = transactionManagerService.classStorageAction(hp);
			}
		} catch(Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
			throw e;
		}
		return hp;
	}

}
