package legacy.hedge;

import legacy.service.implementation.TradingDataAccessServiceImpl;

public class MockTradingDataAccessServiceImpl extends TradingDataAccessServiceImpl {

    protected void synchronizationTimer(int countInSeconds) {
        System.out.println("synchronizationTimer");
    }
}
