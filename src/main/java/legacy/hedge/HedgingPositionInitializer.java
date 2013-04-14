package legacy.hedge;

import legacy.DateTimeUtils;
import legacy.dto.Book;
import legacy.dto.Transaction;
import legacy.security.User;
import legacy.security.UserSessionsManager;
import legacy.service.*;

import java.math.BigInteger;
import java.util.Date;

public class HedgingPositionInitializer {

    ITradingDataAccessService trading;
    IHedgingPositionDataAccessService hpdas;

    public HedgingPositionInitializer(ITradingDataAccessService trading, IHedgingPositionDataAccessService hpdas) {
        this.trading = trading;
        this.hpdas = hpdas;
    }

    public HedgingPosition initHedgingPosition(HedgingPosition hp) {

        // Create Parameter class
        Transaction transaction = trading.getTransactionById(hp.getId());
        long dId = trading.getOptionalIdFromTransaction(transaction);

        double price = hpdas.getPriceQuote(dId, transaction);
        long dps = trading.computeDPSOnTheGrid(transaction.getOuterEdge());
        String combck = dId + " " + transaction.getId() + " CONTROL: [" + hpdas.getControl() + "]";
        Date valueDate = new Date();
        try {
            valueDate = hp.getValueDate();
        } catch(Exception e) {
            valueDate = transaction.getValueDate();
        }

        String hedgingTransactionId = new String();
        if (!HedgingPositionTypeConst.INI.equals(hp.getType())) {
            hedgingTransactionId = hpdas.getHedgingTransactionIdByTransactionId(transaction.getId());
        }
        String userIni = getUser();
        hp.setIkRtH(userIni);


        // Apply command pattern : one method apply
        switch (hp.getType()) {
            case INI: {
                String transactionWay = new String();
                switch (transaction.getWay()) {
                    case LONG:
                        transactionWay = "L";
                        break;
                    case SHORT:
                        transactionWay = "S";
                        break;
                    default:
                        break;
                }
                int bodCode = 0;
                Integer stock = DataAccessService.getAnalyticalService().getRetrieveStockByActiveGK(transaction.getId(), transactionWay);
                TradingOrder evt = hpdas.getTrade(transaction.getId());
                boolean isStockForbidden = false;
                if (stock == null) {
                    isStockForbidden = true;
                }
                if (!isStockForbidden) {
                    Book book = DataAccessService.getAnalyticalService().getBookByName(transaction.getBookName());
                    bodCode = book.getCode();
                } else {
                    Book book = DataAccessService.getAnalyticalService().getBookByName(transaction.getBookName() + "-instock");
                    bodCode = Integer.parseInt(book.getPortfolioIdFromRank());
                }
                /*********************************** INPUT DEAL DATA *********************/
                hp.setTransactionWay(transactionWay);
                hp.setCodetyptkt(34);
                hp.setCodtyptra(BigInteger.valueOf(bodCode));
                hp.setQuantity(String.valueOf(evt.getPrice().getQuantity()));
                hp.setBasprx(evt.getPrice().getFxPrice() / 100);
                hp.setPrxref(evt.getPrice().getFxPrice());
                hp.setCombck(combck);
                /*********************************** INPUT EVENT DATA *********************/
                hp.setTransactionId(transaction.getId());
                hp.setValueDate(valueDate);
            }
            case CANCEL_TRANSACTION:
                /*********************************** INPUT DEAL DATA *********************/
                String hedgingPositionId = hpdas.getHedgingPositionIdByPositionKey(transaction.getPositionKey());

                hp.setCodetyptkt(20);
                /*********************************** INPUT EVENT DATA *********************/
                hp.setValueDate(valueDate);
                break;
            case EXT:
                TradingOrder evt = hpdas.getTrade(transaction.getId());
                double fxprice = -1d;
                if (evt !=null ){
                    price = evt.getPrice().getPrice();
                    fxprice = evt.getPrice().getFxPrice();
                }
                if (price > 0) {
                    price = price * fxprice;
                }
                /*********************************** INPUT DEAL DATA *********************/
                hp.setBasprx(price / 100);
                hp.setPrxref(price);
                hp.setCodetyptkt(42);
                hp.setQuantity(String.valueOf(evt.getPrice().getQuantity()));
                /*********************************** INPUT EVENT DATA *********************/
                Date issueDate = transaction.getIssueDate();
                Date tradeDate = transaction.getTradeDate();
                if (DateTimeUtils.compareDate(issueDate, tradeDate)) {
                    hp.setCreDate(issueDate);
                    hp.setDaprx(tradeDate);
                    hp.setDatefinthe(valueDate);
                } else {
                    hp.setCreDate(issueDate);
                    hp.setDaprx(tradeDate);
                    hp.setDatefinthe(tradeDate);
                    hp.setCombck(combck);
                }
                hp.setValueDate(valueDate);
                break;
            case CANCEL_POSITION:
                /*********************************** INPUT DEAL DATA *********************/
                hp.setCodetyptkt(20);
                hp.setHedgingTransactionId(hedgingTransactionId);
                /*********************************** INPUT EVENT DATA *********************/
                hp.setValueDate(valueDate);
                break;
        }

        return hp;
    }

    private String getUser() {
        User user = UserSessionsManager.getInstance().getCurrentUser();
        if (user !=null) {
            return user.getName();
        } else {
            return "autobot";
        }
    }
}
